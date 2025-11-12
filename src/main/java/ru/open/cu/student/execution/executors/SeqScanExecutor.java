package ru.open.cu.student.execution.executors;

import ru.open.cu.student.catalog.model.TableDefinition;
import ru.open.cu.student.memory.buffer.BufferPoolManager;
import ru.open.cu.student.memory.page.HeapPage;

/**
 * Исполнитель последовательного сканирования таблицы.
 */
public class SeqScanExecutor implements Executor {
    private final BufferPoolManager bufferPool;
    private final String tableName;
    private int currentPageId;
    private int currentRowIndex;
    private boolean isOpen;

    public SeqScanExecutor(BufferPoolManager bufferPool, TableDefinition tableDefinition) {
        this.bufferPool = bufferPool;
        this.tableName = tableDefinition.getName();
    }

    @Override
    public void open() {
        currentPageId = 0;
        currentRowIndex = 0;
        isOpen = true;
    }

    @Override
    public Object next() {
        if (!isOpen) return null;

        while (true) {
            var bufferSlot = bufferPool.getPage(currentPageId);
            if (bufferSlot == null) return null; // Страницы закончились

            HeapPage page = (HeapPage) bufferSlot.getPage();

            // Если есть строки на текущей странице
            if (currentRowIndex < page.size()) {
                byte[] rowData = page.read(currentRowIndex);
                currentRowIndex++;
                return rowData; // Возвращаем сырые байты строки
            } else {
                // Переходим к следующей странице
                currentPageId++;
                currentRowIndex = 0;
            }
        }
    }

    @Override
    public void close() {
        isOpen = false;
        currentPageId = 0;
        currentRowIndex = 0;
    }
}