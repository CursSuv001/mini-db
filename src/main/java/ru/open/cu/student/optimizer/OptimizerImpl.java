package ru.open.cu.student.optimizer;


import ru.open.cu.student.optimizer.node.PhysicalCreateNode;
import ru.open.cu.student.optimizer.node.PhysicalInsertNode;
import ru.open.cu.student.optimizer.node.PhysicalPlanNode;
import ru.open.cu.student.planner.node.CreateTableNode;
import ru.open.cu.student.planner.node.InsertNode;
import ru.open.cu.student.planner.node.LogicalPlanNode;

public class OptimizerImpl implements Optimizer {

    @Override
    public PhysicalPlanNode optimize(LogicalPlanNode logicalPlan) {

        // --- CREATE TABLE ---
        if (logicalPlan instanceof CreateTableNode ln) {
            return new PhysicalCreateNode(ln.getTableDefinition());

            // --- INSERT ---
        } else if (logicalPlan instanceof InsertNode ln) {
            return new PhysicalInsertNode(ln.getTableDefinition(), ln.getValues());

        }

        throw new UnsupportedOperationException(
                "Unsupported logical node type: " + logicalPlan.getClass().getSimpleName()
        );
    }
}