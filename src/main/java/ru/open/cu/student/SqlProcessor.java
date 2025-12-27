package ru.open.cu.student;

import ru.open.cu.student.ast.*;
import ru.open.cu.student.catalog.manager.CatalogManager;
import ru.open.cu.student.lexer.DefaultLexer;
import ru.open.cu.student.lexer.Lexer;
import ru.open.cu.student.lexer.Token;
import ru.open.cu.student.parser.DefaultParser;
import ru.open.cu.student.parser.Parser;
import ru.open.cu.student.parser.nodes.*;
import ru.open.cu.student.catalog.model.ColumnDefinition;
import ru.open.cu.student.parser.nodes.CreateTableStmt;
import ru.open.cu.student.parser.nodes.SelectStmt;
import ru.open.cu.student.parser.nodes.InsertStmt;
import ru.open.cu.student.ast.AConst;
import ru.open.cu.student.ast.AExpr;


import java.util.List;

/**
 * SQL processor used in DI homeworks.
 *
 * HW5 pipeline expects {@link ru.open.cu.student.ast.QueryTree}.
 *
 * This class reuses lexer/parser implementation from HW4 and translates the parsed AST
 * into HW5 AST (ru.open.cu.student.ast.*) to keep planner/optimizer/executor logic intact.
 */
public class SqlProcessor {

    private final Lexer lexer;
    private final Parser parser;
    @SuppressWarnings("unused")
    private final CatalogManager catalogManager;

    public SqlProcessor(CatalogManager catalogManager) {
        this(new DefaultLexer(), new DefaultParser(), catalogManager);
    }

    public SqlProcessor(Lexer lexer, Parser parser, CatalogManager catalogManager) {
        this.lexer = lexer;
        this.parser = parser;
        this.catalogManager = catalogManager;
    }

    /**
     * Parse SQL string and build HW5 {@link QueryTree}.
     *
     * Supported grammar (minimal, as used by the course):
     *  - CREATE TABLE t (col TYPE, ...)
     *  - INSERT INTO t VALUES (v1, v2, ...)
     *  - SELECT col1, col2 FROM t [WHERE col OP const]
     *
     * @param sql query text
     * @return QueryTree for planner
     */
    public QueryTree process(String sql) {
        if (sql == null) throw new IllegalArgumentException("sql is null");

        List<Token> tokens = lexer.tokenize(sql);
        if (tokens.isEmpty()) throw new IllegalArgumentException("Empty SQL");

        String first = tokens.get(0).getType();

        return switch (first) {
            case "CREATE", "SELECT", "UPDATE", "INSERT" -> translateParsedAst(parser.parse(tokens));
            default -> throw new IllegalArgumentException("Unsupported statement: " + first);
        };

    }

    private QueryTree translateParsedAst(AstNode ast) {
        if (ast instanceof CreateTableStmt cs) {
            return translateCreate(cs);
        }
        if (ast instanceof SelectStmt ss) {
            return translateSelect(ss);
        }
        if (ast instanceof InsertStmt is) {
            return translateInsert(is);
        }

        throw new IllegalArgumentException("Unsupported AST node: " + ast.getClass().getSimpleName());
    }

    private QueryTree translateCreate(CreateTableStmt cs) {
        QueryTree q = new QueryTree();
        q.commandType = QueryType.CREATE;

        // Используем RangeVar вместо RangeTblEntry
        RangeVar rangeVar = new RangeVar(cs.schemaName, cs.tableName, null);
        q.rangeTable.add(rangeVar);

        // Преобразуем ColumnDefinition в TargetEntry
        for (ColumnDefinition col : cs.columns) {
            TargetEntry te = new TargetEntry(null, col.getName());
            te.resultType = String.valueOf(col.getTypeOid());
            q.targetList.add(te);
        }
        return q;
    }

    private QueryTree translateSelect(SelectStmt ss) {
        QueryTree q = new QueryTree();
        q.commandType = QueryType.SELECT;

        // FROM
        if (ss.fromClause != null && !ss.fromClause.isEmpty()) {
            for (RangeVar rv : ss.fromClause) {
                // Добавляем RangeVar напрямую, как в translateCreate
                q.rangeTable.add(rv);
            }
        } else {
            throw new IllegalArgumentException("SELECT without FROM is not supported");
        }

        // Target list
        for (ResTarget rt : ss.targetList) {
            // Создаем TargetEntry с val (выражение) и именем (псевдоним)
            Expr expr = translateExpr(rt.val);
            TargetEntry te = new TargetEntry(expr, rt.name);
            q.targetList.add(te);
        }

        // WHERE
        if (ss.whereClause != null) {
            // Преобразуем AstNode whereClause в Expr
            q.whereClause = translateExpr(ss.whereClause);
        }

        return q;
    }

    private QueryTree translateInsert(InsertStmt is) {
        QueryTree q = new QueryTree();
        q.commandType = QueryType.INSERT;

        // Используем RangeVar, как в translateCreate
        RangeVar rangeVar = new RangeVar(is.schemaName, is.tableName, null);
        q.rangeTable.add(rangeVar);

        // Преобразуем значения в AConst выражения
        for (String value : is.values) {
            // Создаем AConst для каждого значения
            AConst constExpr = new AConst(value);
            TargetEntry te = new TargetEntry(constExpr, null);
            q.targetList.add(te);
        }

        return q;
    }

    private Expr translateExpr(AstNode node) {
        if (node instanceof ColumnRef c) {
            // Получаем имя столбца из ColumnRef
            String colName = c.toString();
            String[] qc = splitQualifiedColumn(colName);
            return (qc[0] == null) ? new ColumnRef(qc[1]) : new ColumnRef(qc[0], qc[1]);
        }
        if (node instanceof AConst c) {
            return new AConst(c.getValue());
        }
        if (node instanceof AExpr e) {
            Expr left = translateExpr(e.getLeft());
            Expr right = translateExpr(e.getRight());
            return new AExpr(e.getOp(), left, right);
        }
        throw new IllegalArgumentException("Unsupported expression node: " + node.getClass().getSimpleName());
    }

    /**
     * Split qualified column name from HW4 parser ("col" or "tbl.col" or "*").
     * @return array [tableOrNull, column]
     */
    private String[] splitQualifiedColumn(String name) {
        if (name == null) return new String[]{null, null};
        String trimmed = name.trim();

        // Обработка случая "*"
        if (trimmed.equals("*")) {
            return new String[]{null, "*"};
        }

        int dot = trimmed.indexOf('.');
        if (dot >= 0) {
            String t = trimmed.substring(0, dot);
            String c = trimmed.substring(dot + 1);
            return new String[]{t, c};
        }
        return new String[]{null, trimmed};
    }
}