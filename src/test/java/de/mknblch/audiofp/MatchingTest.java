package de.mknblch.audiofp;

import com.tagtraum.jipes.SignalPump;
import com.tagtraum.jipes.audio.AudioBuffer;
import de.mknblch.audiofp.buffer.DB;
import de.mknblch.audiofp.common.TimestampSignalSource;
import de.mknblch.audiofp.db.DBFinder;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    // TODO change
    private static Path dbPath = Paths.get("C:/data/db.db");

    private static Path path = Paths.get("C:/data/test/mix");

    private static DB db;

    @BeforeClass
    public static void setup() throws IOException, InterruptedException {
        db = DB.load(dbPath);
        System.out.println(db);
    }

    @Test
    public void testFind() throws Exception {

        LOGGER.info("scanning {}", path);

        Files.walk(path)
                .forEach(p -> {
                    if (p.toFile().isFile()) {
                        try {
                            System.out.println(p);
                            findTrack(p, 0, 4000)
                                    .forEach(t -> System.out.println("\t" + t));
                        } catch (IOException | UnsupportedAudioFileException e) {
                            e.printStackTrace();
                        }
                    }
                });


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
                .joinWith(new DBFinder(db)));

        return (List<String>) pump.pump().get(DBFinder.ID);
    }

}
