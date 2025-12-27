package ru.open.cu.student.parser.nodes;

import ru.open.cu.student.ast.AstNode;

import java.util.List;

public class SelectStmt extends AstNode {

    public List<ResTarget> targetList;     // что выбираем
    public List<RangeVar> fromClause;      // откуда выбираем
    public AstNode whereClause;               // условие (может быть null)

    public SelectStmt(List<ResTarget> targets, List<RangeVar> from, AstNode where) {
        this.targetList = targets;
        this.fromClause = from;
        this.whereClause = where;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT ");
        for (int i = 0; i < targetList.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(targetList.get(i));
        }

        if (fromClause != null && !fromClause.isEmpty()) {
            sb.append(" FROM ");
            for (int i = 0; i < fromClause.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(fromClause.get(i));
            }
        }

        if (whereClause != null) {
            sb.append(" WHERE ").append(whereClause);
        }

        return sb.toString();
    }

}