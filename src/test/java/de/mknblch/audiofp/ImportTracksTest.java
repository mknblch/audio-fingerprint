package de.mknblch.audiofp;

import com.tagtraum.jipes.SignalPipeline;
import com.tagtraum.jipes.SignalPump;
import com.tagtraum.jipes.audio.AudioBuffer;
import com.tagtraum.jipes.audio.AudioSignalSource;
import de.mknblch.audiofp.processor.Fingerprint;
import org.junit.BeforeClass;
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

import static launcher.FingerprintSetup.AUDIOSOURCE_SETUP;
import static launcher.FingerprintSetup.FINGERPRINT_SETUP;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author mknblch
 */
public class ImportTracksTest {

    private static final Logger LOGGER = getLogger(ImportTracksTest.class);

    private static final String filetypes = "wav,mp3";
    private static Path path = Paths.get("");

    private static audiofingerprint.H2Dao db;
    private static ExecutorService pool;

    @BeforeClass
    public static void setup() throws SQLException {
        db = new audiofingerprint.H2Dao(Paths.get("./", "tracks.db"));
        pool = Executors.newFixedThreadPool(6);
    }

    @Test
    public void testImport() throws Exception {

        final List<Path> pathList = audiofingerprint.Setup.listTracks(path, filetypes);

        for (Path path : pathList) {
            pool.execute(() -> {
                try {
                    importTrack(path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        LOGGER.info("done");
    }

    private void importTrack(Path path) throws IOException, UnsupportedAudioFileException, SQLException {

        final SignalPump<AudioBuffer> pump =
                new SignalPump<>(new AudioSignalSource(audiofingerprint.Setup.AUDIOSOURCE_SETUP.open(path)));
        final SignalPipeline<AudioBuffer, Void> pipe = audiofingerprint.Setup.FINGERPRINT_SETUP
                .build()
                .joinWith(new audiofingerprint.DBWriter(db, path));
        pump.add(pipe);
        pump.pump();
        final Fingerprint.Info info = (Fingerprint.Info) pipe.getProcessorWithId(Fingerprint.Info.ID);

        LOGGER.debug("{} got {} unique of {} hashes", path.getFileName().toString(), info.getNumUniqueHashes(), info.getNumHashes());
    }
}
