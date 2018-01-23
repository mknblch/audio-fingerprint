package de.mknblch.audiofingerprint.common;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 * @author mknblch
 */
public class FloatRing {

    public final float[] data;
    public final int length;

    private int writeOffset = 0;
    private int readOffset = 0;

    public FloatRing(int length) {
        this.data = new float[length];
        this.length = length;
    }

    public FloatRing put(float value) {
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

    public float get(int index) {
        return data[index % length];
    }

    public Iterator<Cursor> iterator() {
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
    public float tail() {
        if (writeOffset - readOffset <= 0) {
            return 0;
        }
        return data[readOffset % length];
    }

    public float at(double p) {
        if (writeOffset - readOffset <= 0) {
            return 0;
        }
        final int at = (readOffset + (int) (p * (writeOffset - readOffset - 1))) % length;
        return data[at];
    }


    public float mid() {
        if (writeOffset - readOffset <= 0) {
            return 0;
        }
        return data[(readOffset + (writeOffset - readOffset - 1) / 2) % length];
    }

    public float pop() {
        return data[readOffset++];
    }

    public boolean isEmpty() {
        return writeOffset - readOffset <= 0;
    }

    /**
     * return newest element in the queue
     */
    public float head() {
        if (writeOffset == 0) {
            return 0;
        }
        return data[(writeOffset - 1) % length];
    }

    public void forEach(Consumer<Float> consumer) {
        for (int i = readOffset; i < writeOffset; i++) {
            consumer.accept(data[i % length]);
        }
    }

    public void forEachBut(int except, Consumer<Float> consumer) {
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
        for (float datum : data) {
            if (builder.length() > 0) {
                builder.append(" ");
            }
            builder.append(datum);
        }
        return builder.toString();
    }

    public float[] getData() {
        return data;
    }

    public float[] export() {
        return Arrays.copyOf(data, length);
    }

    public static class Cursor {

        public float value;
        public int index;
    }

    private class Ascending implements Iterator<Cursor> {

        private int offset = readOffset;

        private Cursor cursor = new Cursor();

        @Override
        public boolean hasNext() {
            return offset < writeOffset;
        }

        @Override
        public Cursor next() {
            cursor.index = (offset++) % length;
            cursor.value = data[cursor.index];
            return cursor;
        }
    }

}
