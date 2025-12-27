package ru.open.cu.student.memory.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.open.cu.student.memory.buffer.DefaultBufferPoolManager;
import ru.open.cu.student.memory.manager.HeapPageFileManager;
import ru.open.cu.student.memory.page.HeapPage;
import ru.open.cu.student.memory.page.Page;
import ru.open.cu.student.memory.replacer.ClockReplacer;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class DefaultDirtyPageWriterTest {

    @Test
    void backgroundWriter_flushesDirtyPages(@TempDir Path tempDir) throws InterruptedException {
        Path dataFile = tempDir.resolve("test.db");

        // Сначала создаем файл с одной страницей
        HeapPageFileManager fm = new HeapPageFileManager();
        Page initialPage = new HeapPage(0);
        fm.write(initialPage, dataFile);

        DefaultBufferPoolManager bpm = new DefaultBufferPoolManager(10, fm,
                new ClockReplacer(), new ClockReplacer(), dataFile);
        DefaultDirtyPageWriter writer = new DefaultDirtyPageWriter(bpm);

        // Загружаем страницу и делаем ее dirty
        bpm.getPage(0);
        Page newPage = new HeapPage(0);
        newPage.write(new byte[]{1, 2, 3});
        bpm.updatePage(0, newPage);

        assertFalse(bpm.getDirtyPages().isEmpty());

        // Запускаем writer и ждем
        writer.startBackgroundWriter();
        TimeUnit.MILLISECONDS.sleep(1200);

        // Останавливаем поток чтобы не мешал другим тестам
        Thread.sleep(100);

        // После сброса страница должна быть чистой
        assertTrue(bpm.getDirtyPages().isEmpty());
    }

    @Test
    void checkPointer_flushesAllPages(@TempDir Path tempDir) throws InterruptedException {
        Path dataFile = tempDir.resolve("test.db");

        // Сначала создаем файл с тремя страницами
        HeapPageFileManager fm = new HeapPageFileManager();
        for (int i = 0; i < 3; i++) {
            Page initialPage = new HeapPage(i);
            fm.write(initialPage, dataFile);
        }

        DefaultBufferPoolManager bpm = new DefaultBufferPoolManager(10, fm,
                new ClockReplacer(), new ClockReplacer(), dataFile);
        DefaultDirtyPageWriter writer = new DefaultDirtyPageWriter(bpm);

        // Загружаем страницы и делаем их dirty
        for (int i = 0; i < 3; i++) {
            bpm.getPage(i);
            Page newPage = new HeapPage(i);
            newPage.write(new byte[]{(byte) i});
            bpm.updatePage(i, newPage);
        }

        assertEquals(3, bpm.getDirtyPages().size());

        // Запускаем checkpointer и ждем
        writer.startCheckPointer();
        TimeUnit.MILLISECONDS.sleep(10500);

        // Останавливаем поток
        Thread.sleep(100);

        assertTrue(bpm.getDirtyPages().isEmpty());
    }

    @Test
    void writer_respectsMaxDirtyPagesLimit(@TempDir Path tempDir) throws InterruptedException {
        Path dataFile = tempDir.resolve("test.db");

        // Создаем файл со многими страницами
        HeapPageFileManager fm = new HeapPageFileManager();
        for (int i = 0; i < 150; i++) {
            Page initialPage = new HeapPage(i);
            fm.write(initialPage, dataFile);
        }

        DefaultBufferPoolManager bpm = new DefaultBufferPoolManager(200, fm,
                new ClockReplacer(), new ClockReplacer(), dataFile);
        DefaultDirtyPageWriter writer = new DefaultDirtyPageWriter(bpm);

        // Делаем много dirty страниц
        for (int i = 0; i < 150; i++) {
            bpm.getPage(i);
            Page newPage = new HeapPage(i);
            newPage.write(new byte[]{(byte) i});
            bpm.updatePage(i, newPage);
        }

        assertEquals(150, bpm.getDirtyPages().size());

        writer.startBackgroundWriter();
        TimeUnit.MILLISECONDS.sleep(1200);
        Thread.sleep(100);

        // Должно остаться 50 dirty страниц (150 - maxDirtyPages 100)
        assertTrue(bpm.getDirtyPages().size() <= 50);
    }
}