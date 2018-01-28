package de.mknblch.audiofp.common;

import java.util.Arrays;

/**
 * insert sort based priority queue for small element counts
 *
 * @author mknblch
 */
public abstract class PriorityQueue<T> {

    private final float[] scores;
    private final T[] data;
    private final int size;
    private int offset = 0;

    public PriorityQueue(T[] data) {
        this.data = data;
        this.size = data.length - 1;
        scores = new float[data.length];
    }

    public int getSize() {
        return Math.min(offset, size);
    }

    public int getCapacity() {
        return size;
    }

    public abstract boolean eval(float a, float b);

    public float[] getScores() {
        return Arrays.copyOf(scores, Math.min(size - 1, offset));
    }

    public T[] getData() {
        return Arrays.copyOf(data, Math.min(size - 1, offset));
    }

    public PriorityQueue add(float score, T value) {
        int j = Math.min(size - 1, offset++);
        while (j > 0 && eval(scores[j - 1], score)) {
            scores[j] = scores[j - 1];
            data[j] = data[j - 1];
            j--;
        }
        scores[j] = score;
        data[j] = value;
        return this;
    }

    public PriorityQueue reset() {
        return reset(0.0f);
    }

    public PriorityQueue reset(float defaultScores) {
        Arrays.fill(scores, defaultScores);
        return this;
    }

    public static <T> PriorityQueue<T> min(T[] data) {
        return new PriorityQueue<T>(data) {
            @Override
            public boolean eval(float a, float b) {
                return a > b;
            }
        };
    }

    public static <T> PriorityQueue<T> max(T[] data) {
        return new PriorityQueue<T>(data) {
            @Override
            public boolean eval(float a, float b) {
                return a < b;
            }
        };
    }

    public static <T> void sort(float[] scores, T[] data) {
        for (int i = 1; i < scores.length; i++) {
            int j = i;
            final float score = scores[i];
            final T value = data[i];
            while (j > 0 && (scores[j - 1] < score)) {
                scores[j] = scores[j - 1];
                data[j] = data[j - 1];
                j--;
            }
            scores[j] = score;
            data[j] = value;
        }
    }

    public static <T> void sort(float[] scores, int[] data) {
        for (int i = 1; i < scores.length; i++) {
            int j = i;
            final float score = scores[i];
            final int value = data[i];
            while (j > 0 && (scores[j - 1] < score)) {
                scores[j] = scores[j - 1];
                data[j] = data[j - 1];
                j--;
            }
            scores[j] = score;
            data[j] = value;
        }
    }
}
