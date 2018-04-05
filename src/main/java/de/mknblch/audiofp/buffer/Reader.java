package de.mknblch.audiofp.buffer;


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mknblch
 */
public class Reader {

    static Object read(ByteBuffer buffer) {

        final byte header = buffer.get();
        final Type type = Type.id(header);
        switch (type) {
            case LIST:
                return readList(buffer);
            case MAP:
                return readMap(buffer);
            case STRING:
                return readString(buffer);
            case FLOAT:
                return buffer.getFloat();
            case INT:
                return buffer.getInt();
        }
        throw new IllegalArgumentException("Invalid");

    }

    private static Object readString(ByteBuffer buffer) {
        final byte[] bytes = new byte[buffer.getInt()];
        buffer.get(bytes);
        return new String(bytes);
    }

    private static Object readList(ByteBuffer buffer) {
        final int size = buffer.getInt();
        final ArrayList<Object> objects = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            objects.add(read(buffer));
        }
        return objects;
    }

    private static Object readMap(ByteBuffer buffer) {
        final int size = buffer.getInt();
        final Map<Object, Object> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            final Object key = read(buffer);
            final Object value = read(buffer);
            map.put(key, value);
        }
        return map;
    }

}
