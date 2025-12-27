package ru.open.cu.student.parser;

import ru.open.cu.student.ast.AExpr;
import ru.open.cu.student.ast.AstNode;
import ru.open.cu.student.ast.ColumnRef;
import ru.open.cu.student.parser.nodes.*;
import java.util.ArrayList;
import java.util.List;

public class AstTree {

    public static void printAst(AstNode node) {
        printAst(node, "", true);
    }

    private static void printAst(AstNode node, String prefix, boolean isTail) {
        if (node instanceof SelectStmt) {
            printSelectStmt((SelectStmt) node, prefix, isTail);
        } else {
            System.out.println(prefix + (isTail ? "└── " : "├── ") + formatNode(node));
        }
    }

    private static void printSelectStmt(SelectStmt select, String prefix, boolean isTail) {
        System.out.println(prefix + (isTail ? "└── " : "├── ") + "SelectStmt");

        String childPrefix = prefix + (isTail ? "    " : "│   ");

        // targetList
        System.out.println(childPrefix + "├── targetList: " + formatTargetList(select.targetList));

        // fromClause
        System.out.println(childPrefix + "├── fromClause: " + formatFromClause(select.fromClause));

        // whereClause
        if (select.whereClause != null) {
            System.out.println(childPrefix + "└── whereClause: " + formatNode(select.whereClause));
        } else {
            System.out.println(childPrefix + "└── whereClause: null");
        }
    }

    private static String formatTargetList(List<ResTarget> targets) {
        List<String> formatted = new ArrayList<>();
        for (ResTarget target : targets) {
            formatted.add(formatNode(target));
        }
        return "[" + String.join(", ", formatted) + "]";
    }

    private static String formatFromClause(List<RangeVar> fromClause) {
        List<String> formatted = new ArrayList<>();
        for (RangeVar rangeVar : fromClause) {
            formatted.add(formatNode(rangeVar));
        }
        return "[" + String.join(", ", formatted) + "]";
    }

    private static String formatNode(AstNode node) {
        if (node instanceof ResTarget) {
            ResTarget target = (ResTarget) node;
            return "ResTarget(" + formatNode(target.val) +
                    (target.name != null ? ", \"" + target.name + "\"" : "") + ")";
        }
        else if (node instanceof ColumnRef) {
            ColumnRef ref = (ColumnRef) node;
            if (ref.table != null) {
                return "ColumnRef(\"" + ref.table + "\", \"" + ref.column + "\")";
            } else {
                return "ColumnRef(\"" + ref.column + "\")";
            }
        }
        else if (node instanceof RangeVar) {
            RangeVar var = (RangeVar) node;
            StringBuilder sb = new StringBuilder("RangeVar(");
            sb.append("\"").append(var.relname).append("\"");
            if (var.schemaname != null) {
                sb.append(", schema: \"").append(var.schemaname).append("\"");
            }
            if (var.alias != null) {
                sb.append(", alias: \"").append(var.alias).append("\"");
            }
            sb.append(")");
            return sb.toString();
        }
        else if (node instanceof AExpr) {
            AExpr expr = (AExpr) node;
            return "AExpr(\"" + expr.getOp() + "\", " +
                    formatNode(expr.getLeft()) + ", " +
                    formatNode(expr.getRight()) + ")";
        }
        else {
            return node.getClass().getSimpleName();
        }
    }
}