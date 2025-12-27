package ru.open.cu.student.parser;

import org.junit.jupiter.api.Test;
import ru.open.cu.student.ast.AExpr;
import ru.open.cu.student.ast.AstNode;
import ru.open.cu.student.ast.ColumnRef;
import ru.open.cu.student.lexer.DefaultLexer;
import ru.open.cu.student.lexer.Token;
import ru.open.cu.student.parser.nodes.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DefaultParserTest {

    private final DefaultLexer lexer = new DefaultLexer();
    private final DefaultParser parser = new DefaultParser();

    @Test
    void testParseSimpleSelect() {
        String sql = "SELECT name FROM users";
        List<Token> tokens = lexer.tokenize(sql);
        AstNode ast = parser.parse(tokens);

        assertTrue(ast instanceof SelectStmt);
        SelectStmt select = (SelectStmt) ast;

        assertEquals(1, select.targetList.size());
        assertEquals(1, select.fromClause.size());
        assertNull(select.whereClause);

        ResTarget target = select.targetList.get(0);
        assertTrue(target.val instanceof ColumnRef);
        assertEquals("name", ((ColumnRef) target.val).column);
    }

    @Test
    void testParseSelectWithWhere() {
        String sql = "SELECT id FROM users WHERE age > 18";
        List<Token> tokens = lexer.tokenize(sql);
        AstNode ast = parser.parse(tokens);

        assertTrue(ast instanceof SelectStmt);
        SelectStmt select = (SelectStmt) ast;

        assertNotNull(select.whereClause);
        assertTrue(select.whereClause instanceof AExpr);

        AExpr where = (AExpr) select.whereClause;
        assertEquals(">", where.getOp());
        assertTrue(where.getLeft() instanceof ColumnRef);
        assertTrue(where.getRight() instanceof ColumnRef);
    }

    @Test
    void testParseSelectMultipleColumns() {
        String sql = "SELECT id, name, age FROM users";
        List<Token> tokens = lexer.tokenize(sql);
        AstNode ast = parser.parse(tokens);

        assertTrue(ast instanceof SelectStmt);
        SelectStmt select = (SelectStmt) ast;

        assertEquals(3, select.targetList.size());
        assertEquals("id", ((ColumnRef) select.targetList.get(0).val).column);
        assertEquals("name", ((ColumnRef) select.targetList.get(1).val).column);
        assertEquals("age", ((ColumnRef) select.targetList.get(2).val).column);
    }

    @Test
    void testParseSelectStar() {
        String sql = "SELECT * FROM users";
        List<Token> tokens = lexer.tokenize(sql);
        AstNode ast = parser.parse(tokens);

        assertTrue(ast instanceof SelectStmt);
        SelectStmt select = (SelectStmt) ast;

        assertEquals(1, select.targetList.size());
        assertTrue(select.targetList.get(0).val instanceof ColumnRef);
        assertEquals("*", ((ColumnRef) select.targetList.get(0).val).column);
    }
}