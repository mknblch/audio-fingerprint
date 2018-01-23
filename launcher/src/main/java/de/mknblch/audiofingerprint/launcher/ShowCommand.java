package de.mknblch.audiofingerprint.launcher;

import com.tagtraum.jipes.SignalPipeline;
import com.tagtraum.jipes.SignalPump;
import com.tagtraum.jipes.audio.AudioBuffer;
import com.tagtraum.jipes.audio.AudioSignalSource;
import de.mknblch.audiofingerprint.processor.Fingerprint;
import de.mknblch.audiofingerprint.processor.ImageAggregator;
import de.mknblch.audiofingerprint.processor.LocalMaximum;
import de.mknblch.params.Unreliable;
import de.mknblch.params.annotations.Argument;
import de.mknblch.params.annotations.Command;
import de.mknblch.params.annotations.Description;
import de.mknblch.params.transformer.PathTransformer;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

import static de.mknblch.audiofingerprint.launcher.FingerprintSetup.*;

/**
 * Created by mknblch on 24.08.2015.
 */
@Command(trigger = {"show"})
@Description("Show spectrogram image of the track")
public class ShowCommand implements Unreliable {

    @Description("Path to the Track")
    @Argument(trigger = "-p", transformer = PathTransformer.class)
    private Path path;

    @Override
    public void run() throws IOException, UnsupportedAudioFileException {

        final SignalPump<AudioBuffer> pump =
                new SignalPump<>(new AudioSignalSource(AUDIOSOURCE_SETUP.open(path)));
        final SignalPipeline<AudioBuffer, BufferedImage> pipe = FINGERPRINT_SETUP
                .build()
                .joinWith(new ImageAggregator(LocalMaximum.DRAW_FUNC, Fingerprint.DRAW_FUNCTION));
        pump.add(pipe);
        final BufferedImage bi = (BufferedImage) pump.pump().get(ImageAggregator.ID);
        final Fingerprint.Info info = (Fingerprint.Info) pipe.getProcessorWithId(Fingerprint.Info.ID);
        show(bi, info.getNumHashes(), info.getNumUniqueHashes());
    }

    private void show(BufferedImage img, int numberOfHashes, int uniqueHashes) {

        javax.swing.SwingUtilities.invokeLater(() -> {

            final JFrame frame = new JFrame(String.format("%s (%d/%d unique hashes)", path.getFileName().toString(), uniqueHashes, numberOfHashes));
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            final ScrollPane comp = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
            comp.add(new JLabel(new ImageIcon(img)));
            frame.getContentPane().add(comp);
            frame.getContentPane().setBackground(Color.BLACK);
            frame.setPreferredSize(new java.awt.Dimension(800, 600));
            frame.setLocationRelativeTo(null);
            frame.pack();
            frame.setVisible(true);

        });
    }
}
