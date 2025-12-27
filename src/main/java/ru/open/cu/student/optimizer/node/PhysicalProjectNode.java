package ru.open.cu.student.optimizer.node;


import ru.open.cu.student.ast.TargetEntry;

import java.util.List;

/**
 * Физический узел Project — формирует итоговый SELECT-список.
 * Вызывает выполнение нижнего узла и выбирает указанные колонки/выражения.
 */
public class PhysicalProjectNode extends PhysicalPlanNode {
    private final List<TargetEntry> targetList;
    private final PhysicalPlanNode child;

    public PhysicalProjectNode(List<TargetEntry> targetList, PhysicalPlanNode child) {
        super("PhysicalProject");
        this.targetList = targetList;
        this.child = child;
    }

    public List<TargetEntry> getTargetList() {
        return targetList;
    }

    public PhysicalPlanNode getChild() {
        return child;
    }

    @Override
    public String prettyPrint(String indent) {
        List<String> columnNames = targetList.stream()
                .map(te -> te.alias != null ? te.alias : te.expr.toString())
                .toList();
        return indent + "PhysicalProject(" + columnNames + ")\n" + child.prettyPrint(indent + "  ");
    }
}