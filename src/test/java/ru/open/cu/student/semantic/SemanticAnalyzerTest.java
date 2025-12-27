package ru.open.cu.student.semantic;

import ru.open.cu.student.ast.*;
import ru.open.cu.student.catalog.manager.DefaultCatalogManager;
import ru.open.cu.student.catalog.model.ColumnDefinition;
import ru.open.cu.student.catalog.model.TableDefinition;
import ru.open.cu.student.parser.nodes.*;

import java.util.*;

public class SemanticAnalyzerTest {

    public static void main(String[] args) {
        System.out.println("=== Тестирование Семантического Анализатора ===\n");

        try {
            // Создаем каталог менеджер с базовыми типами
            DefaultCatalogManager catalog = new DefaultCatalogManager();

            // Добавляем тестовые типы в каталог (если их нет)
            initializeTestTypes(catalog);

            // Создаем семантический анализатор
            SemanticAnalyzer analyzer = new DefaultSemanticAnalyzer();

            // Тест 1: CREATE TABLE
            testCreateTable(analyzer, catalog);

            // Тест 2: INSERT
            testInsert(analyzer, catalog);

            // Тест 3: SELECT с простым условием
            testSelectSimple(analyzer, catalog);

            // Тест 4: SELECT с JOIN
            testSelectWithJoin(analyzer, catalog);

            // Тест 5: SELECT с алиасами
            testSelectWithAliases(analyzer, catalog);

            // Тест 6: Ошибки - несуществующая таблица
            testErrors(analyzer, catalog);

            System.out.println("\n=== Все тесты пройдены успешно! ===");

        } catch (Exception e) {
            System.err.println("\nОшибка при выполнении тестов: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void initializeTestTypes(DefaultCatalogManager catalog) {
        // Создаем базовые типы, если их нет в каталоге
        try {
            // Проверяем существование типа integer
            if (catalog.getType("integer") == null) {
                // В реальной системе типы должны создаваться по-другому
                // Это просто для тестирования
                System.out.println("Инициализация тестовых типов...");
            }
        } catch (Exception e) {
            // Игнорируем - в тестовом режиме
        }
    }

    private static void testCreateTable(SemanticAnalyzer analyzer, DefaultCatalogManager catalog) {
        System.out.println("Тест 1: CREATE TABLE");

        // Создаем определение таблицы
        List<ColumnDefinition> columns = Arrays.asList(
                new ColumnDefinition(1, "id", 0),        // integer
                new ColumnDefinition(2, "name", 1),      // varchar
                new ColumnDefinition(3, "age", 2),       // integer
                new ColumnDefinition(4, "email", 3)      // varchar
        );

        CreateTableStmt create = new CreateTableStmt(
                "public",
                "users",
                columns
        );

        System.out.println("  SQL: " + create);

        try {
            QueryTree queryTree = analyzer.analyze(create, catalog);
            System.out.println("  Result: " + queryTree);
            System.out.println("  Command Type: " + queryTree.getCommandType());
            System.out.println("  Table Name: " + queryTree.getTableName());
            System.out.println("  Columns: " + queryTree.getColumnNames());
            System.out.println("  ✓ CREATE TABLE проанализирован успешно\n");

        } catch (Exception e) {
            System.err.println("  ✗ Ошибка: " + e.getMessage());
            throw e;
        }
    }

    private static void testInsert(SemanticAnalyzer analyzer, DefaultCatalogManager catalog) {
        System.out.println("Тест 2: INSERT");

        // Сначала создаем таблицу в каталоге (для тестирования)
        try {
            List<ColumnDefinition> columns = Arrays.asList(
                    new ColumnDefinition(1, "id", 0),
                    new ColumnDefinition(2, "name", 1),
                    new ColumnDefinition(3, "age", 2)
            );

            // Создаем таблицу в каталоге (симуляция)
            TableDefinition tableDef = catalog.createTable("users", columns);
            System.out.println("  Создана тестовая таблица: " + tableDef.getName());

        } catch (Exception e) {
            // Таблица уже существует - это нормально
        }

        // Тестируем INSERT
        InsertStmt insert = new InsertStmt(
                "public",
                "users",
                Arrays.asList("id", "name", "age"),
                Arrays.asList("1", "'John Doe'", "25")
        );

        System.out.println("  SQL: " + insert);

        try {
            QueryTree queryTree = analyzer.analyze(insert, catalog);
            System.out.println("  Result: " + queryTree);
            System.out.println("  Command Type: " + queryTree.getCommandType());
            System.out.println("  Table Name: " + queryTree.getTableName());
            System.out.println("  Columns: " + queryTree.getColumnNames());
            System.out.println("  Values: " + queryTree.getValues());
            System.out.println("  ✓ INSERT проанализирован успешно\n");

        } catch (Exception e) {
            System.err.println("  ✗ Ошибка: " + e.getMessage());
            throw e;
        }
    }

    private static void testSelectSimple(SemanticAnalyzer analyzer, DefaultCatalogManager catalog) {
        System.out.println("Тест 3: SELECT с простым условием");

        SelectStmt select = new SelectStmt(
                Arrays.asList(
                        new ResTarget(new ColumnRef(null, "id"), "user_id"),
                        new ResTarget(new ColumnRef(null, "name"), "user_name")
                ),
                Arrays.asList(new RangeVar("public", "users", null)),
                new AExpr(
                        ">",
                        new ColumnRef(null, "age"),
                        new AConst("18")
                )
        );

        System.out.println("  SQL: " + select);

        try {
            QueryTree queryTree = analyzer.analyze(select, catalog);
            System.out.println("  Result: " + queryTree);
            System.out.println("  Command Type: " + queryTree.getCommandType());
            System.out.println("  Range Table: " + queryTree.getRangeTable());
            System.out.println("  Target List: " + queryTree.getTargetList());
            System.out.println("  Where Clause: " + queryTree.getWhereClause());
            System.out.println("  ✓ SELECT проанализирован успешно\n");

        } catch (Exception e) {
            System.err.println("  ✗ Ошибка: " + e.getMessage());
            throw e;
        }
    }

    private static void testSelectWithJoin(SemanticAnalyzer analyzer, DefaultCatalogManager catalog) {
        System.out.println("Тест 4: SELECT с JOIN (несколько таблиц)");

        // Создаем вторую таблицу для JOIN
        try {
            List<ColumnDefinition> ordersColumns = Arrays.asList(
                    new ColumnDefinition(1, "order_id", 0),
                    new ColumnDefinition(2, "user_id", 1),
                    new ColumnDefinition(3, "amount", 2)
            );

            catalog.createTable("orders", ordersColumns);
            System.out.println("  Создана тестовая таблица: orders");

        } catch (Exception e) {
            // Таблица уже существует
        }

        SelectStmt select = new SelectStmt(
                Arrays.asList(
                        new ResTarget(new ColumnRef("u", "id"), "user_id"),
                        new ResTarget(new ColumnRef("u", "name"), "user_name"),
                        new ResTarget(new ColumnRef("o", "amount"), "order_amount")
                ),
                Arrays.asList(
                        new RangeVar("public", "users", "u"),
                        new RangeVar("public", "orders", "o")
                ),
                new AExpr(
                        "=",
                        new ColumnRef("u", "id"),
                        new ColumnRef("o", "user_id")
                )
        );

        System.out.println("  SQL: " + select);

        try {
            QueryTree queryTree = analyzer.analyze(select, catalog);
            System.out.println("  Result: " + queryTree);
            System.out.println("  Command Type: " + queryTree.getCommandType());
            System.out.println("  Range Table: " + queryTree.getRangeTable());
            System.out.println("  Target List: " + queryTree.getTargetList());
            System.out.println("  Where Clause: " + queryTree.getWhereClause());
            System.out.println("  ✓ SELECT с JOIN проанализирован успешно\n");

        } catch (Exception e) {
            System.err.println("  ✗ Ошибка: " + e.getMessage());
            throw e;
        }
    }

    private static void testSelectWithAliases(SemanticAnalyzer analyzer, DefaultCatalogManager catalog) {
        System.out.println("Тест 5: SELECT с алиасами таблиц");

        SelectStmt select = new SelectStmt(
                Arrays.asList(
                        new ResTarget(new ColumnRef("u", "id"), null),
                        new ResTarget(new ColumnRef(null, "name"), "full_name")
                ),
                Arrays.asList(new RangeVar("public", "users", "u")),
                null
        );

        System.out.println("  SQL: " + select);

        try {
            QueryTree queryTree = analyzer.analyze(select, catalog);
            System.out.println("  Result: " + queryTree);
            System.out.println("  Command Type: " + queryTree.getCommandType());
            System.out.println("  Target List: " + queryTree.getTargetList());

            // Проверяем, что ColumnRef имеют правильные индексы
            for (TargetEntry target : queryTree.getTargetList()) {
                if (target.expr instanceof ColumnRef) {
                    ColumnRef colRef = (ColumnRef) target.expr;
                    System.out.println("    Column: " + colRef.table + "." + colRef.column +
                            " [tableIndex=" + colRef.tableIndex +
                            ", columnIndex=" + colRef.columnIndex + "]");
                }
            }

            System.out.println("  ✓ SELECT с алиасами проанализирован успешно\n");

        } catch (Exception e) {
            System.err.println("  ✗ Ошибка: " + e.getMessage());
            throw e;
        }
    }

    private static void testErrors(SemanticAnalyzer analyzer, DefaultCatalogManager catalog) {
        System.out.println("Тест 6: Проверка обработки ошибок");

        // Тест 6.1: Несуществующая таблица
        System.out.println("  6.1: SELECT из несуществующей таблицы");
        SelectStmt badSelect = new SelectStmt(
                Arrays.asList(new ResTarget(new ColumnRef(null, "id"), null)),
                Arrays.asList(new RangeVar("public", "nonexistent_table", null)),
                null
        );

        try {
            analyzer.analyze(badSelect, catalog);
            System.err.println("  ✗ Ошибка: Должно было быть исключение для несуществующей таблицы");
            throw new RuntimeException("Тест не пройден");
        } catch (ru.open.cu.student.semantic.SemanticException e) {
            System.out.println("  ✓ Корректно обработана ошибка: " + e.getMessage());
        }

        // Тест 6.2: Несуществующая колонка
        System.out.println("\n  6.2: SELECT несуществующей колонки");
        SelectStmt badColumnSelect = new SelectStmt(
                Arrays.asList(new ResTarget(new ColumnRef(null, "nonexistent_column"), null)),
                Arrays.asList(new RangeVar("public", "users", null)),
                null
        );

        try {
            analyzer.analyze(badColumnSelect, catalog);
            System.err.println("  ✗ Ошибка: Должно было быть исключение для несуществующей колонки");
            throw new RuntimeException("Тест не пройден");
        } catch (ru.open.cu.student.semantic.SemanticException e) {
            System.out.println("  ✓ Корректно обработана ошибка: " + e.getMessage());
        }

        // Тест 6.3: Дублирование имен колонок в CREATE TABLE
        System.out.println("\n  6.3: CREATE TABLE с дублирующимися именами колонок");
        List<ColumnDefinition> duplicateColumns = Arrays.asList(
                new ColumnDefinition(1, "id", 0),
                new ColumnDefinition(2, "id", 1)  // Дублирование!
        );

        CreateTableStmt badCreate = new CreateTableStmt(
                "public",
                "test_table",
                duplicateColumns
        );

        try {
            analyzer.analyze(badCreate, catalog);
            System.err.println("  ✗ Ошибка: Должно было быть исключение для дублирующихся колонок");
            throw new RuntimeException("Тест не пройден");
        } catch (ru.open.cu.student.semantic.SemanticException e) {
            System.out.println("  ✓ Корректно обработана ошибка: " + e.getMessage());
        }

        // Тест 6.4: Неоднозначное указание колонки
        System.out.println("\n  6.4: Неоднозначное указание колонки (есть в обеих таблицах)");

        // Создаем таблицу с такой же колонкой 'id'
        try {
            List<ColumnDefinition> columns = Arrays.asList(
                    new ColumnDefinition(1, "id", 0),
                    new ColumnDefinition(2, "other_field", 1)
            );
            catalog.createTable("other_table", columns);

        } catch (Exception e) {
            // Игнорируем если уже существует
        }

        SelectStmt ambiguousSelect = new SelectStmt(
                Arrays.asList(new ResTarget(new ColumnRef(null, "id"), null)),
                Arrays.asList(
                        new RangeVar("public", "users", null),
                        new RangeVar("public", "other_table", null)
                ),
                null
        );

        try {
            analyzer.analyze(ambiguousSelect, catalog);
            System.err.println("  ✗ Ошибка: Должно было быть исключение для неоднозначной колонки");
            throw new RuntimeException("Тест не пройден");
        } catch (ru.open.cu.student.semantic.SemanticException e) {
            System.out.println("  ✓ Корректно обработана ошибка: " + e.getMessage());
        }

        System.out.println("\n  ✓ Все тесты обработки ошибок пройдены успешно\n");
    }
}