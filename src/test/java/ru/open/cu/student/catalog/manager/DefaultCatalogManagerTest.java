package ru.open.cu.student.catalog.manager;

import ru.open.cu.student.catalog.model.TableDefinition;
import ru.open.cu.student.catalog.model.ColumnDefinition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import ru.open.cu.student.catalog.model.TypeDefinition;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

public class DefaultCatalogManagerTest {

    private DefaultCatalogManager catalogManager;

    @BeforeEach
    void setUp() {
        // Очищаем файлы перед каждым тестом
        deleteTestFiles();
        catalogManager = new DefaultCatalogManager();
    }

    @Test
    void testCreateTable() {
        List<ColumnDefinition> columns = Arrays.asList(
                new ColumnDefinition(0, 0, 1, "id", 0),
                new ColumnDefinition(0, 0, 2, "name", 1)
        );

        TableDefinition table = catalogManager.createTable("users", columns);

        assertNotNull(table);
        assertEquals("users", table.getName());
        assertTrue(table.getOid() > 0);
    }

    @Test
    void testCreateDuplicateTable() {
        List<ColumnDefinition> columns = Arrays.asList(
                new ColumnDefinition(0, 0, 1, "id", 0)
        );

        catalogManager.createTable("users", columns);

        assertThrows(IllegalArgumentException.class, () ->
                catalogManager.createTable("users", columns));
    }

    @Test
    void testGetTable() {
        List<ColumnDefinition> columns = Arrays.asList(
                new ColumnDefinition(0, 0, 1, "id", 0)
        );

        TableDefinition created = catalogManager.createTable("users", columns);
        TableDefinition found = catalogManager.getTable("users");

        assertNotNull(found);
        assertEquals(created.getOid(), found.getOid());
        assertEquals(created.getName(), found.getName());
    }

    @Test
    void testGetNonExistentTable() {
        TableDefinition table = catalogManager.getTable("non_existent");
        assertNull(table);
    }

    @Test
    void testGetColumn() {
        List<ColumnDefinition> columns = Arrays.asList(
                new ColumnDefinition(0, 0, 1, "id", 0),
                new ColumnDefinition(0, 0, 2, "name", 1)
        );

        TableDefinition table = catalogManager.createTable("users", columns);
        ColumnDefinition column = catalogManager.getColumn(table, "name");

        assertNotNull(column);
        assertEquals("name", column.getName());
    }

    @Test
    void testGetNonExistentColumn() {
        List<ColumnDefinition> columns = Arrays.asList(
                new ColumnDefinition(0, 0, 1, "id", 0)
        );

        TableDefinition table = catalogManager.createTable("users", columns);
        ColumnDefinition column = catalogManager.getColumn(table, "non_existent");

        assertNull(column);
    }

    @Test
    void testListTables() {
        List<ColumnDefinition> columns = Arrays.asList(
                new ColumnDefinition(0, 0, 1, "id", 0)
        );

        catalogManager.createTable("users", columns);
        catalogManager.createTable("products", columns);

        List<TableDefinition> tables = catalogManager.listTables();

        assertEquals(2, tables.size());
        assertTrue(tables.stream().anyMatch(t -> t.getName().equals("users")));
        assertTrue(tables.stream().anyMatch(t -> t.getName().equals("products")));
    }

    @Test
    void testGetTableColumns() {
        List<ColumnDefinition> originalColumns = Arrays.asList(
                new ColumnDefinition(0, 0, 1, "id", 0),
                new ColumnDefinition(0, 0, 2, "name", 1)
        );

        TableDefinition table = catalogManager.createTable("users", originalColumns);
        List<ColumnDefinition> retrievedColumns = catalogManager.getTableColumns(table);

        assertEquals(2, retrievedColumns.size());
        assertEquals("id", retrievedColumns.get(0).getName());
        assertEquals("name", retrievedColumns.get(1).getName());
    }

    @Test
    void testGetType() {
        TypeDefinition type = catalogManager.getType("integer");
        assertNotNull(type);
        assertEquals("integer", type.name());
    }

    private void deleteTestFiles() {
        String[] files = {"table_definitions.dat", "column_definitions.dat", "types_definitions.dat", "1.dat", "2.dat"};
        for (String file : files) {
            new java.io.File(file).delete();
        }
    }
}