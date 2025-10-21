package ru.open.cu.student.execution;


import ru.open.cu.student.catalog.manager.CatalogManager;
import ru.open.cu.student.catalog.operation.OperationManager;
import ru.open.cu.student.execution.executors.CreateTableExecutor;
import ru.open.cu.student.execution.executors.Executor;
import ru.open.cu.student.execution.executors.InsertExecutor;
import ru.open.cu.student.optimizer.node.PhysicalCreateNode;
import ru.open.cu.student.optimizer.node.PhysicalInsertNode;
import ru.open.cu.student.optimizer.node.PhysicalPlanNode;

public class ExecutorFactoryImpl implements ExecutorFactory {

    private final CatalogManager catalogManager;
    private final OperationManager operationManager;

    public ExecutorFactoryImpl(CatalogManager catalogManager, OperationManager operationManager) {
        this.catalogManager = catalogManager;
        this.operationManager = operationManager;
    }

    @Override
    public Executor createExecutor(PhysicalPlanNode plan) {
        if (plan instanceof PhysicalCreateNode create) {
            return new CreateTableExecutor(catalogManager, create.getTableDefinition());

        } else if (plan instanceof PhysicalInsertNode insert) {
            return new InsertExecutor(
                    operationManager,
                    insert.getTableDefinition(),
                    insert.getValues()
            );

        }

        throw new UnsupportedOperationException(
                "Unsupported physical plan node: " + plan.getClass().getSimpleName()
        );
    }
}