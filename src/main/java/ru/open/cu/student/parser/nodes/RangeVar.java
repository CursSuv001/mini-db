package ru.open.cu.student.parser.nodes;

import ru.open.cu.student.ast.AstNode;

public class RangeVar extends AstNode {
    public String schemaname;  // схема (может быть null)
    public String relname;     // имя таблицы
    public String alias;       // псевдоним (может быть null)

    public RangeVar(String schema, String name, String alias) {
        this.schemaname = schema;
        this.relname = name;
        this.alias = alias;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        if (schemaname != null && schemaname.trim().isEmpty()) sb.append(schemaname).append(".");

        sb.append(relname);

        if (alias != null && alias.trim().isEmpty()) sb.append(" AS ").append(alias);

        return sb.toString();
    }
}