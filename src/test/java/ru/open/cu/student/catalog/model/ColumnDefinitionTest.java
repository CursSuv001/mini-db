package ru.open.cu.student.catalog.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ColumnDefinitionTest {

    @Test
    void testColumnDefinitionCreation() {
        ColumnDefinition column = new ColumnDefinition(1, 10, 20, "username", 0);

        assertEquals(1, column.getOid());
        assertEquals(10, column.getTableOid());
        assertEquals(20, column.getTypeOid());
        assertEquals("username", column.getName());
        assertEquals(0, column.getPosition());
    }

    @Test
    void testColumnDefinitionInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () ->
                new ColumnDefinition(-1, 10, 20, "username", 0));
        assertThrows(IllegalArgumentException.class, () ->
                new ColumnDefinition(1, -1, 20, "username", 0));
        assertThrows(IllegalArgumentException.class, () ->
                new ColumnDefinition(1, 10, -1, "username", 0));
        assertThrows(IllegalArgumentException.class, () ->
                new ColumnDefinition(1, 10, 20, "", 0));
        assertThrows(IllegalArgumentException.class, () ->
                new ColumnDefinition(1, 10, 20, "username", -1));
    }

    @Test
    void testColumnDefinitionSerialization() {
        ColumnDefinition original = new ColumnDefinition(1, 10, 20, "username", 0);
        byte[] bytes = original.toBytes();

        ColumnDefinition restored = ColumnDefinition.fromBytes(bytes);

        assertEquals(original, restored);
        assertEquals(original.getName(), restored.getName());
        assertEquals(original.getTableOid(), restored.getTableOid());
    }

    @Test
    void testColumnDefinitionEquality() {
        ColumnDefinition col1 = new ColumnDefinition(1, 10, 20, "username", 0);
        ColumnDefinition col2 = new ColumnDefinition(1, 10, 20, "username", 0);
        ColumnDefinition col3 = new ColumnDefinition(2, 10, 20, "email", 1);

        assertEquals(col1, col2);
        assertNotEquals(col1, col3);
        assertEquals(col1.hashCode(), col2.hashCode());
    }
}