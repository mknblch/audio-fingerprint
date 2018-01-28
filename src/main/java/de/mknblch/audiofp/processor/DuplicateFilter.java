package de.mknblch.audiofp.processor;

import com.tagtraum.jipes.AbstractSignalProcessor;
import de.mknblch.audiofp.Feature;
import de.mknblch.audiofp.Hash;

import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;

/**
 * @author mknblch
 */
public class DuplicateFilter extends AbstractSignalProcessor<Feature, Feature> {

    public static final String ID = DuplicateFilter.class.getName();

    private final BitSet hashes;

    public DuplicateFilter() {
        this(ID);
    }

    public DuplicateFilter(Object id) {
        super(id);
        hashes = new BitSet();
    }

    @Override
    protected Feature processNext(Feature input) throws IOException {
        final Hash[] hashes = input.hashes;
        final Hash[] filtered = new Hash[hashes.length];
        int offset = 0;
        for (Hash hash : hashes) {
            final int value = hash.hash();
            if (!this.hashes.get(value)) {
                filtered[offset++] = hash;
                this.hashes.set(value);
            }
        }
        input.hashes = Arrays.copyOf(filtered, offset);
        return input;
    }

}
