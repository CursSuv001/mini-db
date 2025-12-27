package ru.open.cu.student.catalog.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TableDefinitionTest {

    @Test
    void testTableDefinitionCreation() {
        TableDefinition table = new TableDefinition(1, "users", "table", "123.dat", 5);

        assertEquals(1, table.getOid());
        assertEquals("users", table.getName());
        assertEquals("table", table.getType());
        assertEquals("123.dat", table.getFileNode());
        assertEquals(5, table.getPagesCount());
    }

    @Test
    void testTableDefinitionInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () ->
                new TableDefinition(-1, "users", "table", "123.dat", 5));
        assertThrows(IllegalArgumentException.class, () ->
                new TableDefinition(1, "", "table", "123.dat", 5));
        assertThrows(IllegalArgumentException.class, () ->
                new TableDefinition(1, "users", "", "123.dat", 5));
        assertThrows(IllegalArgumentException.class, () ->
                new TableDefinition(1, "users", "table", "", 5));
        assertThrows(IllegalArgumentException.class, () ->
                new TableDefinition(1, "users", "table", "123.dat", -1));
    }

    @Test
    void testTableDefinitionSerialization() {
        TableDefinition original = new TableDefinition(1, "users", "table", "123.dat", 5);
        byte[] bytes = original.toBytes();

        TableDefinition restored = TableDefinition.fromBytes(bytes);

        assertEquals(original, restored);
        assertEquals(original.getOid(), restored.getOid());
        assertEquals(original.getName(), restored.getName());
    }

    @Test
    void testTableDefinitionEquality() {
        TableDefinition table1 = new TableDefinition(1, "users", "table", "123.dat", 5);
        TableDefinition table2 = new TableDefinition(1, "users", "table", "123.dat", 5);
        TableDefinition table3 = new TableDefinition(2, "products", "table", "124.dat", 3);

        assertEquals(table1, table2);
        assertNotEquals(table1, table3);
        assertEquals(table1.hashCode(), table2.hashCode());
    }
}