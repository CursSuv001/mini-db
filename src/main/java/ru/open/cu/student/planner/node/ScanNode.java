package ru.open.cu.student.planner.node;


import ru.open.cu.student.catalog.model.TableDefinition;

/**
 * Логический узел SeqScan — полное сканирование таблицы.
 */
public class ScanNode extends LogicalPlanNode {
    private final TableDefinition tableDefinition;

    public ScanNode(TableDefinition tableDefinition) {
        super("Scan");
        this.tableDefinition = tableDefinition;
        // Устанавливаем выходные колонки как все колонки таблицы
        this.outputColumns = tableDefinition.getColumns().stream()
                .map(col -> tableDefinition.getName() + "." + col.getName())
                .toList();
    }

    public TableDefinition getTableDefinition() {
        return tableDefinition;
    }

    @Override
    public String prettyPrint(String indent) {
        return indent + "Scan(" + tableDefinition.getName() + ")\n";
    }
}