package de.mknblch.audiofp.common;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

/**
 * @author mknblch
 */
public class ImageBuffer {

    public static final Layout LEFT_RIGHT =
            (width, height, x, y) -> y * width + x;

    public static final Layout BOTTOM_UP =
            (width, height, x, y) -> x * height + y;

    public static final Layout BOTTOM_UP_Y_REVERSED =
            (width, height, x, y) -> x * height + (height - y - 1);

    protected int[] buffer;
    protected int offset = 0;

    public final Layout layout;

    public ImageBuffer(Layout indexLayout) {
        this(indexLayout, 1024 * 64);
    }

    public ImageBuffer(Layout indexLayout, int initialCapacity) {
        this(indexLayout, new int[initialCapacity]);
    }

    public ImageBuffer(Layout indexLayout, int[] buffer) {
        this.layout = indexLayout;
        this.buffer = buffer;
    }

    public ImageBuffer addPixel(int r, int g, int b) {
        grow();
        buffer[offset++] = rgb(r, g, b);
        return this;
    }

    public ImageBuffer addPixel(float gray) {
        grow();
        buffer[offset++] = gray(gray);
        return this;
    }

    public ImageBuffer copy(int[] source, int offset, int length) {
        System.arraycopy(source, offset, buffer, this.offset, length);
        this.offset += length;
        return this;
    }

    public void clear () {
        Arrays.fill(buffer, 0);
    }

    public int get(int offset) {
        return buffer[offset];
    }

    public int size() {
        return offset;
    }

    public Materialized materialize(int width, int height) {
        return new Materialized(Arrays.copyOf(buffer, width * height), width, height) {
            @Override
            public int index(int width, int height, int x, int y) {
                return layout.index(width, height, x, y);
            }
        };
    }

    private void grow() {
        if (buffer.length <= offset) {
            buffer = Arrays.copyOf(buffer, offset + offset / 2);
        }
    }

    public static int clip(int color) {
        return (color > 255 ? 255 : (color < 0 ? 0 : color)) & 0xFF;
    }

    public static int rgb(int r, int g, int b) {
        return clip(r) << 16 | clip(g) << 8 | clip(b);
    }

    public static int gray(float gray) {
        final int g = clip((int) (gray * 255));
        return g << 16 | g << 8 | g;
    }

    public static abstract class Materialized implements Layout {

        public final int width;
        public final int height;
        private final int[] buffer;
        private int undefinedColor = 0;

        private Materialized(int[] buffer, int width, int height) {
            this.buffer = buffer;
            this.width = width;
            this.height = height;
        }

        public Materialized withUndefinedColor(int undefinedColor) {
            this.undefinedColor = undefinedColor;
            return this;
        }

        public Materialized setPixel(int x, int y, int r, int g, int b) {
            return setPixel((y * width + x) % width, (y * width + x) / width, rgb(r, g, b));
        }

        public Materialized setPixel(int x, int y, float g) {
            return setPixel(x, y, gray(g));
        }

        public Materialized setPixel(int x, int y, int rgb) {
            if (x >= width || y >= height) {
                return this;
            }
            buffer[index(width, height, x, y)] = rgb;
            return this;
        }

        public Materialized drawLine(int x1, int y1, int x2, int y2, float gray) {
            return drawLine(x1, y1, x2, y2, gray(gray));
        }


        public Materialized drawLine(int x1, int y1, int x2, int y2, int r, int g, int b) {
            return drawLine(x1, y1, x2, y2, rgb(r, g, b));
        }

        public Materialized drawLine(int x0, int y0, final int x1, final int y1, int rgb) {
            if (x0 >= width || x1 >= width || y0 >= height || y1 >= height) {
                return this;
            }
            int dx = Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
            int dy = -Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
            int err = dx + dy, x = x0, y = y0, e2;
            for (; ; ) {
                buffer[index(width, height, x, y)] = rgb;
                if (x == x1 && y == y1) break;
                e2 = 2 * err;
                if (e2 >= dy) {
                    err += dy;
                    x += sx;
                }
                if (e2 <= dx) {
                    err += dx;
                    y += sy;
                }
            }
            return this;
        }

        public int index(int x, int y) {
            return index(width, height, x, y);
        }

        public BufferedImage toBufferedImage() {
            final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            final int[] data = ((DataBufferInt) bufferedImage.getRaster().getDataBuffer()).getData();
            Arrays.setAll(data, i -> {
                final int index = index(width, height, i % width, i / width);
                return index < buffer.length ? buffer[index] : undefinedColor;
            });
            return bufferedImage;
        }

    }

    public interface Layout {

        int index(int width, int height, int x, int y);
    }
}
