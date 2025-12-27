package ru.open.cu.student.parser.nodes;

import ru.open.cu.student.ast.AstNode;

import java.util.List;

public class InsertStmt extends AstNode {
    public String schemaName;
    public String tableName;
    public List<String> columns;
    public List<String> values;

    public InsertStmt(String schemaName, String tableName,
                      List<String> columns, List<String> values) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columns = columns;
        this.values = values;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        if (schemaName != null) sb.append(schemaName).append(".");
        sb.append(tableName);

        if (columns != null && !columns.isEmpty()) {
            sb.append(" (").append(String.join(", ", columns)).append(")");
        }

        sb.append(" VALUES (");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(values.get(i));
        }
        sb.append(")");

        return sb.toString();
    }
}