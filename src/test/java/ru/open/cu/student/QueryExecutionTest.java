package ru.open.cu.student;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.open.cu.student.ast.*;
import ru.open.cu.student.catalog.manager.CatalogManager;
import ru.open.cu.student.catalog.manager.DefaultCatalogManager;
import ru.open.cu.student.catalog.model.TypeDefinition;
import ru.open.cu.student.catalog.operation.OperationManager;
import ru.open.cu.student.execution.ExecutorFactory;
import ru.open.cu.student.execution.ExecutorFactoryImpl;
import ru.open.cu.student.execution.QueryExecutionEngine;
import ru.open.cu.student.execution.QueryExecutionEngineImpl;
import ru.open.cu.student.execution.executors.Executor;
import ru.open.cu.student.memory.buffer.BufferPoolManager;
import ru.open.cu.student.memory.model.BufferSlot;
import ru.open.cu.student.memory.page.Page;
import ru.open.cu.student.optimizer.Optimizer;
import ru.open.cu.student.optimizer.OptimizerImpl;
import ru.open.cu.student.planner.Planner;
import ru.open.cu.student.planner.PlannerImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimpleQueryExecutionTest {
    private CatalogManager catalogManager;
    private Planner planner;
    private Optimizer optimizer;
    private ExecutorFactory executorFactory;
    private QueryExecutionEngine executionEngine;

    @BeforeEach
    void setUp() {
        catalogManager = new DefaultCatalogManager();

        BufferPoolManager bufferPool = new BufferPoolManager() {
            public BufferSlot getPage(int pageId) { return null; }
            public void updatePage(int pageId, Page page) { }
            public void pinPage(int pageId) { }
            public void flushPage(int pageId) { }
            public void flushAllPages() { }
            public List<BufferSlot> getDirtyPages() { return List.of(); }
        };

        OperationManager operationManager = new OperationManager() {
            public void insert(String tableName, List<Object> values) {}
            public List<Object> select(String tableName, List<String> columnNames) {
                return List.of();
            }
        };

        planner = new PlannerImpl(catalogManager);
        optimizer = new OptimizerImpl();
        executorFactory = new ExecutorFactoryImpl(catalogManager, operationManager, bufferPool);
        executionEngine = new QueryExecutionEngineImpl();
    }

    // ========== МИНИМАЛЬНЫЕ РАБОЧИЕ ТЕСТЫ ==========

    @Test
    void testPlanner_CreateTable_WithValidTypes() {
        QueryTree query = createSimpleCreateQueryWithValidTypes("test_table");

        var logicalPlan = planner.plan(query);
        assertNotNull(logicalPlan);
    }

    @Test
    void testOptimizer_CreateTable_WithValidTypes() {
        QueryTree query = createSimpleCreateQueryWithValidTypes("test_table");

        var logicalPlan = planner.plan(query);
        var physicalPlan = optimizer.optimize(logicalPlan);
        assertNotNull(physicalPlan);
    }

    @Test
    void testFullPipeline_CreateTable_WithValidTypes() {
        QueryTree query = createSimpleCreateQueryWithValidTypes("test_table");

        var logicalPlan = planner.plan(query);
        var physicalPlan = optimizer.optimize(logicalPlan);
        Executor executor = executorFactory.createExecutor(physicalPlan);
        List<Object> results = executionEngine.execute(executor);

        assertNotNull(results);
    }

    @Test
    void testPlanner_Select_NonExistentTable() {
        QueryTree selectQuery = createSimpleSelectQuery("nonexistent_table");

        // Для несуществующей таблицы ожидаем ошибку
        assertThrows(Exception.class, () -> planner.plan(selectQuery));
    }

    @Test
    void testMultipleCreateTables_WithValidTypes() {
        executeCreateTable("table1");
        executeCreateTable("table2");

        // Если дошли сюда - все ок
        assertTrue(true);
    }

    // ========== ИСПРАВЛЕННЫЕ ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    private QueryTree createSimpleCreateQueryWithValidTypes(String tableName) {
        QueryTree query = new QueryTree();
        query.commandType = QueryType.CREATE;
        query.rangeTable.add(new RangeTblEntry(tableName));

        // Используем ТОЛЬКО существующие типы из DefaultCatalogManager
        TargetEntry idCol = new TargetEntry(null, "id");
        idCol.resultType = "integer";  // Этот тип создается в initializeDefaultTypes()
        query.targetList.add(idCol);

        TargetEntry nameCol = new TargetEntry(null, "name");
        nameCol.resultType = "varchar"; // Этот тип создается в initializeDefaultTypes()
        query.targetList.add(nameCol);

        return query;
    }

    private QueryTree createSimpleInsertQuery(String tableName, int id, String name) {
        QueryTree query = new QueryTree();
        query.commandType = QueryType.INSERT;
        query.rangeTable.add(new RangeTblEntry(tableName));

        query.targetList.add(new TargetEntry(new AConst(id), null));
        query.targetList.add(new TargetEntry(new AConst(name), null));

        return query;
    }

    private QueryTree createSimpleSelectQuery(String tableName) {
        QueryTree query = new QueryTree();
        query.commandType = QueryType.SELECT;
        query.rangeTable.add(new RangeTblEntry(tableName));
        return query;
    }

    private void executeCreateTable(String tableName) {
        QueryTree createQuery = createSimpleCreateQueryWithValidTypes(tableName);
        var logicalPlan = planner.plan(createQuery);
        var physicalPlan = optimizer.optimize(logicalPlan);
        Executor executor = executorFactory.createExecutor(physicalPlan);
        executionEngine.execute(executor);
    }

    // ========== ТЕСТЫ ДЛЯ ПРОВЕРКИ КАТАЛОГА ==========

    @Test
    void testCatalogManager_DefaultTypesExist() {
        // Проверяем что базовые типы создаются
        TypeDefinition integerType = ((DefaultCatalogManager) catalogManager).getType("integer");
        assertNotNull(integerType, "Integer type should exist");

        TypeDefinition varcharType = ((DefaultCatalogManager) catalogManager).getType("varchar");
        assertNotNull(varcharType, "Varchar type should exist");

        TypeDefinition booleanType = ((DefaultCatalogManager) catalogManager).getType("boolean");
        assertNotNull(booleanType, "Boolean type should exist");
    }
}