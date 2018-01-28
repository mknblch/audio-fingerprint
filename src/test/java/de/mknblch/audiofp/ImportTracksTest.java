package de.mknblch.audiofp;

import com.tagtraum.jipes.SignalPipeline;
import com.tagtraum.jipes.SignalPump;
import com.tagtraum.jipes.audio.AudioBuffer;
import com.tagtraum.jipes.audio.AudioSignalSource;
import de.mknblch.audiofp.db.DBWriter;
import de.mknblch.audiofp.db.H2Dao;
import de.mknblch.audiofp.processor.Fingerprint;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Import all tracks found in path into the db
 *
 * @author mknblch
 */
@Ignore
public class ImportTracksTest {

    private static final Logger LOGGER = getLogger(ImportTracksTest.class);

    private static final String filetypes = "wav,mp3";
    // TODO change
    private static Path path = Paths.get("c:/data/test/tracks");

    private static H2Dao db;
    private static ExecutorService pool;

    @BeforeClass
    public static void setup() throws SQLException {
        db = new H2Dao(Paths.get("./", "tracks.db"));
        pool = Executors.newFixedThreadPool(6);
    }

    @Test
    public void testImport() throws Exception {

        final List<Path> pathList = Setup.listTracks(path, filetypes);

        for (Path path : pathList) {
            pool.execute(() -> {
                try {
                    importTrack(path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(10L, TimeUnit.MINUTES);
        LOGGER.info("done");
    }

    private void importTrack(Path path) throws IOException, UnsupportedAudioFileException, SQLException {

        final SignalPump<AudioBuffer> pump =
                new SignalPump<>(new AudioSignalSource(Setup.AUDIOSOURCE_SETUP.open(path)));
        final SignalPipeline<AudioBuffer, Void> pipe = Setup.FINGERPRINT_SETUP
                .build()
                .joinWith(new DBWriter(db, path));
        pump.add(pipe);
        pump.pump();
        final Fingerprint.Info info = (Fingerprint.Info) pipe.getProcessorWithId(Fingerprint.Info.ID);

        LOGGER.debug("{} got {} unique of {} hashes", path.getFileName().toString(), info.getNumUniqueHashes(), info.getNumHashes());
    }
}
