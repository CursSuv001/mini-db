package ru.open.cu.student.memory.page;

import java.util.List;

public interface Page {
    byte[] bytes();

    int getPageId();

    int size();

    boolean isValid();

    byte[] read(int index);

    void write(byte[] data);

}
