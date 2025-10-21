package ru.open.cu.student.catalog.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TableDefinition {
    private final int oid;
    private final String name;
    private final String type;
    private final String fileNode;
    private int pagesCount;
    private final List<ColumnDefinition> columns = new ArrayList<>();

    public TableDefinition(int oid, String name, String type, String fileNode, int pagesCount) {
        this.oid = oid;
        this.name = Objects.requireNonNull(name, "name");
        this.type = Objects.requireNonNull(type, "type");
        this.fileNode = Objects.requireNonNull(fileNode, "fileNode");
        this.pagesCount = pagesCount;
    }

    private TableDefinition(int oid, String name, String type, String fileNode, int pagesCount, List<ColumnDefinition> columns) {
        this(oid, name, type, fileNode, pagesCount);
        this.columns.addAll(columns);
    }

    public int getOid() {
        return oid;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getFileNode() {
        return fileNode;
    }

    public int getPagesCount() {
        return pagesCount;
    }

    public void setPagesCount(int pagesCount) {
        this.pagesCount = pagesCount;
    }

    public List<ColumnDefinition> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    // Заменяем список колонок, сохраняя неизменяемый вид наружу.
    public void setColumns(List<ColumnDefinition> columnDefinitions) {
        columns.clear();
        columns.addAll(columnDefinitions);
    }
}
