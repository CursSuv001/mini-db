package ru.open.cu.student.memory.io;

import ru.open.cu.student.memory.buffer.DefaultBufferPoolManager;

public class DefaultDirtyPageWriter implements DirtyPageWriter{
    private final DefaultBufferPoolManager bpm;
    private final int checkpointerInterval = 10_000;
    private final int writerInterval = 1_000;
    private final int maxDirtyPages = 100;

    public DefaultDirtyPageWriter(DefaultBufferPoolManager bpm) {
        this.bpm = bpm;
    }

    @Override
    public void startBackgroundWriter() {
        new Thread(() -> {
            while (true) {
                try {
                    var dirtyPages = bpm.getDirtyPages();
                    for (int i = 0; i < Math.min(dirtyPages.size(), maxDirtyPages); i++) {
                        bpm.flushPage(dirtyPages.get(i).getPageId());
                    }
                    Thread.sleep(writerInterval);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        }).start();

    }

    @Override
    public void startCheckPointer() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(checkpointerInterval);
                    bpm.flushAllPages();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}