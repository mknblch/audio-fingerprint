package de.mknblch.audiofp.processor;

import com.tagtraum.jipes.audio.AudioBuffer;
import com.tagtraum.jipes.audio.AudioBufferFunctions;
import com.tagtraum.jipes.math.MapFunction;
import com.tagtraum.jipes.universal.Mapping;

/**
 * maps magnitude using natural logarithm and a scale factor
 * <pre>
 * y = log(1.0 + x) * scale
 * </pre>
 *
 * @author mknblch
 */
public class Log1pMapping {

    public static float[] map(float[] data, double scale) {
        for (int i = 0; i < data.length; i++) {
            data[i] = (float) (scale * Math.log1p(data[i]));
        }
        return data;
    }

    public static float[] map(float[] data) {
        for (int i = 0; i < data.length; i++) {
            data[i] = (float) Math.log1p(data[i]);
        }
        return data;
    }

    public static final class Unscaled implements MapFunction<float[]> {

        @Override
        public float[] map(float[] data) {
            return Log1pMapping.map(data);
        }
    }

    public static final class Scaled implements MapFunction<float[]> {

        private final double scale;

        public Scaled(double scale) {
            this.scale = scale;
        }

        @Override
        public float[] map(float[] data) {
            return Log1pMapping.map(data, scale);
        }
    }

    public static Mapping<AudioBuffer> unscaled() {
        return new Mapping<>(AudioBufferFunctions.createMagnitudeMapFunction(new Unscaled()));
    }

    public static Mapping<AudioBuffer> scaled(double factor) {
        return new Mapping<>(AudioBufferFunctions.createMagnitudeMapFunction(new Scaled(factor)));
    }
}
