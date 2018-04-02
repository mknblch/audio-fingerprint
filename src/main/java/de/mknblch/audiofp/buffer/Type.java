package de.mknblch.audiofp.buffer;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author mknblch
 */
public enum Type {

    INT(42), FLOAT(43), STRING(44), LIST(45), MAP(46);

    public final byte header;

    Type(int header) {
        this.header = (byte) header;
    }

    public static final Type id(int type) {
        switch (type) {
            case 42:
                return INT;
            case 43:
                return FLOAT;
            case 44:
                return STRING;
            case 45:
                return LIST;
            case 46:
                return MAP;
        }
        throw new IllegalArgumentException("Invalid type " + type);
    }

    public static final Type of(Object o) {
        if (null == o) {
            throw new IllegalArgumentException("Null");
        }
        if (o instanceof Integer) {
            return INT;
        }
        if (o instanceof Float) {
            return FLOAT;
        }
        if (o instanceof String) {
            return STRING;
        }
        if (o instanceof Collection) {
            return LIST;
        }
        if (o instanceof Map) {
            return MAP;
        }
        throw new IllegalArgumentException("Invalid type " + o.getClass().getSimpleName());
    }
}
