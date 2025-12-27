package ru.open.cu.student.execution.executors;

import ru.open.cu.student.ast.TargetEntry;
import ru.open.cu.student.ast.ColumnRef;
import ru.open.cu.student.ast.AConst;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Исполнитель SELECT-списка.
 */
public class ProjectExecutor implements Executor {
    private final Executor child;
    private final List<TargetEntry> targetList;
    private boolean isOpen;

    public ProjectExecutor(Executor child, List<TargetEntry> targetList) {
        this.child = child;
        this.targetList = targetList;
    }

    @Override
    public void open() {
        child.open();
        isOpen = true;
    }

    @Override
    public Object next() {
        if (!isOpen) return null;

        Object rowData = child.next();
        if (rowData == null) return null;

        // Простая проекция - возвращаем только нужные поля
        return extractProjectedFields((byte[]) rowData);
    }

    private Object extractProjectedFields(byte[] rowData) {
        if (targetList.isEmpty()) {
            return rowData; // SELECT * - возвращаем все
        }

        List<Object> result = new ArrayList<>();
        ByteBuffer buffer = ByteBuffer.wrap(rowData);

        for (TargetEntry target : targetList) {
            if (target.expr instanceof ColumnRef) {
                // Для простоты - все поля int и идут по порядку
                String colName = ((ColumnRef) target.expr).column;
                int fieldIndex = getFieldIndex(colName);
                result.add(buffer.getInt(fieldIndex * 4));
            } else if (target.expr instanceof AConst) {
                result.add(((AConst) target.expr).value);
            }
        }
        return result;
    }

    private int getFieldIndex(String columnName) {
        // Простая логика - для демонстрации
        return switch (columnName) {
            case "id" -> 0;
            case "name" -> 1;
            case "age" -> 2;
            default -> 0;
        };
    }

    @Override
    public void close() {
        child.close();
        isOpen = false;
    }
}