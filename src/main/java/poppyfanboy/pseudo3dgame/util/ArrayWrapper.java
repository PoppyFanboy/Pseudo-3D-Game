package poppyfanboy.pseudo3dgame.util;

import java.util.AbstractList;

public class ArrayWrapper<T> extends AbstractList<T> {
    private T[] array;
    private int startIndex, endIndex;

    public ArrayWrapper(T[] array) {
        this.array = array;
        startIndex = 0;
        endIndex = array.length;
    }

    public ArrayWrapper(T[] array, int startIndex, int endIndex) {
        if (startIndex < 0 || startIndex >= array.length
                || endIndex < 0 || endIndex > array.length) {
            throw new IllegalArgumentException(String.format(
                    "Indices are out of range: iStart = %d, iEnd = %d,"
                    + " len(array) = %d", startIndex, endIndex, array.length));
        }
        this.array = array;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    @Override
    public T get(int index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException(index);
        }
        return array[startIndex + index];
    }

    @Override
    public int size() {
        return endIndex - startIndex;
    }
}
