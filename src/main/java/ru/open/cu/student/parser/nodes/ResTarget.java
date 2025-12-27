package ru.open.cu.student.parser.nodes;

import ru.open.cu.student.ast.AstNode;
import ru.open.cu.student.ast.ColumnRef;

public class ResTarget extends AstNode {
    public AstNode val;           // выражение
    public String name;        // псевдоним (может быть null)

    public ResTarget(ColumnRef expr, String alias) {
        this.val = expr;
        this.name = alias;
    }

    @Override
    public String toString() {
        if (name != null) return val.toString() + " AS " + name;
        return val.toString();
    }
}