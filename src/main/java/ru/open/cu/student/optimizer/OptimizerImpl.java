package ru.open.cu.student.optimizer;


import ru.open.cu.student.optimizer.node.*;
import ru.open.cu.student.planner.node.*;

public class OptimizerImpl implements Optimizer {

    @Override
    public PhysicalPlanNode optimize(LogicalPlanNode logicalPlan) {

        // --- CREATE TABLE ---
        if (logicalPlan instanceof CreateTableNode ln) {
            return new PhysicalCreateNode(ln.getTableDefinition());

            // --- INSERT ---
        } else if (logicalPlan instanceof InsertNode ln) {
            return new PhysicalInsertNode(ln.getTableDefinition(), ln.getValues());

        } else if (logicalPlan instanceof ProjectNode ln) {
            PhysicalPlanNode child = optimize(ln.getChild());
            return new PhysicalProjectNode(ln.getTargetList(), child);

            // --- SELECT - Filter ---
        } else if (logicalPlan instanceof FilterNode ln) {
            PhysicalPlanNode child = optimize(ln.getChild());
            return new PhysicalFilterNode(ln.getCondition(), child);

            // --- SELECT - Scan ---
        } else if (logicalPlan instanceof ScanNode ln) {
            return new PhysicalSeqScanNode(ln.getTableDefinition());
        }


        throw new UnsupportedOperationException(
                "Unsupported logical node type: " + logicalPlan.getClass().getSimpleName()
        );
    }
}