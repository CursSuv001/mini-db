package ru.open.cu.student.lexer;

import java.util.Objects;

public class Token {
    private final String type;
    private final String value;

    public Token(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public String getType() { return type; }
    public String getValue() { return value; }

    @Override
    public String toString() {
        if ("IDENT".equals(type) || "NUMBER".equals(type)) {
            return type + "(" + value + ")";
        }
        return type;
    }
    //не пон что здесь
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Token token = (Token) obj;
        return type.equals(token.type) && value.equals(token.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }
}