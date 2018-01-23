package de.mknblch.audiofringerprint.h2;

import com.tagtraum.jipes.AbstractSignalProcessor;
import de.mknblch.audiofingerprint.Feature;
import de.mknblch.audiofingerprint.Hash;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author mknblch
 */
public class DBCachedHashWriter extends AbstractSignalProcessor<Feature, Void> {

    private static final Logger LOGGER = getLogger(DBCachedHashWriter.class);

    private final Path track;
    private final H2Dao.BatchInsert batchInsert;
    private final boolean trackExists;

    public DBCachedHashWriter(H2Dao dao, Path track) throws SQLException {
        this.track = track;
        final String name = track.getFileName().toString();
        trackExists = dao.trackExists(name);
        if (trackExists) {
            batchInsert = null;
        } else {
            batchInsert = dao.batchInsert(name);
        }
    }

    @Override
    protected Void processNext(Feature feature) throws IOException {
        if (trackExists) {
            return null;
        }
        final Hash[] hashes = feature.hashes;
        if (hashes == null || hashes.length == 0) {
            return null;
        }
        batchInsert.insertHash(hashes);
        return null;
    }

    @Override
    public void flush() throws IOException {
        if (trackExists) {
            return;
        }
        try {
            LOGGER.debug("writing {}", track);
            batchInsert.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
