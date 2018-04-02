package de.mknblch.audiofp.db;

import com.tagtraum.jipes.SignalProcessor;
import com.tagtraum.jipes.SignalProcessorSupport;
import de.mknblch.audiofp.Feature;
import de.mknblch.audiofp.Hash;
import de.mknblch.audiofp.buffer.DB;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author mknblch
 */
public class DBFinder implements SignalProcessor<Feature, List<String>> {

    public static final String ID = DBFinder.class.getName();

    public static final Comparator<Candidate> CANDIDATE_COMPARATOR = Comparator.comparingInt(a -> a.count);
    private final int buckets;

    private final SignalProcessorSupport<List<String>> signalProcessorSupport = new SignalProcessorSupport<>();

    private final Map<Integer, LinkedList<Integer>> bag;
    private final DB db;
    private List<String> result;

    public DBFinder(int buckets, DB db) {
        this.buckets = buckets;
        this.db = db;
        bag = new HashMap<>();
    }

    @Override
    public void process(Feature feature) throws IOException {
        final Hash[] hashes = feature.hashes;
        if (null == hashes || hashes.length == 0) {
            return;
        }
        for (Hash hash : hashes) {
            db.find(hash.hash(), this::add);
        }
    }

    @Override
    public void flush() throws IOException {

        final LinkedList<Candidate> list = new LinkedList<>();
        for (Map.Entry<Integer, LinkedList<Integer>> entry : bag.entrySet()) {
            final int compactness = compactness(entry.getValue().toArray(new Integer[]{}));
            list.add(new Candidate(entry.getKey(), compactness));
        }
        Collections.sort(list, CANDIDATE_COMPARATOR.reversed());

        result = list.stream()
                .sorted(CANDIDATE_COMPARATOR.reversed())
                .map(c -> c.count + " : " + db.getTrack(c.track))
                .limit(3)
                .collect(Collectors.toList());

        signalProcessorSupport.process(result);
        signalProcessorSupport.flush();
    }

    @Override
    public List<String> getOutput() throws IOException {
        return result;
    }

    @Override
    public Object getId() {
        return ID;
    }

    @Override
    public <O2> SignalProcessor<List<String>, O2> connectTo(SignalProcessor<List<String>, O2> signalProcessor) {
        return signalProcessorSupport.connectTo(signalProcessor);
    }

    @Override
    public <O2> SignalProcessor<List<String>, O2> disconnectFrom(SignalProcessor<List<String>, O2> signalProcessor) {
        return signalProcessorSupport.disconnectFrom(signalProcessor);
    }

    @Override
    public SignalProcessor<List<String>, ?>[] getConnectedProcessors() {
        return signalProcessorSupport.getConnectedProcessors();
    }

    private int compactness(Integer[] values) {
        int min = Integer.MAX_VALUE;
        int max = 0;
        for (int v : values) {
            if (v < min) {
                min = v;
            }
            if (v > max) {
                max = v;
            }
        }
        final double d = max - min + 1;
        final int buckets = (int) d;
        int[] data = new int[buckets];
        for (int v : values) {
            final int t = (int) (((v - min) / d) * (data.length - 1));
            data[t] = 1; // = Math.max(2, data[t] + 1);
        }
        data = foldTo(data, 16);
        int best = 0;
        for (int i = 0; i < data.length; i++) {
            best = Math.max(data[i], best);
        }

//        System.out.println(Arrays.toString(data));


        return best;
    }


    private int[] foldTo(int[] data, int n) {
        int l = data.length;
        do {
            l /= 2;
            for (int i = 0; i < l; i++) {
                data[i] = (data[i * 2] + data[i * 2 + 1]);
            }
        } while (l > n);

        return Arrays.copyOf(data, l);
    }

    private void add(int track, int ts) {

        if (!bag.containsKey(track)) {
            LinkedList<Integer> tsl = new LinkedList<>();
            bag.put(track, tsl);
            tsl.add(ts);
        } else {
            bag.get(track).add(ts);
        }
    }

    public static final class Candidate {

        public final int track;
        public final int count;

        public Candidate(int track, int count) {
            this.track = track;
            this.count = count;
        }

    }
}
