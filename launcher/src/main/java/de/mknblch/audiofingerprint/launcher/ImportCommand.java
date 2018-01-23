package de.mknblch.audiofingerprint.launcher;

import com.tagtraum.jipes.SignalPipeline;
import com.tagtraum.jipes.SignalPump;
import com.tagtraum.jipes.audio.AudioBuffer;
import com.tagtraum.jipes.audio.AudioSignalSource;
import de.mknblch.audiofingerprint.processor.Fingerprint;
import de.mknblch.audiofingerprint.h2.DBCachedHashWriter;
import de.mknblch.audiofingerprint.h2.H2Dao;
import de.mknblch.audiofingerprint.h2.H2DaoTransformer;
import de.mknblch.params.Unreliable;
import de.mknblch.params.annotations.Argument;
import de.mknblch.params.annotations.Command;
import de.mknblch.params.annotations.Description;
import de.mknblch.params.transformer.PathTransformer;
import org.slf4j.Logger;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static de.mknblch.audiofingerprint.launcher.FingerprintSetup.AUDIOSOURCE_SETUP;
import static de.mknblch.audiofingerprint.launcher.FingerprintSetup.FINGERPRINT_SETUP;
import static org.slf4j.LoggerFactory.getLogger;

/**
 */
@Command(trigger = {"import"})
@Description("Show spectrogram image of the track")
public class ImportCommand implements Unreliable {

    private static final Logger LOGGER = getLogger(ImportCommand.class);

    @Description("Path to the db. A new file is created if it does not exist.")
    @Argument(trigger = "-db", transformer = H2DaoTransformer.class)
    private H2Dao db;

    @Description("Path to the track directory.")
    @Argument(trigger = "-t", transformer = PathTransformer.class)
    private Path tracksPath;

    @Description("Maximum directory scan depth.")
    @Argument(trigger = "-depth", optional = true)
    private int depth = Integer.MAX_VALUE;

    @Description("Number of threads.")
    @Argument(trigger = "-threads", optional = true)
    private int threads = 4;

    @Description("Comma separated list of allowed filetypes.")
    @Argument(trigger = "-ftype", optional = true, transformer = FileTypeTransformer.class)
    private List<String> filetypes = Arrays.asList("wav", "mp3");

    @Override
    public void run() throws IOException, UnsupportedAudioFileException, SQLException {

        LOGGER.info("scanning for tracks in {}", tracksPath.toString());

        final List<Path> pathList = Files.walk(tracksPath, depth)
                .filter(p -> filetypes.contains(fileType(p)))
                .collect(Collectors.toList());

        LOGGER.debug("importing {} tracks", pathList.size());

        final ExecutorService pool = Executors.newFixedThreadPool(threads);

        for (Path path : pathList) {
            pool.execute(() -> {
                try {
                    importTrack(path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void importTrack(Path path) throws IOException, UnsupportedAudioFileException, SQLException {

        LOGGER.debug("importing {}", path.getFileName().toString());

        final SignalPump<AudioBuffer> pump =
                new SignalPump<>(new AudioSignalSource(AUDIOSOURCE_SETUP.open(path)));
        final SignalPipeline<AudioBuffer, Void> pipe = FINGERPRINT_SETUP
                .build()
                .joinWith(new DBCachedHashWriter(db, path));
        pump.add(pipe);
        final Fingerprint.Info info = (Fingerprint.Info) pipe.getProcessorWithId(Fingerprint.Info.ID);

        LOGGER.debug("got {} unique of {} hashes", info.getNumUniqueHashes(), info.getNumHashes());
    }

    private static final String fileType(Path path) {
        final String string = path.getFileName().toString();
        return string.substring(string.lastIndexOf('.') + 1).toLowerCase();
    }
}
