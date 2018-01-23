package de.mknblch.audiofingerprint.processor;

import de.mknblch.audiofingerprint.Feature;
import de.mknblch.audiofingerprint.common.ImageBuffer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author mknblch
 */
public class ImageAggregator extends SignalAggregator<Feature, BufferedImage> {

    public interface DrawFunction {
        void draw(ImageBuffer.Materialized image, Feature[] features);
    }

    public static final String ID = ImageAggregator.class.getName();

    public static final int SECONDS_COLOR = ImageBuffer.rgb(50, 100, 255);
    public static final int SECONDS_10_COLOR = ImageBuffer.rgb(50, 255, 200);

    public static DrawFunction TIME_MARKERS = (image, features) -> {
        long last = 0;
        int c = 0;
        for (int x = 0; x < features.length; x++) {
            final long timestamp = features[x].getTimestamp(TimeUnit.SECONDS);
            if (timestamp != last) {
                if (++c % 10 == 0) {
                    image.drawLine(x, 0, x, 5, SECONDS_10_COLOR);
                } else {
                    image.drawLine(x, 0, x, 3, SECONDS_COLOR);
                }
                last = timestamp;
            }
        }
    };

    private List<DrawFunction> drawFunctions = new ArrayList<>();

    public ImageAggregator(DrawFunction... functions) {
        this(ID, functions);
    }

    public ImageAggregator(Object id, DrawFunction... functions) {
        super(id, new Feature[1024]);
        addDrawFunction(TIME_MARKERS);
        for (DrawFunction function : functions) {
            addDrawFunction(function);
        }
    }

    @Override
    protected void process(Feature[] data) throws IOException {
        final ImageBuffer.Materialized materialized = toImage(data)
                .materialize(data.length, data[0].getMagnitudes().length);
        for (DrawFunction drawFunction : drawFunctions) {
            drawFunction.draw(materialized, data);
        }
        emit(materialized.toBufferedImage());
    }

    public ImageAggregator clearDrawFunctions() {
        this.drawFunctions.clear();
        return this;
    }

    public ImageAggregator addDrawFunction(DrawFunction drawFunction) {
        drawFunctions.add(drawFunction);
        return this;
    }

    private static ImageBuffer toImage(Feature[] data) {
        final ImageBuffer image = new ImageBuffer(ImageBuffer.BOTTOM_UP_Y_REVERSED);
        for (int i = 0; i < data.length; i++) {
            final float[] mag = data[i].getMagnitudes();
            for (int j = 0; j <  mag.length; j++) {
                image.addPixel(mag[j]);
            }
        }
        return image;
    }

}
