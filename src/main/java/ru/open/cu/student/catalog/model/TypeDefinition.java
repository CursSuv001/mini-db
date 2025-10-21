package ru.open.cu.student.catalog.model;

import java.util.Objects;

public class TypeDefinition {
    private final int oid;
    private final String name;
    private final int byteLength;

    public TypeDefinition(int oid, String name, int byteLength) {
        this.oid = oid;
        this.name = Objects.requireNonNull(name, "name");
        this.byteLength = byteLength;
    }

    public int getOid() {
        return oid;
    }

    public String getName() {
        return name;
    }

    public int getByteLength() {
        return byteLength;
    }

}

