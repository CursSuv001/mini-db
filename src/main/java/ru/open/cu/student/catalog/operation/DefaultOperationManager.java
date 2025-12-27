package ru.open.cu.student.catalog.operation;

import ru.open.cu.student.catalog.manager.CatalogManager;
import ru.open.cu.student.catalog.model.TableDefinition;
import ru.open.cu.student.catalog.model.ColumnDefinition;
import ru.open.cu.student.catalog.model.TypeDefinition;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DefaultOperationManager implements OperationManager {
    private static final int PAGE_SIZE = 8192;
    private final CatalogManager catalogManager;

    public DefaultOperationManager(CatalogManager catalogManager) {
        this.catalogManager = catalogManager;
    }

    @Override
    public void insert(String tableName, List<Object> values) {
        TableDefinition table = catalogManager.getTable(tableName);
        if (table == null) {
            throw new IllegalArgumentException("Table not found: " + tableName);
        }

        List<ColumnDefinition> columns = getTableColumns(table);
        if (values.size() != columns.size()) {
            throw new IllegalArgumentException("Parameter count mismatch");
        }

        byte[] rowData = serializeRow(values, columns);

        byte[] page = readPage(table, 0);
        int freePos = findFreePosition(page);

        if (freePos + rowData.length + 4 <= PAGE_SIZE) {
            writeRow(page, freePos, rowData);
            writePage(table, 0, page);
        } else {
            throw new IllegalStateException("No space in page");
        }
    }

    @Override
    public List<Object> select(String tableName, List<String> columnNames) {
        TableDefinition table = catalogManager.getTable(tableName);
        if (table == null) {
            throw new IllegalArgumentException("Table not found: " + tableName);
        }

        List<Object> result = new ArrayList<>();
        List<ColumnDefinition> allColumns = getTableColumns(table);
        List<ColumnDefinition> selectedColumns = columnNames.isEmpty() ?
                allColumns : getSelectedColumns(allColumns, columnNames);


        byte[] page = readPage(table, 0);
        if (page != null) {
            result.addAll(readPageRows(page, selectedColumns, allColumns));
        }

        return result;
    }

    private byte[] serializeRow(List<Object> values, List<ColumnDefinition> columns) {
        ByteBuffer buffer = ByteBuffer.allocate(PAGE_SIZE).order(ByteOrder.LITTLE_ENDIAN);

        for (int i = 0; i < values.size(); i++) {
            Object value = values.get(i);
            TypeDefinition type = getType(columns.get(i).getTypeOid());

            switch (type.name().toLowerCase()) {
                case "integer":
                    buffer.putInt((Integer) value);
                    break;
                case "bigint":
                    buffer.putLong((Long) value);
                    break;
                case "boolean":
                    buffer.put((byte) ((Boolean) value ? 1 : 0));
                    break;
                case "varchar":
                    String str = (String) value;
                    byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);
                    buffer.putShort((short) strBytes.length);
                    buffer.put(strBytes);
                    break;
            }
        }

        byte[] result = new byte[buffer.position()];
        System.arraycopy(buffer.array(), 0, result, 0, buffer.position());
        return result;
    }

    private List<Object> deserializeRow(ByteBuffer buffer, List<ColumnDefinition> columns) {
        List<Object> row = new ArrayList<>();

        for (ColumnDefinition column : columns) {
            TypeDefinition type = getType(column.getTypeOid());

            switch (type.name().toLowerCase()) {
                case "integer":
                    row.add(buffer.getInt());
                    break;
                case "bigint":
                    row.add(buffer.getLong());
                    break;
                case "boolean":
                    row.add(buffer.get() != 0);
                    break;
                case "varchar":
                    int len = buffer.getShort() & 0xFFFF;
                    byte[] strBytes = new byte[len];
                    buffer.get(strBytes);
                    row.add(new String(strBytes, StandardCharsets.UTF_8));
                    break;
            }
        }

        return row;
    }

    private int findFreePosition(byte[] page) {
        ByteBuffer buffer = ByteBuffer.wrap(page).order(ByteOrder.LITTLE_ENDIAN);
        int position = 0;

        while (position < PAGE_SIZE - 4) {
            buffer.position(position);
            int size = buffer.getInt();
            if (size == 0) {
                return position;
            }
            position += 4 + size;
        }

        return position;
    }

    private void writeRow(byte[] page, int position, byte[] rowData) {
        ByteBuffer buffer = ByteBuffer.wrap(page).order(ByteOrder.LITTLE_ENDIAN);
        buffer.position(position);
        buffer.putInt(rowData.length);
        buffer.put(rowData);
    }

    private List<Object> readPageRows(byte[] page, List<ColumnDefinition> selectedColumns,
                                      List<ColumnDefinition> allColumns) {
        List<Object> rows = new ArrayList<>();
        ByteBuffer buffer = ByteBuffer.wrap(page).order(ByteOrder.LITTLE_ENDIAN);

        while (buffer.position() < PAGE_SIZE - 4) {
            int currentPos = buffer.position();
            int rowSize = buffer.getInt();

            if (rowSize == 0 || buffer.position() + rowSize > PAGE_SIZE) {
                break;
            }

            List<Object> row = deserializeRow(buffer, allColumns);

            if (!selectedColumns.equals(allColumns)) {
                List<Object> filteredRow = new ArrayList<>();
                for (int i = 0; i < allColumns.size(); i++) {
                    if (selectedColumns.contains(allColumns.get(i))) {
                        filteredRow.add(row.get(i));
                    }
                }
                rows.add(filteredRow);
            } else {
                rows.add(row);
            }

            // Ensure we read exactly rowSize bytes
            if (buffer.position() != currentPos + 4 + rowSize) {
                buffer.position(currentPos + 4 + rowSize);
            }
        }

        return rows;
    }

    private byte[] readPage(TableDefinition table, int pageNum) {
        String filename = table.getOid() + ".dat";
        File file = new File(filename);

        // If file doesn't exist — return a newly initialized page with HeapPage header
        if (!file.exists()) {
            return createEmptyHeapPageBytes();
        }

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            if (pageNum * PAGE_SIZE >= raf.length()) {
                // page beyond file size -> return empty initialized page
                return createEmptyHeapPageBytes();
            }

            raf.seek(pageNum * PAGE_SIZE);
            byte[] page = new byte[PAGE_SIZE];
            int bytesRead = raf.read(page);

            if (bytesRead < PAGE_SIZE) {
                // Fill remaining with zeros
                Arrays.fill(page, bytesRead, PAGE_SIZE, (byte) 0);
            }

            // If page doesn't have valid HeapPage signature, initialize header so later readers (HeapPage) won't fail
            ByteBuffer buf = ByteBuffer.wrap(page).order(ByteOrder.LITTLE_ENDIAN);
            int sig = buf.getInt(0);
            if (sig != 0xDBDB01) {
                // initialize header fields similarly to HeapPage constructor:
                buf.putInt(0, 0xDBDB01);
                buf.putShort(4, (short) 0);             // size = 0 (no records yet)
                buf.putShort(6, (short) 10);            // lower = HEADER_SIZE (10)
                buf.putShort(8, (short) PAGE_SIZE);     // upper = PAGE_SIZE
                // copy back
                System.arraycopy(buf.array(), 0, page, 0, PAGE_SIZE);
            }

            return page;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read page", e);
        }
    }

    private byte[] createEmptyHeapPageBytes() {
        byte[] page = new byte[PAGE_SIZE];
        ByteBuffer buf = ByteBuffer.wrap(page).order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(0, 0xDBDB01);
        buf.putShort(4, (short) 0);          // size = 0
        buf.putShort(6, (short) 10);         // lower = HEADER_SIZE (10)
        buf.putShort(8, (short) PAGE_SIZE);  // upper = PAGE_SIZE
        return page;
    }

    private void writePage(TableDefinition table, int pageNum, byte[] page) {
        String filename = table.getOid() + ".dat";

        try (RandomAccessFile raf = new RandomAccessFile(filename, "rw")) {
            raf.seek(pageNum * PAGE_SIZE);
            raf.write(page);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write page", e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<ColumnDefinition> getTableColumns(TableDefinition table) {
        try {
            java.lang.reflect.Method method = catalogManager.getClass()
                    .getMethod("getTableColumns", TableDefinition.class);
            return (List<ColumnDefinition>) method.invoke(catalogManager, table);
        } catch (Exception e) {
            throw new RuntimeException("Cannot get table columns", e);
        }
    }

    private TypeDefinition getType(int typeOid) {
        try {
            java.lang.reflect.Method method = catalogManager.getClass()
                    .getMethod("getType", int.class);
            return (TypeDefinition) method.invoke(catalogManager, typeOid);
        } catch (Exception e) {
            return new TypeDefinition(typeOid, "integer", 4);
        }
    }

    private List<ColumnDefinition> getSelectedColumns(List<ColumnDefinition> allColumns,
                                                      List<String> columnNames) {
        List<ColumnDefinition> selected = new ArrayList<>();
        for (String colName : columnNames) {
            for (ColumnDefinition col : allColumns) {
                if (col.getName().equalsIgnoreCase(colName)) {
                    selected.add(col);
                    break;
                }
            }
        }
        return selected;
    }
}