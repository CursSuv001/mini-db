package ru.open.cu.student.lexer;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DefaultLexerTest {

    private final DefaultLexer lexer = new DefaultLexer();

    @Test
    void testTokenizeSelectQuery() {
        String sql = "SELECT name, age FROM users WHERE age > 25";
        List<Token> tokens = lexer.tokenize(sql);

        assertEquals(10, tokens.size());
        assertEquals("SELECT", tokens.get(0).getType());
        assertEquals("IDENT", tokens.get(1).getType());
        assertEquals("name", tokens.get(1).getValue());
        assertEquals("COMMA", tokens.get(2).getType());
        assertEquals("IDENT", tokens.get(3).getType());
        assertEquals("age", tokens.get(3).getValue());
        assertEquals("FROM", tokens.get(4).getType());
        assertEquals("IDENT", tokens.get(5).getType());
        assertEquals("users", tokens.get(5).getValue());
        assertEquals("WHERE", tokens.get(6).getType());
        assertEquals("IDENT", tokens.get(7).getType());
        assertEquals("age", tokens.get(7).getValue());
        assertEquals("GT", tokens.get(8).getType());
        assertEquals("NUMBER", tokens.get(9).getType());
        assertEquals("25", tokens.get(9).getValue());
    }

    @Test
    void testTokenizeOperators() {
        String sql = "= > < != *";
        List<Token> tokens = lexer.tokenize(sql);

        assertEquals(5, tokens.size());
        assertEquals("EQ", tokens.get(0).getType());
        assertEquals("GT", tokens.get(1).getType());
        assertEquals("LT", tokens.get(2).getType());
        assertEquals("NEQ", tokens.get(3).getType());
        assertEquals("ASTERISK", tokens.get(4).getType());
    }

    @Test
    void testTokenizeNumbers() {
        String sql = "123 456";
        List<Token> tokens = lexer.tokenize(sql);

        assertEquals(2, tokens.size());
        assertEquals("NUMBER", tokens.get(0).getType());
        assertEquals("123", tokens.get(0).getValue());
        assertEquals("NUMBER", tokens.get(1).getType());
        assertEquals("456", tokens.get(1).getValue());
    }
}