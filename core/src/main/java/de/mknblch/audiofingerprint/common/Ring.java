package de.mknblch.audiofingerprint.common;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 * @author mknblch
 */
public class Ring<T> {

    public final T[] data;
    public final int length;

    private int writeOffset = 0;
    private int readOffset = 0;

    public Ring(T[] data) {
        this.data = data;
        length = data.length;
    }

    public Ring<T> put(T value) {
        if (writeOffset - readOffset >= length) {
            readOffset = (writeOffset - length) + 1;
        }
        data[writeOffset++ % length] = value;
        return this;
    }

    public int capacity() {
        return data.length;
    }

    public int size() {
        return isSaturated() ? length : writeOffset;
    }

    public boolean isSaturated() {
        return writeOffset - readOffset >= length;
    }

    public T get(int index) {
        return data[index % length];
    }

    public Iterator<T> iterator() {
        return new Ascending();
    }

    public int headIndex() {
        if (writeOffset - readOffset <= 0) {
            return -1;
        }
        return (writeOffset - 1);
    }

    public int tailIndex() {
        if (writeOffset - readOffset <= 0) {
            return -1;
        }
        return readOffset;
    }

    public int midIndex() {
        if (writeOffset - readOffset <= 0) {
            return -1;
        }
        return (readOffset + (writeOffset - readOffset - 1) / 2);
    }

    public int atIndex(double p) {
        if (writeOffset - readOffset <= 0) {
            return -1;
        }
        return (readOffset + (int) (p * (writeOffset - readOffset - 1)));
    }

    /**
     * return oldest element in the queue
     */
    public T tail() {
        if (writeOffset - readOffset <= 0) {
            return null;
        }
        return data[readOffset % length];
    }

    public T at(double p) {
        if (writeOffset - readOffset <= 0) {
            return null;
        }
        final int at = (readOffset + (int) (p * (writeOffset - readOffset - 1))) % length;
        return data[at];
    }


    public T mid() {
        if (writeOffset - readOffset <= 0) {
            return null;
        }
        return data[(readOffset + (writeOffset - readOffset - 1) / 2) % length];
    }

    public T pop() {
        return data[readOffset++];
    }

    public boolean isEmpty() {
        return writeOffset - readOffset <= 0;
    }

    /**
     * return newest element in the queue
     */
    public T head() {
        if (writeOffset == 0) {
            return null;
        }
        return data[(writeOffset - 1) % length];
    }

    public void forEach(Consumer<T> consumer) {
        for (int i = readOffset; i < writeOffset; i++) {
            consumer.accept(data[i % length]);
        }
    }

    public void forEachBut(int except, Consumer<T> consumer) {
        for (int i = readOffset; i < writeOffset; i++) {
            if (i == except) {
                continue;
            }
            consumer.accept(get(i));
        }
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        for (T datum : data) {
            if (builder.length() > 0) {
                builder.append(" ");
            }
            builder.append(datum);
        }
        return builder.toString();
    }

    public T[] getData() {
        return data;
    }

    public T[] export() {
        return Arrays.copyOf(data, length);
    }

    public void release() {
        Arrays.fill(data, null);
    }

    private class Ascending implements Iterator<T> {

        private int offset = readOffset;

        @Override
        public boolean hasNext() {
            return offset < writeOffset;
        }

        @Override
        public T next() {
            return data[(offset++) % length];
        }
    }

}
