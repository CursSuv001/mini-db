package ru.open.cu.student.planner.node;


import ru.open.cu.student.ast.Expr;
import ru.open.cu.student.ast.TargetEntry;

import java.util.List;

/**
 * Логический узел Project — выборка указанных колонок из результата нижнего узла.
 */
public class ProjectNode extends LogicalPlanNode {
    private final List<TargetEntry> targetList;
    private final LogicalPlanNode child;

    public ProjectNode(List<TargetEntry> targetList, LogicalPlanNode child) {
        super("Project");
        this.targetList = targetList;
        this.child = child;
        // Устанавливаем выходные колонки из targetList
        this.outputColumns = targetList.stream()
                .map(te -> te.alias != null ? te.alias : extractColumnName(te.expr))
                .toList();
    }

    public List<TargetEntry> getTargetList() {
        return targetList;
    }

    public LogicalPlanNode getChild() {
        return child;
    }

    private String extractColumnName(Expr expr) {
        // Простая реализация для извлечения имени колонки из выражения
        // В реальной системе здесь был бы более сложный анализ AST
        return expr.toString();
    }

    @Override
    public String prettyPrint(String indent) {
        return indent + "Project(" + outputColumns + ")\n" + child.prettyPrint(indent + "  ");
    }
}