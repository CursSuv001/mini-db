package ru.open.cu.student.execution;


import ru.open.cu.student.ast.AExpr;
import ru.open.cu.student.catalog.manager.CatalogManager;
import ru.open.cu.student.catalog.operation.OperationManager;
import ru.open.cu.student.execution.executors.*;
import ru.open.cu.student.memory.buffer.BufferPoolManager;
import ru.open.cu.student.optimizer.node.*;

public class ExecutorFactoryImpl implements ExecutorFactory {

    private final CatalogManager catalogManager;
    private final OperationManager operationManager;
    private final BufferPoolManager bufferPool;


    public ExecutorFactoryImpl(CatalogManager catalogManager, OperationManager operationManager, BufferPoolManager bufferPool) {
        this.catalogManager = catalogManager;
        this.operationManager = operationManager;
        this.bufferPool = bufferPool;
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

        } else if (plan instanceof PhysicalSeqScanNode scan) {
            return new SeqScanExecutor(bufferPool, scan.getTableDefinition());

        } else if (plan instanceof PhysicalFilterNode filter) {
            Executor child = createExecutor(filter.getChild());
            return new FilterExecutor(child, filter.getCondition());

        } else if (plan instanceof PhysicalProjectNode project) {
            Executor child = createExecutor(project.getChild());
            return new ProjectExecutor(child, project.getTargetList());
        }

        throw new UnsupportedOperationException(
                "Unsupported physical plan node: " + plan.getClass().getSimpleName()
        );
    }
}