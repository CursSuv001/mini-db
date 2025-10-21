package ru.open.cu.student.catalog.model;

import java.util.Objects;

public class ColumnDefinition {
    private final int oid;
    private final int tableOid;
    private final int typeOid;
    private final String name;
    private final int position;

    public ColumnDefinition(int oid, int tableOid, int typeOid, String name, int position) {
        this.oid = oid;
        this.tableOid = tableOid;
        this.typeOid = typeOid;
        this.name = Objects.requireNonNull(name, "name");
        this.position = position;
    }

    public ColumnDefinition(int typeOid, String name, int position) {
        oid = 0;
        tableOid = 0;
        this.typeOid = typeOid;
        this.name = Objects.requireNonNull(name, "name");
        this.position = position;
    }

    public int getOid() {
        return oid;
    }

    public int getTableOid() {
        return tableOid;
    }

    public int getTypeOid() {
        return typeOid;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }
}

