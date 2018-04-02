package de.mknblch.audiofp.buffer;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * @author mknblch
 */
public class DB {

    public static final int B = 20;
    private final List<Object> db;

    private final Map<Integer, String> tracks;
    private final Map<Integer, Map<Integer, List<Integer>>> hashes;

    public DB() {
        db = new ArrayList<>();
        tracks = new HashMap<>();
        hashes = new HashMap<>();
        db.add(tracks);
        db.add(hashes);
    }

    private DB(List<Object> db) {
        this.db = db;
        tracks = (Map<Integer, String>) db.get(0);
        hashes = (Map<Integer, Map<Integer, List<Integer>>>) db.get(1);
    }

    public Predicate<Path> isKnown() {
        return p -> {
            return tracks.containsValue(p.getFileName().toString());
        };
    }

    public String getTrack(int id) {
        if (!tracks.containsKey(id)) {
            throw new IllegalArgumentException("Unknown id");
        }
        return tracks.get(id);
    }

    @Override
    public String toString() {
        return "DB (" + tracks.size() + ")";
    }

    public void find(int hash, BiConsumer<Integer, Integer> consumer) {
        final Map<Integer, List<Integer>> map = hashes.get(hash);
        if (null == map) {
            return;
        }
        map.forEach((key, value) -> value.forEach(ts -> consumer.accept(key, ts)));
    }

    public static DB load(Path dbPath) throws IOException {
        final File file = dbPath.toFile();
        if (!file.exists()) {
            return new DB();
        }
        final RandomAccessFile aFile = new RandomAccessFile(file, "r");
        FileChannel inChannel = aFile.getChannel();
        MappedByteBuffer buffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
        final List<Object> list = (List<Object>) Reader.read(buffer);
        return new DB(list);
    }

    public void write(Path dbPath) throws IOException {
        final ByteBuffer buffer = Writer.write(db);
        buffer.flip();
        final FileOutputStream outputStream = new FileOutputStream(dbPath.toFile(), false);
        outputStream.getChannel()
                .write(buffer);
        outputStream.close();
    }

    private void add(int track, int hash, List<Integer> ts) {
        Map<Integer, List<Integer>> map = hashes.get(hash);
        if (null == map) {
            map = new HashMap<>();
            final ArrayList<Integer> list = new ArrayList<>(Math.min(ts.size(), B));
            list.addAll(ts);
            map.put(track, list);
            hashes.put(hash, map);
        } else {
            List<Integer> list = map.computeIfAbsent(track, k -> new ArrayList<>(Math.min(ts.size(), B)));
            list.addAll(ts);
        }
    }

    public synchronized BatchImport batch(Path track) {
        final String name = track.getFileName().toString();
        if (tracks.containsKey(name)) {
            throw new IllegalArgumentException("Track already known");
        }
        final int id = tracks.size();
        tracks.put(id, name);
        return new BatchImport(id);
    }


    public class BatchImport {

        private final int id;
        private final Map<Integer, List<Integer>> data;

        private BatchImport(int id) {
            this.id = id;
            data = new HashMap<>();
        }

        public void add(int hash, int ts) {
            List<Integer> list = data.get(hash);
            if (null == list) {
                list = new ArrayList<>(B);
                data.put(hash, list);
            }
            list.add(ts);
        }

        public void commit() {
            synchronized (hashes) {
                for (Map.Entry<Integer, List<Integer>> entry : data.entrySet()) {
                    DB.this.add(id, entry.getKey(), entry.getValue());
                }
            }
        }
    }

}
