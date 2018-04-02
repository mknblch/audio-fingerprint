package de.mknblch.audiofp.buffer;

import org.slf4j.Logger;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author mknblch
 */
public class Writer {

    private static final Logger LOGGER = getLogger(Writer.class);

    public static ByteBuffer write(Object value) {
        final int capacity = sizeInBytes(value);
        LOGGER.info("allocating {}", capacity);
        final ByteBuffer buffer = ByteBuffer.allocate(capacity);
        write(buffer, value);
        return buffer;
    }

    private static void write(ByteBuffer out, Object o) {
        if (null == o) {
            return;
        }
        final Type type = Type.of(o);
        out.put(type.header);
        switch (type) {
            case STRING:
                writeString(out, (String) o);
                break;
            case INT:
                out.putInt(((int) o));
                break;
            case FLOAT:
                out.putFloat((float) o);
                break;
            case LIST:
                writeList(out, (List) o);
                break;
            case MAP:
                writeMap(out, (Map) o);
                break;
            default:
                throw new IllegalArgumentException("Unknown value");
        }
    }

    private static void writeList(ByteBuffer out, List o) {
        final List list = o;
        out.putInt(list.size());
        list.forEach(e -> write(out, e));
    }

    private static void writeString(ByteBuffer out, String o) {
        final byte[] bytes = o.getBytes();
        out.putInt(bytes.length);
        out.put(bytes);
    }

    private static void writeMap(ByteBuffer out, Map<Object, Object> o) {
        out.putInt(o.size());
        o.entrySet().forEach(e -> {
            write(out, e.getKey());
            write(out, e.getValue());
        });
    }

    private static int sizeInBytes(Object object) {
        switch (Type.of(object)) {
            case FLOAT:
            case INT:
                return 5;
            case STRING:
                return 5 + ((String) object).getBytes().length;
            case LIST:
                //noinspection unchecked
                return 5 + ((List) object).stream()
                        .mapToInt(Writer::sizeInBytes)
                        .sum();
            case MAP:
                //noinspection unchecked
                return 5 + ((Map<Object, Object>) object).entrySet()
                        .stream()
                        .mapToInt(e -> sizeInBytes(e.getKey()) + sizeInBytes(e.getValue()))
                        .sum();
        }
        throw new IllegalArgumentException("Invalid type " + object.getClass().getSimpleName());
    }
}
