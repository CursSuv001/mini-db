package ru.open.cu.student.planner.node;


import ru.open.cu.student.ast.Expr;

/**
 * Логический узел Filter — фильтрация строк по предикату.
 */
public class FilterNode extends LogicalPlanNode {
    private final Expr condition;
    private final LogicalPlanNode child;

    public FilterNode(Expr condition, LogicalPlanNode child) {
        super("Filter");
        this.condition = condition;
        this.child = child;
        // Filter передает схему от ребенка
        this.outputColumns = child.getOutputColumns();
    }

    public Expr getCondition() {
        return condition;
    }

    public LogicalPlanNode getChild() {
        return child;
    }

    @Override
    public String prettyPrint(String indent) {
        return indent + "Filter(" + condition + ")\n" + child.prettyPrint(indent + "  ");
    }
}