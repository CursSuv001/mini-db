package ru.open.cu.student.ast;

public abstract class Expr extends AstNode {
    // Можем добавить общие поля/методы
    protected String alias;

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }
}