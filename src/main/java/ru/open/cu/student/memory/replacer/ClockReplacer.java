package ru.open.cu.student.memory.replacer;

import ru.open.cu.student.memory.model.BufferSlot;

import java.util.ArrayList;
import java.util.List;

public class ClockReplacer implements Replacer{

    private static class ClockEntry {
        BufferSlot bufferSlot;
        boolean ref;

        ClockEntry(BufferSlot bufferSlot) {
            this.bufferSlot = bufferSlot;
            this.ref = true;
        }
    }

    private final List<ClockEntry> entries;
    private int hand;

    public ClockReplacer() {
        this.entries = new ArrayList<>();
        this.hand = 0;
    }


    @Override
    public void push(BufferSlot bufferSlot) {
        if (bufferSlot.isPinned()) {
            return;
        }

        for (ClockEntry entry : entries) {
            if (entry.bufferSlot.getPageId() == bufferSlot.getPageId()) {
                entry.ref = true;
                return;
            }
        }
        delete(bufferSlot.getPageId());
        entries.add(new ClockEntry(bufferSlot));
    }

    @Override
    public void delete(int pageId) {
        entries.removeIf(entry -> entry.bufferSlot.getPageId() == pageId);
    }

    @Override
    public BufferSlot pickVictim() {
        int checked = 0;
        while (!entries.isEmpty() && checked < entries.size()) {
            if (hand >= entries.size()) hand = 0;

            ClockEntry entry = entries.get(hand);

            if (entry.bufferSlot.isPinned()) {
                entries.remove(hand);
                checked = 0;
                continue;
            }

            if (entry.ref) {
                entry.ref = false;
                hand = (hand + 1) % entries.size();
            } else {
                BufferSlot victim = entry.bufferSlot;
                entries.remove(hand);
                return victim;
            }
        }
        return null;
    }
}