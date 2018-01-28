package de.mknblch.audiofp.processor;

import com.tagtraum.jipes.AbstractSignalProcessor;
import de.mknblch.audiofp.Hash;
import de.mknblch.audiofp.common.ImageBuffer;
import de.mknblch.audiofp.common.PriorityQueue;
import de.mknblch.audiofp.common.Ring;
import de.mknblch.audiofp.Feature;

import java.io.IOException;
import java.util.BitSet;
import java.util.Iterator;
import java.util.function.IntBinaryOperator;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Feature Processor calculates hashes based on maxima.
 *
 * The processor aggregates up to 32 frames and builds hashes by taking all
 * maximum bins and connecting them to
 *
 * @author mknblch
 */
public class Fingerprint extends FeatureQueue<Feature> {

    public static final int MAX_FRAMES = 32;

    public interface ScoreFunction {

        float score(int i0, int i1, int f0, int f1, float vf0, float vf1);
    }

    public static final String ID = Fingerprint.class.getName();

    private final int maxPerFrame;
    private final int maxPerReference;
    private final long minDtInMs;
    private final long maxDtInMs;
    private ScoreFunction scoreFunction = (int i0, int i1, int f0, int f1, float vf0, float vf1) -> vf0;

    private PriorityQueue<Hash> heap;

    private IntBinaryOperator windowSizeFunction = (index, samples) -> 5; // samples / 24;

    public Fingerprint(int maxPerFrame, int maxPerReference, long minDtInMs, long maxDtInMs) {
        this(ID, maxPerFrame, maxPerReference, minDtInMs, maxDtInMs);
    }
    public Fingerprint(Object id, int maxPerFrame, int maxPerReference, long minDtInMs, long maxDtInMs) {
        super(id, MAX_FRAMES);
        this.maxPerFrame = maxPerFrame;
        this.maxPerReference = maxPerReference;
        this.minDtInMs = minDtInMs;
        this.maxDtInMs = maxDtInMs;
    }

    public Fingerprint withScoreFunction(ScoreFunction scoreFunction, boolean scoreMax) {
        this.scoreFunction = scoreFunction;
        this.heap = scoreMax ?
                PriorityQueue.max(new Hash[maxPerFrame]) :
                PriorityQueue.min(new Hash[maxPerFrame]);
        return this;
    }

    public Fingerprint withWindowSizeFunction(IntBinaryOperator windowFunction) {
        this.windowSizeFunction = windowFunction;
        return this;
    }

    @Override
    public Feature process(Ring<Feature> queue) {
        if (!queue.isSaturated()) {
            return null;
        }

        final Feature reference = queue.tail();
        final int[] refBins = reference.maxBins;
        final float[] refValues = reference.maxValues;
        heap.reset();
        final Iterator<Feature> it = queue.iterator();
        // skip tail
        it.next();
        for (; it.hasNext(); ) {
            final Feature feature = it.next();
            final long dt = (feature.getTimestamp() - reference.getTimestamp());
            if (dt < minDtInMs || dt > maxDtInMs) {
                continue;
            }
            final int[] bins = feature.maxBins;
            final float[] values = feature.maxValues;
            for (int i = 0; i < refBins.length; i++) {
                for (int refCount = 0, j = 0; j < bins.length && refCount < maxPerReference; j++) {
                    final int df = Math.abs(refBins[i] - bins[j]);
                    if (df > windowSizeFunction.applyAsInt(refBins[i], reference.getNumberOfSamples())) {
                        continue;
                    }
                    heap.add(
                            scoreFunction.score(reference.index, feature.index,
                                    refBins[i], bins[j],
                                    refValues[i], values[j]),
                            new Hash(reference.getTimestamp(),
                                    reference.index,
                                    feature.index,
                                    refBins[i],
                                    bins[j]));
                    refCount++;
                }
            }
        }
        return reference.withHashes(heap.getData());
    }

    @Override
    public void flush() throws IOException {
        final int s = queue.midIndex();
        final int t = queue.headIndex();
        final int capacity = queue.capacity();

        for (int i = s + 1; i <= t; i++) {
            signalProcessorSupport.process(queue.get(i % capacity));
        }
        super.flush();
    }

    private static final int COLOR = ImageBuffer.rgb(230, 150, 30);

    public static final ImageAggregator.DrawFunction DRAW_FUNCTION = (image, features) -> {

        for (Feature feature : features) {
            for (Hash hash : feature.hashes) {
                image.drawLine(hash.ts, image.height - hash.fs - 1, hash.tt, image.height - hash.ft - 1, COLOR);
            }
        }
    };

    public static class Info extends AbstractSignalProcessor<Feature, Feature> {

        public static final String ID = Info.class.getName();

        private final BitSet set;
        private int numHashes = 0;

        public Info() {
            super(ID);
            set = new BitSet();
        }

        public int getNumHashes() {
            return numHashes;
        }

        public int getNumUniqueHashes() {
            return set.cardinality();
        }

        @Override
        protected Feature processNext(Feature feature) throws IOException {
            final Hash[] hashes = feature.hashes;
            for (Hash hash : hashes) {
                set.set(hash.hash());
            }
            numHashes += hashes.length;
            return feature;
        }
    }
}
