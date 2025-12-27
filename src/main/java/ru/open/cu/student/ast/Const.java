package ru.open.cu.student.ast;

public class Const extends Expr {
    private final Object value;

    public Const(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value) +
                (alias != null && !alias.trim().isEmpty() ? " AS " + alias : "");
    }
}