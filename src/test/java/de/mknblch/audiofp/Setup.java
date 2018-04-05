package de.mknblch.audiofp;

import utils.Tones;
import utils.WindowSizeFunction;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author mknblch
 */
public class Setup {

    private static final Logger LOGGER = getLogger(Setup.class);

    public static final AudioSource AUDIOSOURCE_SETUP = new AudioSource()
            .withSampleAndFrameRate(44100 / 2);

    public static final PipeBuilder FINGERPRINT_SETUP = new PipeBuilder()
            .withFrames(2048)
            .withDownsample(3)
            .withWhiteningKillFactor(1.0 / 3)
            .withCutFreqBottom(Tones.A.shift(-4))
            .withCutFreqTop(Tones.A.shift(4))
            .withLocalMaximumLookupWidth(5)
            .withLocalMaximumLookupHeightFunction(WindowSizeFunction.adaptive(7, 15))
            .withHashingLookupHeightFunction(WindowSizeFunction.adaptive(12, 36))
            .withMaxHashesPerFrame(8)
            .withMaxHashesPerReference(1)
            .withHashScoreFunction((i0, i1, f0, f1, vf0, vf1) -> vf0 + vf1 / 2f, true);

    public static List<Path> listTracks(Path path, String filetypes) throws IOException {

        LOGGER.info("scanning for tracks in {}", path.toString());

        final List<Path> pathList = Files.walk(path)
                .filter(p -> filetypes.contains(fileType(p)))
                .collect(Collectors.toList());

        return pathList;
    }


    private static final String fileType(Path path) {
        final String string = path.getFileName().toString();
        return string.substring(string.lastIndexOf('.') + 1).toLowerCase();
    }
}
