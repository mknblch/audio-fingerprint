package de.mknblch.audiofp.processor;

import com.tagtraum.jipes.AbstractSignalProcessor;
import com.tagtraum.jipes.audio.AudioSpectrum;

import java.io.IOException;
import java.util.Arrays;

/**
 * Cut off
 * @author mknblch
 */
public class Cut {

    public final static String ID = Cut.class.getName();

    public static class BinCut extends AbstractSignalProcessor<AudioSpectrum, AudioSpectrum> {

        private final int bottomBin;

        private final int topBin;
        public BinCut(int bottomBin, int topBin) {
            this(ID, bottomBin, topBin);
        }

        public BinCut(Object id, int bottomBin, int topBin) {
            super(id);
            this.bottomBin = bottomBin;
            this.topBin = topBin;
        }

        @Override
        protected AudioSpectrum processNext(AudioSpectrum input) throws IOException {
            final float[] realData = input.getRealData();
            final float[] imaginaryData = input.getImaginaryData();
            return input.derive(
                    realData == null ? null : Arrays.copyOfRange(realData, Math.max(0, bottomBin), Math.min(topBin, realData.length)),
                    imaginaryData == null ? null : Arrays.copyOfRange(imaginaryData, Math.max(0, bottomBin), Math.min(topBin, imaginaryData.length))
            );
        }
    }

    public static final class FrequencyCut extends AbstractSignalProcessor<AudioSpectrum, AudioSpectrum> {

        private final float bottomFrequency;
        private final float topFrequency;

        public FrequencyCut(float bottomFrequency, float topFrequency) {
            this(ID, bottomFrequency, topFrequency);
        }

        public FrequencyCut(Object id, float bottomFrequency, float topFrequency) {
            super(id);
            this.bottomFrequency = bottomFrequency;
            this.topFrequency = topFrequency;
        }

        @Override
        protected AudioSpectrum processNext(AudioSpectrum input) throws IOException {
            final int bottomBin = input.getBin(bottomFrequency);
            final int topBin = input.getBin(topFrequency);
            final float[] realData = input.getRealData();
            final float[] imaginaryData = input.getImaginaryData();
            return input.derive(
                    realData == null ? null : Arrays.copyOfRange(realData, Math.max(0, bottomBin), Math.min(topBin, realData.length)),
                    imaginaryData == null ? null : Arrays.copyOfRange(imaginaryData, Math.max(0, bottomBin), Math.min(topBin, imaginaryData.length))
            );
        }
    }

    public static FrequencyCut byFrequency(double bottomFrequency, double topFrequency) {
        return new FrequencyCut((float) bottomFrequency, (float) topFrequency);
    }


    public static FrequencyCut byFrequency(float bottomFrequency, float topFrequency) {
        return new FrequencyCut(bottomFrequency, topFrequency);
    }

    public static BinCut byBin(int bottomBin, int topBin) {
        return new BinCut(bottomBin, topBin);
    }
}
