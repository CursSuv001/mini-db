package ru.open.cu.student.memory.replacer;

import org.junit.jupiter.api.Test;
import ru.open.cu.student.memory.model.BufferSlot;
import ru.open.cu.student.memory.page.HeapPage;

import java.util.LinkedList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LRUReplacerTest {

    @Test
    void push_then_pickVictim_returnsLRU() {
        LinkedList<BufferSlot> list = new LinkedList<>();
        Map<Integer, BufferSlot> map = new HashMap<>();
        LRUReplacer lru = new LRUReplacer(list, map);

        BufferSlot slot1 = new BufferSlot(1, new HeapPage(1));
        BufferSlot slot2 = new BufferSlot(2, new HeapPage(2));
        BufferSlot slot3 = new BufferSlot(3, new HeapPage(3));

        lru.push(slot1);
        lru.push(slot2);
        lru.push(slot3);

        // slot1 should be LRU
        BufferSlot victim = lru.pickVictim();
        assertEquals(1, victim.getPageId());

        // slot2 should be next LRU
        victim = lru.pickVictim();
        assertEquals(2, victim.getPageId());
    }

    @Test
    void push_existingSlot_movesToFront() {
        LinkedList<BufferSlot> list = new LinkedList<>();
        Map<Integer, BufferSlot> map = new HashMap<>();
        LRUReplacer lru = new LRUReplacer(list, map);

        BufferSlot slot1 = new BufferSlot(1, new HeapPage(1));
        BufferSlot slot2 = new BufferSlot(2, new HeapPage(2));

        lru.push(slot1);
        lru.push(slot2);
        lru.push(slot1); // Push again - should move to front

        // slot2 should be LRU now
        BufferSlot victim = lru.pickVictim();
        assertEquals(2, victim.getPageId());
    }

    @Test
    void delete_removesSlot() {
        LinkedList<BufferSlot> list = new LinkedList<>();
        Map<Integer, BufferSlot> map = new HashMap<>();
        LRUReplacer lru = new LRUReplacer(list, map);

        BufferSlot slot1 = new BufferSlot(1, new HeapPage(1));
        BufferSlot slot2 = new BufferSlot(2, new HeapPage(2));

        lru.push(slot1);
        lru.push(slot2);
        lru.delete(1);

        // Only slot2 should remain
        assertEquals(1, list.size());
        assertEquals(2, list.getFirst().getPageId());
    }

    @Test
    void pickVictim_empty_returnsNull() {
        LinkedList<BufferSlot> list = new LinkedList<>();
        Map<Integer, BufferSlot> map = new HashMap<>();
        LRUReplacer lru = new LRUReplacer(list, map);

        assertNull(lru.pickVictim());
    }
}