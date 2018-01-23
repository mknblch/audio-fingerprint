package de.mknblch.audiofingerprint.launcher;

import de.mknblch.audiofingerprint.AudioSource;
import de.mknblch.audiofingerprint.PipeBuilder;
import de.mknblch.audiofingerprint.utils.Tones;
import de.mknblch.audiofingerprint.utils.WindowSizeFunction;

/**
 * @author mknblch
 */
public class FingerprintSetup {

    public static final AudioSource AUDIOSOURCE_SETUP = new AudioSource()
            .withSampleAndFrameRate(44100 / 2);

    public static final PipeBuilder FINGERPRINT_SETUP = new PipeBuilder()
            .withFrames(2048)
            .withDownsample(3)
            .withWhiteningKillFactor(1.0 / 3)
            .withCutFreqBottom(Tones.A.shift(-4))
            .withCutFreqTop(Tones.A.shift(3))
            .withLocalMaximumLookupWidth(7)
            .withLocalMaximumLookupHeightFunction(WindowSizeFunction.adaptive(5, 18))
            .withHashingLookupHeightFunction(WindowSizeFunction.adaptive(12, 36))
            .withMaxHashesPerFrame(8)
            .withMaxHashesPerReference(1)
            .withHashScoreFunction((i0, i1, f0, f1, vf0, vf1) -> vf0 + vf1 / 3, true);
}
