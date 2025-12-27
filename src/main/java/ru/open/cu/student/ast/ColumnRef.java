package ru.open.cu.student.ast;

public class ColumnRef extends Expr {
    public String table;       // имя таблицы (может быть null)
    public String column;      // имя столбца
    public int tableIndex = -1;    // индекс таблицы в rangeTable
    public int columnIndex = -1;   // индекс колонки в таблице

    public ColumnRef(String table, String column) {
        this.table = table;
        this.column = column;
    }

    public ColumnRef(String column) {
        this(null, column);
    }

    public ColumnRef(String table, String column, int tableIndex, int columnIndex) {
        this.table = table;
        this.column = column;
        this.tableIndex = tableIndex;
        this.columnIndex = columnIndex;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (table != null && !table.trim().isEmpty()) {
            sb.append(table).append(".");
        }
        sb.append(column);
        if (alias != null && !alias.trim().isEmpty()) {
            sb.append(" AS ").append(alias);
        }
        return sb.toString();
    }
}