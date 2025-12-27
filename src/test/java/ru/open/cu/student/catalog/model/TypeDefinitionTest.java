package ru.open.cu.student.catalog.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TypeDefinitionTest {

    @Test
    void testTypeDefinitionCreation() {
        TypeDefinition type = new TypeDefinition(1, "integer", 4);

        assertEquals(1, type.oid());
        assertEquals("integer", type.name());
        assertEquals(4, type.byteLength());
    }

    @Test
    void testTypeDefinitionVariableLength() {
        TypeDefinition type = new TypeDefinition(2, "varchar", -1);
        assertEquals(-1, type.byteLength());
    }

    @Test
    void testTypeDefinitionInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () ->
                new TypeDefinition(-1, "integer", 4));
        assertThrows(IllegalArgumentException.class, () ->
                new TypeDefinition(1, "", 4));
        assertThrows(IllegalArgumentException.class, () ->
                new TypeDefinition(1, "integer", -2));
    }

    @Test
    void testTypeDefinitionSerialization() {
        TypeDefinition original = new TypeDefinition(1, "integer", 4);
        byte[] bytes = original.toBytes();

        TypeDefinition restored = TypeDefinition.fromBytes(bytes);

        assertEquals(original, restored);
        assertEquals(original.name(), restored.name());
        assertEquals(original.byteLength(), restored.byteLength());
    }

    @Test
    void testTypeDefinitionEquality() {
        TypeDefinition type1 = new TypeDefinition(1, "integer", 4);
        TypeDefinition type2 = new TypeDefinition(1, "integer", 4);
        TypeDefinition type3 = new TypeDefinition(2, "varchar", -1);

        assertEquals(type1, type2);
        assertNotEquals(type1, type3);
        assertEquals(type1.hashCode(), type2.hashCode());
    }
}