package de.mknblch.audiofp.processor;

import de.mknblch.audiofp.Feature;
import de.mknblch.audiofp.common.ImageBuffer;
import de.mknblch.audiofp.common.Ring;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.IntBinaryOperator;

/**
 * Feature Processor for finding local maximum in magnitudes with fixed time window size and
 * a dynamic frequency window size.
 *
 * The freq. window is calculated using an IntBinaryOperator. The window function automatically
 * adjusts its offsets at the borders to keep its size and avoid padding.
 *
 * @author mknblch
 */
public class LocalMaximum extends AbstractFeatureProcessor {

    public static String ID = LocalMaximum.class.getName();

    private final Ring<Feature> bufferQueue;
    private IntBinaryOperator windowFunction = (index, samples) -> samples / 24;

    private final int[] bins;
    private final float[] values;

    public LocalMaximum(int frames, int max) {
        bufferQueue = new Ring<>(new Feature[frames]);
        bins = new int[max];
        values = new float[max];
    }

    /**
     *
     * @param windowFunction (index, sampled)
     * @return
     */
    public LocalMaximum withWindowFunction(IntBinaryOperator windowFunction) {
        this.windowFunction = windowFunction;
        return this;
    }

    private boolean feed(Feature audioBuffer) {
        bufferQueue.put(audioBuffer);
        return bufferQueue.isSaturated();
    }

    @Override
    public void process(Feature feature) throws IOException {
        if (feed(feature)) {
            emit(compute());
        } else if (feature.index < bufferQueue.capacity() / 2) {
            emit(feature);
        }
    }

    @Override
    public void flush() throws IOException {
        final int s = bufferQueue.midIndex();
        final int t = bufferQueue.headIndex();
        final int capacity = bufferQueue.capacity();

        for (int i = s + 1; i <= t; i++) {
            emit(bufferQueue.get(i % capacity));
        }
        super.flush();
    }

    private Feature compute() {
        final Feature[] features = bufferQueue.getData();
        final int referenceIndex = bufferQueue.midIndex() % bufferQueue.capacity();
        final Feature reference = features[referenceIndex];
        final float[] data = reference.getMagnitudes();
        int offset = 0; // element counter
        for (int i = 0; i < data.length && offset < bins.length; ) {
            final int window = Math.max(windowFunction.applyAsInt(i, data.length), 3);
            final float value = data[i];
            if (isMaximum(features, referenceIndex, i, data.length, window, value)) {
                values[offset] = value;
                bins[offset++] = i;
                i += window / 2;
            } else {
                i++;
            }
        }
        insertSort(values, bins);
        final int[] copyBins = Arrays.copyOf(bins, offset);
        final float[] copyValues = Arrays.copyOf(values, offset);
        return reference
                .withMaxBins(copyBins)
                .withMaxValues(copyValues);
    }

    private static boolean isMaximum(Feature[] buffers, int ref, int y, int length, int window, double max) {
        final int w2 = window / 2;
        final int db = -Math.min(y - w2, 0);
        final int dt = Math.max(y + w2, length) - length;
        for (int x = 0; x < buffers.length; x++) {
            final float[] data = buffers[x].getMagnitudes();
            final int bottom = Math.max(0, y - w2) - dt;
            final int top = Math.min(data.length, y + w2) + db;
            for (int j = bottom; j < top; j++) {
                if (x == ref && j == y) {
                    continue;
                }
                if (data[j] >= max) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void insertSort(float[] scores, int[] data) {
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

    /**
     * Draw function. Renders maxima as red dots
     */
    public static ImageAggregator.DrawFunction DRAW_FUNC = (image, features) -> {
        final int rgb = ImageBuffer.rgb(255, 50, 50);

        for (Feature feature : features) {
            final int[] maxBins = feature.maxBins;
            for (int maxBin : maxBins) {
                image.setPixel(feature.index, image.height - maxBin - 1, rgb);
            }
        }
    };
}
