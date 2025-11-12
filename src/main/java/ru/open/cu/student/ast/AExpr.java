package ru.open.cu.student.ast;

import ru.open.cu.student.ast.AstNode;

public class AExpr extends Expr {
    private final String op;          // оператор: "=", ">", "+", etc.
    private final AstNode left;          // левый операнд
    private final AstNode right;         // правый операнд

    public String getOp() {
        return op;
    }

    public AstNode getRight() {
        return right;
    }

    public AExpr(String operator, AstNode leftExpr, AstNode rightExpr) {
        this.op = operator;
        this.left = leftExpr;
        this.right = rightExpr;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (left instanceof AExpr) {
            sb.append("(").append(left).append(")");
        } else {
            sb.append(left);
        }

        sb.append(" ").append(op).append(" ");

        if (right instanceof AExpr) {
            sb.append("(").append(right).append(")");
        } else {
            sb.append(right);
        }
        return sb.toString();
    }
}
