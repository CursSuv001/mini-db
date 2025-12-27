package ru.open.cu.student.memory.buffer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.open.cu.student.memory.manager.HeapPageFileManager;
import ru.open.cu.student.memory.manager.PageFileManager;
import ru.open.cu.student.memory.model.BufferSlot;
import ru.open.cu.student.memory.page.HeapPage;
import ru.open.cu.student.memory.page.Page;
import ru.open.cu.student.memory.replacer.ClockReplacer;
import ru.open.cu.student.memory.replacer.Replacer;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DefaultBufferPoolManagerTest {

    @Test
    void getPage_then_update_roundTrip(@TempDir Path tempDir) {
        Path dataFile = tempDir.resolve("test.db");
        PageFileManager fm = new HeapPageFileManager();
        Replacer replacer = new ClockReplacer();
        DefaultBufferPoolManager bpm = new DefaultBufferPoolManager(10, fm, replacer, replacer, dataFile);

        // Сначала создаем страницу на диске
        Page initialPage = new HeapPage(0);
        initialPage.write(new byte[]{1, 2, 3});
        fm.write(initialPage, dataFile);

        BufferSlot slot = bpm.getPage(0);
        assertNotNull(slot);
        assertEquals(0, slot.getPageId());
        assertTrue(slot.getPage().isValid());

        Page newPage = new HeapPage(0);
        newPage.write(new byte[]{4, 5, 6});
        bpm.updatePage(0, newPage);

        assertTrue(slot.isDirty());
    }

    @Test
    void pin_then_unpin_works(@TempDir Path tempDir) {
        Path dataFile = tempDir.resolve("test.db");
        PageFileManager fm = new HeapPageFileManager();
        Replacer replacer = new ClockReplacer();
        DefaultBufferPoolManager bpm = new DefaultBufferPoolManager(10, fm, replacer, replacer, dataFile);

        // Сначала создаем страницу на диске
        Page initialPage = new HeapPage(0);
        fm.write(initialPage, dataFile);

        bpm.getPage(0);
        bpm.pinPage(0);

        BufferSlot slot = bpm.getPage(0);
        assertTrue(slot.isPinned());

        bpm.unpinPage(0);
        assertFalse(slot.isPinned());
    }

    @Test
    void flushPage_clearsDirtyFlag(@TempDir Path tempDir) {
        Path dataFile = tempDir.resolve("test.db");
        PageFileManager fm = new HeapPageFileManager();
        Replacer replacer = new ClockReplacer();
        DefaultBufferPoolManager bpm = new DefaultBufferPoolManager(10, fm, replacer, replacer, dataFile);

        // Сначала создаем страницу на диске
        Page initialPage = new HeapPage(0);
        fm.write(initialPage, dataFile);

        bpm.getPage(0);
        Page newPage = new HeapPage(0);
        newPage.write(new byte[]{1, 2, 3});
        bpm.updatePage(0, newPage);

        assertTrue(bpm.getDirtyPages().get(0).isDirty());
        bpm.flushPage(0);
        assertTrue(bpm.getDirtyPages().isEmpty());
    }

    @Test
    void flushAllPages_clearsAllDirty(@TempDir Path tempDir) {
        Path dataFile = tempDir.resolve("test.db");
        PageFileManager fm = new HeapPageFileManager();
        Replacer replacer = new ClockReplacer();
        DefaultBufferPoolManager bpm = new DefaultBufferPoolManager(10, fm, replacer, replacer, dataFile);

        // Сначала создаем страницы на диске
        for (int i = 0; i < 3; i++) {
            Page initialPage = new HeapPage(i);
            fm.write(initialPage, dataFile);
        }

        for (int i = 0; i < 3; i++) {
            bpm.getPage(i);
            Page newPage = new HeapPage(i);
            newPage.write(new byte[]{(byte) i});
            bpm.updatePage(i, newPage);
        }

        assertEquals(3, bpm.getDirtyPages().size());
        bpm.flushAllPages();
        assertTrue(bpm.getDirtyPages().isEmpty());
    }

    @Test
    void updatePage_notInBuffer_throws(@TempDir Path tempDir) {
        Path dataFile = tempDir.resolve("test.db");
        PageFileManager fm = new HeapPageFileManager();
        Replacer replacer = new ClockReplacer();
        DefaultBufferPoolManager bpm = new DefaultBufferPoolManager(10, fm, replacer, replacer, dataFile);

        Page newPage = new HeapPage(999);
        assertThrows(IllegalArgumentException.class, () -> bpm.updatePage(999, newPage));
    }
}