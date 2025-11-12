package ru.open.cu.student.execution.executors;

import ru.open.cu.student.ast.AConst;
import ru.open.cu.student.ast.AExpr;
import ru.open.cu.student.ast.AstNode;
import ru.open.cu.student.ast.Expr;

import java.nio.ByteBuffer;

public class FilterExecutor implements Executor {
    private final Executor child;
    private final Expr condition;
    private boolean isOpen;

    public FilterExecutor(Executor child, Expr condition) {
        this.child = child;
        this.condition = condition;
    }

    @Override
    public void open() {
        child.open();
        isOpen = true;
    }

    @Override
    public Object next() {
        if (!isOpen) return null;

        Object rowData;
        while ((rowData = child.next()) != null) {
            if (passesFilter(rowData)) {
                return rowData;
            }
        }
        return null;
    }

    private boolean passesFilter(Object rowData) {
        // Проверяем, что condition это AExpr
        if (!(condition instanceof AExpr)) {
            return false;
        }

        AExpr aexpr = (AExpr) condition;

        // Проверяем, что данные в байтовом формате
        if (!(rowData instanceof byte[])) {
            return false;
        }

        byte[] data = (byte[]) rowData;

        // Получаем значение из данных (простая реализация - первое поле)
        int value = ByteBuffer.wrap(data).getInt(0);

        // Обрабатываем правую часть выражения
        AstNode rightNode = aexpr.getRight();
        if (rightNode instanceof AConst) {
            Object constValue = ((AConst) rightNode).value;
            if (constValue instanceof Integer) {
                int filterValue = (Integer) constValue;

                // Применяем оператор
                switch (aexpr.getOp()) {
                    case "=": return value == filterValue;
                    case ">": return value > filterValue;
                    case "<": return value < filterValue;
                    case ">=": return value >= filterValue;
                    case "<=": return value <= filterValue;
                    case "!=": return value != filterValue;
                    default: return false;
                }
            }
        }

        return false;
    }

    @Override
    public void close() {
        child.close();
        isOpen = false;
    }
}