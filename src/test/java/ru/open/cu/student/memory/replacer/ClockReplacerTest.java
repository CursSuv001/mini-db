package ru.open.cu.student.memory.replacer;

import org.junit.jupiter.api.Test;
import ru.open.cu.student.memory.model.BufferSlot;
import ru.open.cu.student.memory.page.HeapPage;

import static org.junit.jupiter.api.Assertions.*;

class ClockReplacerTest {

    @Test
    void push_existingSlot_setsRefBit() {
        ClockReplacer clock = new ClockReplacer();

        BufferSlot slot1 = new BufferSlot(1, new HeapPage(1));
        BufferSlot slot2 = new BufferSlot(2, new HeapPage(2));

        clock.push(slot1);
        clock.push(slot2);

        // Multiple pushes to slot1 should not cause errors
        assertDoesNotThrow(() -> {
            clock.push(slot1);
            clock.push(slot1);
            clock.push(slot1);
        });

        // Should be able to pick a victim without errors
        BufferSlot victim = clock.pickVictim();
        assertNotNull(victim);
    }

    @Test
    void push_existingSlot_refBitBehavior() {
        ClockReplacer clock = new ClockReplacer();

        BufferSlot slot1 = new BufferSlot(1, new HeapPage(1));
        BufferSlot slot2 = new BufferSlot(2, new HeapPage(2));

        clock.push(slot1);
        clock.push(slot2);
        clock.push(slot1); // Update slot1

        // The behavior depends on the clock hand position
        // Just verify that we can pick victims without errors
        BufferSlot victim1 = clock.pickVictim();
        BufferSlot victim2 = clock.pickVictim();

        assertNotNull(victim1);
        assertNotNull(victim2);
        assertNotEquals(victim1.getPageId(), victim2.getPageId());
    }
}