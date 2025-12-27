package ru.open.cu.student.memory.replacer;

import ru.open.cu.student.memory.model.BufferSlot;

import java.util.LinkedList;
import java.util.Map;

public class LRUReplacer implements Replacer{
    private final LinkedList<BufferSlot> lruList;
    private final Map<Integer, BufferSlot> slotMap;

    public LRUReplacer(LinkedList<BufferSlot> lruList, Map<Integer, BufferSlot> slotMap) {
        this.lruList = lruList;
        this.slotMap = slotMap;
    }

    @Override
    public void push(BufferSlot bufferSlot) {
        if (bufferSlot.isPinned()) return;

        int pageId = bufferSlot.getPageId();

        if (slotMap.containsKey(pageId)) lruList.remove(bufferSlot);

        lruList.addFirst(bufferSlot);
        slotMap.put(pageId, bufferSlot);
    }

    @Override
    public void delete(int pageId) {
        if (pageId <= 0) throw new IllegalArgumentException("Invalid page id: " + pageId);

        BufferSlot slot = slotMap.remove(pageId);
        if (slot != null) lruList.remove(slot);
    }

    @Override
    public BufferSlot pickVictim() {
        if (lruList.isEmpty()) return null;

        BufferSlot victim = lruList.removeLast();
        slotMap.remove(victim.getPageId());
        return victim;
    }
}