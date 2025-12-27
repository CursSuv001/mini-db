package ru.open.cu.student.parser.nodes;

import ru.open.cu.student.ast.AstNode;
import ru.open.cu.student.catalog.model.ColumnDefinition;
import java.util.List;

public class CreateTableStmt extends AstNode {
    public String schemaName;
    public String tableName;
    public List<ColumnDefinition> columns;

    public CreateTableStmt(String schemaName, String tableName,
                           List<ColumnDefinition> columns) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columns = columns;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ");
        if (schemaName != null) sb.append(schemaName).append(".");
        sb.append(tableName).append(" (");

        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(columns.get(i).getName())
                    .append(" ")
                    .append("type_oid_")  // Префикс для ясности
                    .append(columns.get(i).getTypeOid());
        }
        sb.append(")");

        return sb.toString();
    }
}