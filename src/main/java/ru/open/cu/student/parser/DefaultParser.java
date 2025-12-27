package ru.open.cu.student.parser;

import ru.open.cu.student.ast.AExpr;
import ru.open.cu.student.ast.AstNode;
import ru.open.cu.student.ast.ColumnRef;
import ru.open.cu.student.catalog.model.ColumnDefinition;
import ru.open.cu.student.lexer.Token;
import ru.open.cu.student.parser.nodes.*;

import java.util.ArrayList;
import java.util.List;

public class DefaultParser implements Parser{
    private List<Token> tokens;
    private int curPosition;

    @Override
    public AstNode parse(List<Token> tokens) {
        this.tokens = tokens;
        this.curPosition = 0;

        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("Empty token list");
        }

        String firstToken = currentToken().getType();

        return switch (firstToken) {
            case "SELECT" -> parseSelect();
            case "CREATE" -> parseCreate();
            case "INSERT" -> parseInsert();
            default -> throw new IllegalArgumentException("Unsupported SQL statement: " + firstToken);
        };
    }

    private AstNode parseSelect() {
        match("SELECT");

        List<ResTarget> targetList = parseTargetList();

        match("FROM");

        List<RangeVar> fromClause = parseFromClause();

        AstNode whereClause = null;
        if (curPosition < tokens.size() && currentToken().getType().equals("WHERE")) {
            match("WHERE");
            whereClause = parseWhereClause();
        }

        return new SelectStmt(targetList, fromClause, whereClause);
    }

    private AstNode parseCreate() {
        match("CREATE");
        match("TABLE");

        // Получаем имя таблицы (возможно с schema)
        String tableName;
        String schemaName = null;

        Token tableToken = expectToken("IDENT");
        if (curPosition < tokens.size() && currentToken().getType().equals("DOT")) {
            // Есть схема: schema.table
            schemaName = tableToken.getValue();
            match("DOT");
            tableToken = expectToken("IDENT");
            tableName = tableToken.getValue();
        } else {
            tableName = tableToken.getValue();
        }

        match("LPAREN");

        List<ColumnDefinition> columns = parseColumnDefinitions();

        match("RPAREN");

        return new CreateTableStmt(schemaName, tableName, columns);
    }

    private List<ColumnDefinition> parseColumnDefinitions() {
        List<ColumnDefinition> columns = new ArrayList<>();
        int position = 0;

        while (curPosition < tokens.size()) {
            Token columnNameToken = expectToken("IDENT");
            String columnName = columnNameToken.getValue();

            Token typeToken = expectToken("IDENT");
            String typeName = typeToken.getValue();

            // Преобразуем имя типа в typeOid (можно сделать маппинг)
            int typeOid = mapTypeNameToOid(typeName);

            columns.add(new ColumnDefinition(typeOid, columnName, position++));

            // Проверяем, есть ли следующая колонка
            if (curPosition < tokens.size() && currentToken().getType().equals("COMMA")) {
                match("COMMA");
            } else {
                break;
            }
        }

        return columns;
    }

    private int mapTypeNameToOid(String typeName) {
        // Простой маппинг типов на OID
        return switch (typeName.toUpperCase()) {
            case "INTEGER", "INT" -> 23;    // Пример OID для integer
            case "BIGINT" -> 20;            // Пример OID для bigint
            case "VARCHAR", "TEXT" -> 25;   // Пример OID для text
            case "BOOLEAN", "BOOL" -> 16;   // Пример OID для boolean
            default -> 25;                  // По умолчанию text
        };
    }

    private AstNode parseInsert() {
        match("INSERT");
        match("INTO");

        // Получаем имя таблицы
        String tableName;
        String schemaName = null;

        Token tableToken = expectToken("IDENT");
        if (curPosition < tokens.size() && currentToken().getType().equals("DOT")) {
            schemaName = tableToken.getValue();
            match("DOT");
            tableToken = expectToken("IDENT");
            tableName = tableToken.getValue();
        } else {
            tableName = tableToken.getValue();
        }

        // Опциональный список колонок
        List<String> columns = null;
        if (curPosition < tokens.size() && currentToken().getType().equals("LPAREN")) {
            match("LPAREN");
            columns = parseColumnNames();
            match("RPAREN");
        }

        match("VALUES");
        match("LPAREN");

        List<String> values = parseValues();

        match("RPAREN");

        return new InsertStmt(schemaName, tableName, columns, values);
    }

    private List<String> parseColumnNames() {
        List<String> columns = new ArrayList<>();

        while (curPosition < tokens.size()) {
            Token columnToken = expectToken("IDENT");
            columns.add(columnToken.getValue());

            if (curPosition < tokens.size() && currentToken().getType().equals("COMMA")) {
                match("COMMA");
            } else {
                break;
            }
        }

        return columns;
    }

    private List<String> parseValues() {
        List<String> values = new ArrayList<>();

        while (curPosition < tokens.size()) {
            Token valueToken = currentToken();
            String value;

            if (valueToken.getType().equals("STRING")) {
                value = valueToken.getValue();
            } else if (valueToken.getType().equals("NUMBER")) {
                value = valueToken.getValue();
            } else if (valueToken.getType().equals("IDENT") &&
                    (valueToken.getValue().equalsIgnoreCase("NULL") ||
                            valueToken.getValue().equalsIgnoreCase("TRUE") ||
                            valueToken.getValue().equalsIgnoreCase("FALSE"))) {
                value = valueToken.getValue();
            } else {
                throw new IllegalArgumentException("Unexpected value token: " + valueToken);
            }

            values.add(value);
            curPosition++;

            if (curPosition < tokens.size() && currentToken().getType().equals("COMMA")) {
                match("COMMA");
            } else {
                break;
            }
        }

        return values;
    }

    // Вспомогательные методы для токенов (должны быть в вашем классе)
    private Token expectToken(String expectedType) {
        if (curPosition >= tokens.size()) {
            throw new IllegalArgumentException("Expected token " + expectedType + " but reached end of input");
        }
        Token token = tokens.get(curPosition);
        if (!token.getType().equals(expectedType)) {
            throw new IllegalArgumentException("Expected token " + expectedType +
                    " but got " + token.getType() + " (" + token.getValue() + ")");
        }
        curPosition++;
        return token;
    }





    private List<ResTarget> parseTargetList() {
        List<ResTarget> targets = new ArrayList<>();

        targets.add(parseResTarget());

        while (currentToken().getType().equals("COMMA")) {
            match("COMMA");
            targets.add(parseResTarget());
        }

        return targets;
    }

    private ResTarget parseResTarget() {
        Token token = currentToken();

        if (token.getType().equals("ASTERISK")) {
            match("ASTERISK");
            ColumnRef columnRef = new ColumnRef("*");
            return new ResTarget(columnRef, null);
        } else if (token.getType().equals("IDENT")) {
            match("IDENT");
            ColumnRef columnRef = new ColumnRef(token.getValue());
            return new ResTarget(columnRef, null);
        } else {
            throw new RuntimeException("Ожидался идентификатор колонки или *: " + token);
        }
    }

    private List<RangeVar> parseFromClause() {
        List<RangeVar> tables = new ArrayList<>();

        tables.add(parseRangeVar());

        while (currentToken().getType().equals("COMMA")) {
            match("COMMA");
            tables.add(parseRangeVar());
        }
        return tables;
    }

    private AstNode parseWhereClause() {
        // Левый операнд
        Token left = match("IDENT");
        ColumnRef leftRef = new ColumnRef(left.getValue());

        // Оператор
        String operator = parseOperator();

        // Правый операнд
        AstNode right = parseExpression();

        return new AExpr(operator, leftRef, right);
    }

    private AstNode parseExpression() {
        Token token = currentToken();

        if (token.getType().equals("NUMBER")) {
            match("NUMBER");
            // Для простоты используем ColumnRef для чисел
            return new ColumnRef(null, token.getValue());
        }
        else if (token.getType().equals("IDENT")) {
            match("IDENT");
            return new ColumnRef(token.getValue());
        }
        else {
            throw new RuntimeException("Ожидалось выражение: " + token);
        }
    }

    private String parseOperator() {
        Token token = currentToken();
        switch (token.getType()) {
            case "GT": match("GT"); return ">";
            case "LT": match("LT"); return "<";
            case "EQ": match("EQ"); return "=";
            case "NEQ": match("NEQ"); return "!=";
            default: throw new RuntimeException("Неизвестный оператор: " + token);
        }
    }

    private RangeVar parseRangeVar() {
        Token tableToken = match("IDENT");
        return new RangeVar(null, tableToken.getValue(), null);
    }

    //функция проверки текущего элемента
    private Token currentToken() {
        if (curPosition >= tokens.size()) return new Token("EOF", "");
        return tokens.get(curPosition);
    }

    private Token match(String expectedType) {
        Token token = currentToken();
        if (token.getType().equals(expectedType)) {
            curPosition++;
            return token;
        }
        throw new RuntimeException("Ожидался токен: " + expectedType + " , но получен токен:  " + token.getType());
    }
}