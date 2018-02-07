package de.mknblch.audiofp;

import com.tagtraum.jipes.SignalPump;
import com.tagtraum.jipes.audio.AudioBuffer;
import de.mknblch.audiofp.common.TimestampSignalSource;
import de.mknblch.audiofp.db.DBFinder;
import de.mknblch.audiofp.db.H2Dao;
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

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Match track based on first 10 seconds
 *
 * @author mknblch
 */
@Ignore
public class MatchingTest {

    private static final Logger LOGGER = getLogger(MatchingTest.class);

    /**
     * magic value
     *
     * number of timebuckets for track matching.
     * should be somehow related to the recording duration for matching
     */
    public static final int BUCKETS = 100;

    // TODO change
    private static Path path = Paths.get("C:/data/test/tracks/Bobby Hebb - Sunny (Anaa Remix).mp3");

    private static H2Dao db;

    @BeforeClass
    public static void setup() throws SQLException {
        db = new H2Dao(Paths.get("./", "tracks.db"));
    }

    @Test
    public void testFind() throws Exception {

        LOGGER.info("scanning {}", path);
        findTrack(path, 0, 10_000)
                .forEach(System.out::println);

    }

    private List<String> findTrack(Path path, long start, long length) throws IOException, UnsupportedAudioFileException {

        final SignalPump<AudioBuffer> pump =
                new SignalPump<AudioBuffer>(
                        new TimestampSignalSource<AudioBuffer>(
                                Setup.AUDIOSOURCE_SETUP.open(path),
                                start,
                                length));
//
//        final SignalPump<AudioBuffer> pump =
//                new SignalPump<AudioBuffer>(new AudioSignalSource(Setup.AUDIOSOURCE_SETUP.open(path)));

        pump.add(Setup.FINGERPRINT_SETUP
                .build()
//                .joinWith(new DuplicateFilter())
                .joinWith(new DBFinder(BUCKETS, db)));

        return (List<String>) pump.pump().get(DBFinder.ID);
    }

}
