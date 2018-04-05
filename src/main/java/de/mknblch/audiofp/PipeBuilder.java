package de.mknblch.audiofp;

import com.tagtraum.jipes.SignalPipeline;
import com.tagtraum.jipes.audio.*;
import com.tagtraum.jipes.math.MapFunction;
import com.tagtraum.jipes.math.WindowFunction;
import com.tagtraum.jipes.universal.Mapping;
import de.mknblch.audiofp.processor.*;
import utils.Tones;
import utils.WindowSizeFunction;

import java.util.function.IntBinaryOperator;

/**
 * @author mknblch
 */
public class PipeBuilder {

    // fft frame size / spectrum size
    private int frames = 2048;

    // time signal
    private MapFunction<float[]> timeSignalWindowFunction = WindowFunction.HAMMING;
    private double hopFactor = 1.0 / 3.0;

    // downsample
    private int downsample = 1;

    // magnitude scaling
    private final double magnitudeScaling = 1.0 / 4.0;

    // cut spectrum
    private double cutFreqBottom = Tones.A.shift(-8);
    private double cutFreqTop = Tones.A.shift(8);

    // whitening
    private int whiteningLookupWindow = 9;
    private double whiteningKillFactor = 1f / 4f;

    // local max
    private int localMaximumLookupWidth = 11;
    private IntBinaryOperator localMaximumLookupHeightFunction = (index, samples) -> samples / 24;

    // hashing
    private int maxHashesPerFrame = 6;
    private int maxHashesPerReference = 2;
    private long hashingMinDt = 200;
    private long hashingMaxDt = 600;
    private IntBinaryOperator hashingLookupHeightFunction = WindowSizeFunction.adaptive(6, 30);
    private Fingerprint.ScoreFunction hashScoreFunction = (int i0, int i1, int f0, int f1, float vf0, float vf1) -> vf0 + vf1 / 2;
    private boolean scoreMax = true;

    public SignalPipeline<AudioBuffer, Feature> build() {

        return new SignalPipeline<>(
                new Mono(),
                new Downsample(downsample),
                new SlidingWindow(frames, (int) (frames * hopFactor)),
                new Mapping<>(AudioBufferFunctions.createMapFunction(timeSignalWindowFunction)),
                new FFT(),
                Cut.byFrequency(cutFreqBottom, cutFreqTop),
                Log1pMapping.scaled(magnitudeScaling),
                new Whitening(whiteningLookupWindow, whiteningKillFactor),
                Feature.wrapper(false),
                new LocalMaximum(localMaximumLookupWidth, frames / 6)
                        .withWindowFunction(localMaximumLookupHeightFunction),
                new Fingerprint(maxHashesPerFrame, maxHashesPerReference, hashingMinDt, hashingMaxDt)
                        .withWindowSizeFunction(hashingLookupHeightFunction)
                        .withScoreFunction(hashScoreFunction, scoreMax),
                new Fingerprint.Info()
        );
    }

    public PipeBuilder withFrames(int frames) {
        this.frames = frames;
        return this;
    }

    public PipeBuilder withDownsample(int downsample) {
        this.downsample = downsample;
        return this;
    }

    public PipeBuilder withCutFreqBottom(double cutFreqBottom) {
        this.cutFreqBottom = cutFreqBottom;
        return this;
    }

    public PipeBuilder withCutFreqTop(double cutFreqTop) {
        this.cutFreqTop = cutFreqTop;
        return this;
    }

    public PipeBuilder withHopFactor(double hopFactor) {
        this.hopFactor = hopFactor;
        return this;
    }

    public PipeBuilder withTimeSignalWindowFunction(MapFunction<float[]> timeSignalWindowFunction) {
        this.timeSignalWindowFunction = timeSignalWindowFunction;
        return this;
    }

    public PipeBuilder withWhiteningLookupWindow(int whiteningLookupWindow) {
        this.whiteningLookupWindow = whiteningLookupWindow;
        return this;
    }

    public PipeBuilder withWhiteningKillFactor(double whiteningKillFactor) {
        this.whiteningKillFactor = whiteningKillFactor;
        return this;
    }

    public PipeBuilder withLocalMaximumLookupWidth(int localMaximumLookupWidth) {
        this.localMaximumLookupWidth = localMaximumLookupWidth;
        return this;
    }

    public PipeBuilder withLocalMaximumLookupHeightFunction(IntBinaryOperator localMaximumLookupHeightFunction) {
        this.localMaximumLookupHeightFunction = localMaximumLookupHeightFunction;
        return this;
    }

    public PipeBuilder withMaxHashesPerFrame(int maxHashesPerFrame) {
        this.maxHashesPerFrame = maxHashesPerFrame;
        return this;
    }

    public PipeBuilder withMaxHashesPerReference(int maxHashesPerReference) {
        this.maxHashesPerReference = maxHashesPerReference;
        return this;
    }

    public PipeBuilder withHashingMinDt(long hashingMinDt) {
        this.hashingMinDt = hashingMinDt;
        return this;
    }

    public PipeBuilder withHashingMaxDt(long hashingMaxDt) {
        this.hashingMaxDt = hashingMaxDt;
        return this;
    }

    public PipeBuilder withHashingLookupHeightFunction(IntBinaryOperator hashingLookupHeightFunction) {
        this.hashingLookupHeightFunction = hashingLookupHeightFunction;
        return this;
    }

    public PipeBuilder withHashScoreFunction(Fingerprint.ScoreFunction hashScoreFunction, boolean scoreMax) {
        this.hashScoreFunction = hashScoreFunction;
        this.scoreMax = scoreMax;
        return this;
    }
}
