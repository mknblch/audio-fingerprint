package de.mknblch.audiofp.processor;

import com.tagtraum.jipes.AbstractSignalProcessor;
import com.tagtraum.jipes.audio.AudioSpectrum;
import com.tagtraum.jipes.math.Floats;
import de.mknblch.audiofp.common.FloatRing;

import java.io.IOException;

/**
 * AudioSpectrum processor to kill signals lower then
 * a given factor multiplied by the running maximum
 * over a given amount of frames.
 *
 * @author mknblch
 */
public class Whitening extends AbstractSignalProcessor<AudioSpectrum, AudioSpectrum> {

    public static final String ID = Whitening.class.getName();

    private final FloatRing runningMax;

    private final double killFactor;

    public Whitening(int frames, double killFactor) {
        super(ID);
        runningMax = new FloatRing(frames);
        this.killFactor = killFactor;
    }

    @Override
    protected AudioSpectrum processNext(AudioSpectrum input) throws IOException {
        final float[] data = input.getMagnitudes();
        final double kill = killFactor * Floats.max(runningMax.getData());
        float max = 0;
        for (int i = 0; i < data.length; i++) {
            if (data[i] < kill) {
                data[i] = 0f;
            }
            if (data[i] > max) {
                max = data[i];
            }
        }
        runningMax.put(max);
        return input;
    }
}
