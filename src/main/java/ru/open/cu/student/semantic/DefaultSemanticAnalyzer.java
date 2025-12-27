package ru.open.cu.student.semantic;

import ru.open.cu.student.ast.*;
import ru.open.cu.student.ast.ColumnRef;
import ru.open.cu.student.catalog.manager.CatalogManager;
import ru.open.cu.student.catalog.model.TableDefinition;
import ru.open.cu.student.catalog.model.ColumnDefinition;
import ru.open.cu.student.catalog.model.TypeDefinition;
import ru.open.cu.student.parser.nodes.*;

import java.util.*;

public class DefaultSemanticAnalyzer implements SemanticAnalyzer {

    private CatalogManager catalog;

    @Override
    public QueryTree analyze(AstNode ast, CatalogManager catalog) throws SemanticException {
        this.catalog = catalog;

        if (ast instanceof SelectStmt) {
            return analyzeSelect((SelectStmt) ast);
        } else if (ast instanceof InsertStmt) {
            return analyzeInsert((InsertStmt) ast);
        } else if (ast instanceof CreateTableStmt) {
            return analyzeCreateTable((CreateTableStmt) ast);
        } else {
            throw new SemanticException("Неподдерживаемый тип запроса: " +
                    ast.getClass().getSimpleName());
        }
    }

    private QueryTree analyzeSelect(SelectStmt select) {
        QueryTree queryTree = new QueryTree();
        queryTree.setCommandType(QueryType.SELECT);

        // 1. Анализ FROM clause
        Map<String, TableInfo> tableInfoMap = new HashMap<>();

        if (select.fromClause != null && !select.fromClause.isEmpty()) {
            for (int i = 0; i < select.fromClause.size(); i++) {
                RangeVar rangeVar = select.fromClause.get(i);

                // Проверяем существование таблицы
                String fullTableName = getFullTableName(rangeVar);
                TableDefinition tableDef = catalog.getTable(fullTableName);

                if (tableDef == null) {
                    throw new SemanticException("Таблица не найдена: " + fullTableName);
                }

                // Добавляем в rangeTable
                queryTree.addRangeVar(rangeVar);

                // Сохраняем информацию о таблице
                TableInfo tableInfo = new TableInfo();
                tableInfo.tableDef = tableDef;
                tableInfo.tableIndex = i;
                tableInfo.columns = catalog.getTableColumns(tableDef);

                String key = rangeVar.alias != null ? rangeVar.alias : tableDef.getName();
                tableInfoMap.put(key.toLowerCase(), tableInfo);

                // Сохраняем в QueryTree
                List<QueryTree.ColumnInfo> columnInfos = new ArrayList<>();
                for (int colIdx = 0; colIdx < tableInfo.columns.size(); colIdx++) {
                    ColumnDefinition colDef = tableInfo.columns.get(colIdx);
                    TypeDefinition typeDef = catalog.getType(colDef.getTypeOid());

                    QueryTree.ColumnInfo colInfo = new QueryTree.ColumnInfo(
                            tableDef.getName(),
                            colDef.getName(),
                            typeDef,
                            i,
                            colIdx
                    );
                    columnInfos.add(colInfo);
                }

                QueryTree.TableInfo qTableInfo = new QueryTree.TableInfo(
                        tableDef.getName(),
                        rangeVar.alias,
                        columnInfos
                );
                queryTree.addTableInfo(key, qTableInfo);
            }
        }

        // 2. Анализ SELECT list
        if (select.targetList != null && !select.targetList.isEmpty()) {
            for (ResTarget target : select.targetList) {
                if (target.val instanceof ColumnRef) {
                    // Преобразуем parser ColumnRef в ast ColumnRef
                    ColumnRef parserColRef =
                            (ColumnRef) target.val;

                    ColumnRef astColRef = resolveColumnRef(parserColRef, tableInfoMap);

                    if (target.name != null) {
                        astColRef.setAlias(target.name);
                    }

                    TargetEntry targetEntry = new TargetEntry(astColRef, target.name);
                    queryTree.addTargetEntry(targetEntry);
                } else {
                    // Для других типов выражений
                    TargetEntry targetEntry = new TargetEntry((Expr) target.val, target.name);
                    queryTree.addTargetEntry(targetEntry);
                }
            }
        }

        // 3. Анализ WHERE clause
        if (select.whereClause != null) {
            if (select.whereClause instanceof AExpr) {
                 AExpr parserAExpr =
                        (AExpr) select.whereClause;
                Expr whereExpr = analyzeParserAExpr(parserAExpr, tableInfoMap);
                queryTree.setWhereClause(whereExpr);
            } else if (select.whereClause instanceof Expr) {
                queryTree.setWhereClause((Expr) select.whereClause);
            }
        }

        return queryTree;
    }

    private ColumnRef resolveColumnRef(ColumnRef parserColRef,
                                       Map<String, TableInfo> tableInfoMap) {
        String columnName = parserColRef.column.toLowerCase();
        String tableName = parserColRef.table != null ? parserColRef.table.toLowerCase() : null;

        // Вариант 1: Явное указание таблицы
        if (tableName != null) {
            TableInfo tableInfo = tableInfoMap.get(tableName);
            if (tableInfo != null) {
                // Ищем колонку в таблице
                for (int i = 0; i < tableInfo.columns.size(); i++) {
                    ColumnDefinition colDef = tableInfo.columns.get(i);
                    if (colDef.getName().equalsIgnoreCase(columnName)) {
                        return new ColumnRef(
                                tableInfo.tableDef.getName(),
                                colDef.getName(),
                                tableInfo.tableIndex,
                                i
                        );
                    }
                }
            }
            throw new SemanticException("Колонка не найдена: " + parserColRef);
        }

        // Вариант 2: Неявное указание - ищем во всех таблицах
        List<ColumnRef> candidates = new ArrayList<>();

        for (TableInfo tableInfo : tableInfoMap.values()) {
            for (int i = 0; i < tableInfo.columns.size(); i++) {
                ColumnDefinition colDef = tableInfo.columns.get(i);
                if (colDef.getName().equalsIgnoreCase(columnName)) {
                    candidates.add(new ColumnRef(
                            tableInfo.tableDef.getName(),
                            colDef.getName(),
                            tableInfo.tableIndex,
                            i
                    ));
                }
            }
        }

        if (candidates.isEmpty()) {
            throw new SemanticException("Колонка не найдена: " + parserColRef.column);
        } else if (candidates.size() > 1) {
            throw new SemanticException("Неоднозначное указание колонки: " + parserColRef.column);
        }

        return candidates.get(0);
    }

    private Expr analyzeParserAExpr(AExpr parserAExpr,
                                    Map<String, TableInfo> tableInfoMap) {
        AstNode left = parserAExpr.getLeft();
        AstNode right = parserAExpr.getRight();

        // Преобразуем левую часть
        Expr leftExpr;
        if (left instanceof ColumnRef) {
            leftExpr = resolveColumnRef(
                    (ColumnRef) left, tableInfoMap);
        } else if (left instanceof AConst) {
            leftExpr = new Const((( AConst) left).value);
        } else if (left instanceof Const) {
            leftExpr = (Expr) left;
        } else {
            throw new SemanticException("Неподдерживаемый тип выражения: " +
                    left.getClass().getSimpleName());
        }

        // Преобразуем правую часть
        Expr rightExpr;
        if (right instanceof ColumnRef) {
            rightExpr = resolveColumnRef(
                    (ColumnRef) right, tableInfoMap);
        } else if (right instanceof AConst) {
            rightExpr = new Const(((AConst) right).value);
        } else if (right instanceof Const) {
            rightExpr = (Expr) right;
        } else {
            throw new SemanticException("Неподдерживаемый тип выражения: " +
                    right.getClass().getSimpleName());
        }

        // Создаем AExpr
        return new AExpr(parserAExpr.getOp(), leftExpr, rightExpr);
    }

    private String getFullTableName(RangeVar rangeVar) {
        if (rangeVar.schemaname != null && !rangeVar.schemaname.trim().isEmpty()) {
            return rangeVar.schemaname + "." + rangeVar.relname;
        }
        return rangeVar.relname;
    }

    // Вспомогательный класс для хранения информации о таблицах
    private static class TableInfo {
        TableDefinition tableDef;
        int tableIndex;
        List<ColumnDefinition> columns;
    }

    // Остальные методы (analyzeInsert и analyzeCreateTable) остаются такими же,
    // как в предыдущем ответе, но без использования Expr.Column
    private QueryTree analyzeInsert(InsertStmt insert) {
        QueryTree queryTree = new QueryTree();
        queryTree.setCommandType(QueryType.INSERT);
        queryTree.setTableName(insert.tableName);

        // Проверяем существование таблицы
        String fullTableName = getFullTableName(insert);
        TableDefinition tableDef = catalog.getTable(fullTableName);

        if (tableDef == null) {
            throw new SemanticException("Таблица не найдена: " + fullTableName);
        }

        // Получаем колонки таблицы
        List<ColumnDefinition> tableColumns = catalog.getTableColumns(tableDef);

        if (insert.columns != null && !insert.columns.isEmpty()) {
            // INSERT с указанием колонок
            queryTree.setColumnNames(new ArrayList<>(insert.columns));

            // Проверяем, что все указанные колонки существуют
            for (String colName : insert.columns) {
                boolean found = false;
                for (ColumnDefinition colDef : tableColumns) {
                    if (colDef.getName().equals(colName)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new SemanticException("Колонка не найдена: " + colName);
                }
            }

            // Проверяем количество значений
            if (insert.values.size() != insert.columns.size()) {
                throw new SemanticException("Количество значений (" + insert.values.size() +
                        ") не совпадает с количеством колонок (" +
                        insert.columns.size() + ")");
            }

        } else {
            // INSERT без указания колонок
            List<String> columnNames = new ArrayList<>();
            for (ColumnDefinition colDef : tableColumns) {
                columnNames.add(colDef.getName());
            }
            queryTree.setColumnNames(columnNames);

            // Проверяем количество значений
            if (insert.values.size() != tableColumns.size()) {
                throw new SemanticException("Количество значений (" + insert.values.size() +
                        ") не совпадает с количеством колонок таблицы (" +
                        tableColumns.size() + ")");
            }
        }

        // Преобразуем значения
        List<Object> typedValues = new ArrayList<>();
        for (String value : insert.values) {
            // Простое преобразование
            if (isInteger(value)) {
                typedValues.add(Integer.parseInt(value));
            } else if (isDouble(value)) {
                typedValues.add(Double.parseDouble(value));
            } else if (isBoolean(value)) {
                typedValues.add(Boolean.parseBoolean(value));
            } else {
                // Убираем кавычки для строк
                String trimmed = value.trim();
                if (trimmed.startsWith("'") && trimmed.endsWith("'") ||
                        trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
                    typedValues.add(trimmed.substring(1, trimmed.length() - 1));
                } else {
                    typedValues.add(value);
                }
            }
        }

        queryTree.setValues(typedValues);
        return queryTree;
    }

    private String getFullTableName(InsertStmt insert) {
        if (insert.schemaName != null && !insert.schemaName.trim().isEmpty()) {
            return insert.schemaName + "." + insert.tableName;
        }
        return insert.tableName;
    }

    private boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isBoolean(String str) {
        return "true".equalsIgnoreCase(str) || "false".equalsIgnoreCase(str);
    }

    private QueryTree analyzeCreateTable(CreateTableStmt create) {
        QueryTree queryTree = new QueryTree();
        queryTree.setCommandType(QueryType.CREATE);
        queryTree.setTableName(create.tableName);

        // Проверяем, что таблица не существует
        String fullTableName = getFullTableName(create);
        if (catalog.getTable(fullTableName) != null) {
            throw new SemanticException("Таблица уже существует: " + fullTableName);
        }

        // Проверяем уникальность имен колонок
        Set<String> columnNames = new HashSet<>();
        for (ColumnDefinition col : create.columns) {
            if (columnNames.contains(col.getName())) {
                throw new SemanticException("Дублирующееся имя колонки: " + col.getName());
            }
            columnNames.add(col.getName());

            // Проверяем существование типа
            TypeDefinition typeDef = catalog.getType(col.getTypeOid());
            if (typeDef == null) {
                throw new SemanticException("Неизвестный OID типа: " + col.getTypeOid() +
                        " для колонки " + col.getName());
            }
        }

        // Сохраняем имена колонок
        List<String> colNames = new ArrayList<>();
        for (ColumnDefinition col : create.columns) {
            colNames.add(col.getName());
        }
        queryTree.setColumnNames(colNames);

        return queryTree;
    }

    private String getFullTableName(CreateTableStmt create) {
        if (create.schemaName != null && !create.schemaName.trim().isEmpty()) {
            return create.schemaName + "." + create.tableName;
        }
        return create.tableName;
    }
}
