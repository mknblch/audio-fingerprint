package de.mknblch.audiofp.db;

import com.tagtraum.jipes.AbstractSignalProcessor;
import de.mknblch.audiofp.Feature;
import de.mknblch.audiofp.Hash;
import de.mknblch.audiofp.buffer.DB;
import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author mknblch
 */
public class DBWriter extends AbstractSignalProcessor<Feature, Void> {

    private static final Logger LOGGER = getLogger(DBWriter.class);

    private final DB.BatchImport batchImport;

    public DBWriter(DB.BatchImport batchImport) {
        this.batchImport = batchImport;
    }


    @Override
    protected Void processNext(Feature feature) throws IOException {
        final Hash[] hashes = feature.hashes;
        if (hashes == null || hashes.length == 0) {
            return null;
        }
        for (Hash hash : hashes) {
            batchImport.add(hash.hash(), (int) hash.timestamp);
        }
        return null;
    }

    @Override
    public void flush() throws IOException {
        batchImport.commit();
    }
}
