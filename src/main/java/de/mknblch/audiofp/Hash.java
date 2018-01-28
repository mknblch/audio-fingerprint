package de.mknblch.audiofp;

/**
 * @author mknblch
 */
public class Hash {

    public final long timestamp;
    public final int ts;
    public final int tt;
    public final int fs;
    public final int ft;

    public Hash(long timestamp, int ts, int tt, int fs, int ft) {
        this.timestamp = timestamp;
        this.ts = ts;
        this.tt = tt;
        this.fs = fs;
        this.ft = ft;
    }

    public int hash() {
        return ((tt - ts) & 0xFF) << 20 | ((fs & 0xEFF) << 10) | (ft & 0xEFF);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hash)) return false;

        Hash hash = (Hash) o;

        if (timestamp != hash.timestamp) return false;
        if (ts != hash.ts) return false;
        if (tt != hash.tt) return false;
        if (fs != hash.fs) return false;
        return ft == hash.ft;
    }

    @Override
    public int hashCode() {
        return hash();
    }

    @Override
    public String toString() {
        return "Hash{" +
                "ts=" + ts +
                ", tt=" + tt +
                ", fs=" + fs +
                ", ft=" + ft +
                '}';
    }
}