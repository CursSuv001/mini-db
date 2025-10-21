package ru.open.cu.student.planner;


import ru.open.cu.student.ast.QueryTree;
import ru.open.cu.student.catalog.manager.CatalogManager;
import ru.open.cu.student.catalog.model.ColumnDefinition;
import ru.open.cu.student.catalog.model.TableDefinition;
import ru.open.cu.student.catalog.model.TypeDefinition;
import ru.open.cu.student.ast.Expr;
import ru.open.cu.student.ast.TargetEntry;
import ru.open.cu.student.planner.node.CreateTableNode;
import ru.open.cu.student.planner.node.InsertNode;
import ru.open.cu.student.planner.node.LogicalPlanNode;

import java.util.ArrayList;
import java.util.List;


/**
 * Планировщик, преобразующий QueryTree в LogicalPlanNode.
 */
public class PlannerImpl implements Planner {

    private final CatalogManager catalogManager;

    public PlannerImpl(CatalogManager catalogManager) {
        this.catalogManager = catalogManager;
    }

    @Override
    public LogicalPlanNode plan(QueryTree queryTree) {
        if (queryTree == null) throw new IllegalArgumentException("QueryTree is null");

        return switch (queryTree.commandType) {
            case CREATE -> planCreate(queryTree);
            case INSERT -> planInsert(queryTree);
        };
    }

    // ---------- CREATE ----------
    private LogicalPlanNode planCreate(QueryTree q) {
        String tableName = extractTableName(q);

        List<ColumnDefinition> columns = new ArrayList<>();
        int position = 0;
        for (TargetEntry te : q.targetList) {
            TypeDefinition type = catalogManager.getTypeByName(te.resultType);

            columns.add(new ColumnDefinition(
                    type.getOid(),
                    te.alias,
                    position++
            ));
        }

        TableDefinition tableDef = new TableDefinition(0, tableName, "USER", tableName, 0);
        tableDef.setColumns(columns);

        return new CreateTableNode(tableDef);
    }

    // ---------- INSERT ----------
    private LogicalPlanNode planInsert(QueryTree q) {
        String tableName = extractTableName(q);
        TableDefinition tableDef = catalogManager.getTable(tableName);

        List<Expr> values = q.targetList.stream()
                .map(te -> te.expr)
                .toList();

        return new InsertNode(tableDef, values);
    }


    private String extractTableName(QueryTree q) {
        if (q.rangeTable != null && !q.rangeTable.isEmpty() && q.rangeTable.get(0).tableName != null) {
            return q.rangeTable.get(0).tableName;
        }
        throw new IllegalArgumentException("Cannot determine table name");
    }
}