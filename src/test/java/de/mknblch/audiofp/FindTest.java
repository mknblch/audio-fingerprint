package de.mknblch.audiofp;

import com.tagtraum.jipes.SignalPump;
import com.tagtraum.jipes.audio.AudioBuffer;
import com.tagtraum.jipes.audio.AudioSignalSource;
import de.mknblch.audiofp.common.TimestampSignalSource;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author mknblch
 */
public class FindTest {

    private static final Logger LOGGER = getLogger(FindTest.class);

    public static final int BUCKETS = 100;
    private static Path path = Paths.get("");

    private static audiofingerprint.H2Dao db;

    @BeforeClass
    public static void setup() throws SQLException {
        db = new audiofingerprint.H2Dao(Paths.get("./", "tracks.db"));
    }

    @Test
    public void testFind() throws Exception {

        LOGGER.info("scanning {}", path);
        findTrack(path, 0, 500)
                .forEach(System.out::println);

    }

    private List<String> findTrack(Path path, long start, long length) throws IOException, UnsupportedAudioFileException {

        final SignalPump<AudioBuffer> pump =
                new SignalPump<>(
                        new TimestampSignalSource<AudioBuffer>(
                                new AudioSignalSource(audiofingerprint.Setup.AUDIOSOURCE_SETUP.open(path)),
                                start,
                                length));

        pump.add(audiofingerprint.Setup.FINGERPRINT_SETUP
                .build()
                .joinWith(new audiofingerprint.DBFinder(BUCKETS, db)));

        return (List<String>) pump.pump().get(audiofingerprint.DBFinder.ID);
    }

}
