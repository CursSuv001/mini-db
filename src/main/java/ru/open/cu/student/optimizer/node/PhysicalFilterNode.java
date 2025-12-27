package ru.open.cu.student.optimizer.node;


import ru.open.cu.student.ast.Expr;

/**
 * Физический узел Filter — фильтрация строк по предикату WHERE.
 * Работает поверх дочернего физического узла (child).
 */
public class PhysicalFilterNode extends PhysicalPlanNode {
    private final Expr condition;
    private final PhysicalPlanNode child;

    public PhysicalFilterNode(Expr condition, PhysicalPlanNode child) {
        super("PhysicalFilter");
        this.condition = condition;
        this.child = child;
    }

    public Expr getCondition() {
        return condition;
    }

    public PhysicalPlanNode getChild() {
        return child;
    }

    @Override
    public String prettyPrint(String indent) {
        return indent + "PhysicalFilter(" + condition + ")\n" + child.prettyPrint(indent + "  ");
    }
}