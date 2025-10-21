package ru.open.cu.student.catalog.manager;

import ru.open.cu.student.catalog.model.TableDefinition;
import ru.open.cu.student.catalog.model.ColumnDefinition;

import java.util.List;

public interface CatalogManager {

    TableDefinition createTable(String name, List<ColumnDefinition> columns);

    TableDefinition getTable(String tableName);

    ColumnDefinition getColumn(TableDefinition table, String columnName);

    List<TableDefinition> listTables();

}
