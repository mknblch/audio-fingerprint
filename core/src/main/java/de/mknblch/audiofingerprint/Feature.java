package de.mknblch.audiofingerprint;

import com.tagtraum.jipes.SignalProcessor;
import com.tagtraum.jipes.SignalProcessorSupport;
import com.tagtraum.jipes.audio.AudioSpectrum;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author mknblch
 */
public final class Feature implements AudioSpectrum {

    public static final String ID = "Feature";

    public final int index;
    public final AudioSpectrum buffer;

    public int[] maxBins = new int[0];
    public float[] maxValues = new float[0];
    public Hash[] hashes = new Hash[0];

    private Feature(int index, AudioSpectrum buffer) {
        this.index = index;
        this.buffer = buffer;
    }

    public Feature withMaxBins(int[] maxBins) {
        this.maxBins = maxBins;
        return this;
    }

    public Feature withMaxValues(float[] maxValues) {
        this.maxValues = maxValues;
        return this;
    }

    public Feature withHashes(Hash[] hashes) {
        this.hashes = hashes;
        return this;
    }

    @Override
    public long getTimestamp() {
        return buffer.getTimestamp();
    }

    @Override
    public long getTimestamp(TimeUnit timeUnit) {
        return buffer.getTimestamp(timeUnit);
    }

    @Override
    public int getFrameNumber() {
        return buffer.getFrameNumber();
    }

    @Override
    public AudioFormat getAudioFormat() {
        return buffer.getAudioFormat();
    }

    @Override
    public float[] getData() {
        return buffer.getData();
    }

    @Override
    public float[] getRealData() {
        return buffer.getRealData();
    }

    @Override
    public float[] getImaginaryData() {
        return buffer.getImaginaryData();
    }

    @Override
    public float[] getPowers() {
        return buffer.getPowers();
    }

    @Override
    public float[] getMagnitudes() {
        return buffer.getMagnitudes();
    }

    @Override
    public int getNumberOfSamples() {
        return buffer.getNumberOfSamples();
    }

    @Override
    public float[] getFrequencies() {
        return buffer.getFrequencies();
    }

    @Override
    public float getFrequency(int bin) {
        return buffer.getFrequency(bin);
    }

    @Override
    public int getBin(float frequency) {
        return buffer.getBin(frequency);
    }

    @Override
    public AudioSpectrum derive(float[] real, float[] imaginary) {
        return buffer.derive(real, imaginary);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    @Override
    public String toString() {
        return "Feature{" +
                "index=" + index +
                '}';
    }

    public static <S extends AudioSpectrum> SignalProcessor<S, Feature> wrapper(boolean clone) {
        return new FeatureWrapperProcessor<>(ID, clone);
    }

    private static class FeatureWrapperProcessor<S extends AudioSpectrum> implements SignalProcessor<S, Feature> {

        private final SignalProcessorSupport<Feature> support;
        private final Object id;
        private final boolean clone;
        private Feature feature;
        private int offset = 0;

        private FeatureWrapperProcessor(Object id, boolean clone) {
            this.id = id;
            this.clone = clone;
            support = new SignalProcessorSupport<>();
        }

        @Override
        public void process(S s) throws IOException {
            if (clone) {
                try {
                    //noinspection unchecked
                    emit(new Feature(offset++, (AudioSpectrum) s.clone()));
                } catch (CloneNotSupportedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                emit(new Feature(offset++, s));
            }
        }

        @Override
        public void flush() throws IOException {
            support.flush();
        }

        @Override
        public Object getId() {
            return id;
        }

        @Override
        public Feature getOutput() throws IOException {
            return feature;
        }

        @Override
        public <O2> SignalProcessor<Feature, O2> connectTo(SignalProcessor<Feature, O2> signalProcessor) {
            return support.connectTo(signalProcessor);
        }

        @Override
        public <O2> SignalProcessor<Feature, O2> disconnectFrom(SignalProcessor<Feature, O2> signalProcessor) {
            return support.disconnectFrom(signalProcessor);
        }

        @Override
        public SignalProcessor<Feature, ?>[] getConnectedProcessors() {
            return support.getConnectedProcessors();
        }

        protected void emit(Feature feature) throws IOException {
            this.feature = feature;
            support.process(feature);
        }
    }
}
