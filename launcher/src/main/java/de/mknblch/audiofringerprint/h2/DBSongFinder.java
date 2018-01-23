package de.mknblch.audiofringerprint.h2;

import com.carrotsearch.hppc.*;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.tagtraum.jipes.SignalProcessor;
import com.tagtraum.jipes.SignalProcessorSupport;
import de.mknblch.audiofingerprint.Feature;
import de.mknblch.audiofingerprint.Hash;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

/**
 * @author mknblch
 */
public class DBSongFinder implements SignalProcessor<Feature, String> {//extends AbstractSignalProcessor<Feature, String> {

    public static final String ID = DBSongFinder.class.getName();

    public static final Comparator<Candidate> CANDIDATE_COMPARATOR = Comparator.comparingInt(a -> a.count);
    private final int buckets;
    private final H2Dao dao;

    private final SignalProcessorSupport<String> signalProcessorSupport = new SignalProcessorSupport<>();

    private final IntObjectMap<IntArrayList> map;
    private final Path track;
    private String result;

    public DBSongFinder(int buckets, Path track, H2Dao dao) {
        this.buckets = buckets;
        this.track = track;
        this.dao = dao;
        map = new IntObjectScatterMap<>();
    }

    @Override
    public void process(Feature feature) throws IOException {
        final Hash[] hashes = feature.hashes;
        if (null == hashes || hashes.length == 0) {
            return;
        }
        final ResultSet resultSet = dao.findHashes(hashes); //, 0);
        try {
            while (resultSet.next()) {
                final int track = resultSet.getInt(1);
                final int ts = resultSet.getInt(2);
                add(track, ts);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void flush() throws IOException {

        final String name = track.getFileName().toString() + ".png";

        System.out.println(name);
        final LinkedList<Candidate> list = new LinkedList<>();
//        int sum = 0;
        for (IntObjectCursor<IntArrayList> cursor : map) {
            final int compactness = compactness(cursor.value.toArray());
            list.add(new Candidate(cursor.key, compactness));
//            sum += compactness;
        }
        Collections.sort(list, CANDIDATE_COMPARATOR.reversed());

//        for (int i = 0; i < list.size() - 1 && i < 3; i++) {
//
//            final Candidate head = list.get(i);
//            final Candidate tail = list.get(i + 1);
//
//            System.out.println("\t"
//                    + "(" + head.count + ") "
//                    + dao.getTrack(head.track) + " [ "
//                    + String.format("%.2f%%", 100 * ((double) head.count / sum)) + " / "
//                    + String.format("%.2f%%", 100 * (((double) head.count / tail.count) - 1.0)) + " ]"
//            );
//        }

        result = dao.getTrack(list.get(0).track);
        signalProcessorSupport.process(result);
        signalProcessorSupport.flush();
    }

    @Override
    public String getOutput() throws IOException {
        return result;
    }

    @Override
    public Object getId() {
        return ID;
    }

    @Override
    public <O2> SignalProcessor<String, O2> connectTo(SignalProcessor<String, O2> signalProcessor) {
        return signalProcessorSupport.connectTo(signalProcessor);
    }

    @Override
    public <O2> SignalProcessor<String, O2> disconnectFrom(SignalProcessor<String, O2> signalProcessor) {
        return signalProcessorSupport.disconnectFrom(signalProcessor);
    }

    @Override
    public SignalProcessor<String, ?>[] getConnectedProcessors() {
        return signalProcessorSupport.getConnectedProcessors();
    }

    private int compactness(int[] values) {
        int min = Integer.MAX_VALUE;
        int max = 0;
        for (int i : values) {
            if (i < min) {
                min = i;
            }
            if (i > max) {
                max = i;
            }
        }
        final double d = max - min;
        final int[] data = new int[buckets];
        int best = 0;
        for (int v : values) {
            final int t = (int) ((v - min) / d * (buckets - 1));
            data[t]++;
            best = Math.max(data[t], best);
        }

        return best;
    }

    private void add(int track, int ts) {

        final IntArrayList list;
        if (!map.containsKey(track)) {
            list = new IntArrayList();
            map.put(track, list);
            list.add(ts);
        } else {
            map.get(track).add(ts);
        }

    }

    public static final class Candidate {
        public final int track;
        public final int count;

        public Candidate(int track, int count) {
            this.track = track;
            this.count = count;
        }

        @Override
        public String toString() {
            return "\t" +
                    "'" + track + "\' : " + count;
        }
    }
}
