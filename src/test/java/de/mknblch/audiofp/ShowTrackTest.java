package de.mknblch.audiofp;

import com.tagtraum.jipes.SignalPipeline;
import com.tagtraum.jipes.SignalPump;
import com.tagtraum.jipes.audio.AudioBuffer;
import com.tagtraum.jipes.audio.AudioSignalSource;
import de.mknblch.audiofp.processor.Fingerprint;
import de.mknblch.audiofp.processor.ImageAggregator;
import de.mknblch.audiofp.processor.LocalMaximum;
import org.junit.Ignore;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Renders spectrogram and visualizes all hashes.
 *
 * @author mknblch
 */
@Ignore
public class ShowTrackTest {

    // TODO change
    public static final Path PATH = Paths.get("C:/data/test/tracks/Bobby Hebb - Sunny (Anaa Remix).mp3");
    
    @Test
    public void testShowTrack() throws Exception {

        final SignalPump<AudioBuffer> pump =
                new SignalPump<>(new AudioSignalSource(Setup.AUDIOSOURCE_SETUP.open(PATH)));
        final SignalPipeline<AudioBuffer, BufferedImage> pipe = Setup.FINGERPRINT_SETUP
                .build()
                .joinWith(new ImageAggregator(LocalMaximum.DRAW_FUNC, Fingerprint.DRAW_FUNCTION));
        pump.add(pipe);
        final BufferedImage bi = (BufferedImage) pump.pump().get(ImageAggregator.ID);
        final Fingerprint.Info info = (Fingerprint.Info) pipe.getProcessorWithId(Fingerprint.Info.ID);

        show(bi, info.getNumHashes(), info.getNumUniqueHashes());
    }

    private void show(BufferedImage img, int numberOfHashes, int uniqueHashes) throws InterruptedException {

        javax.swing.SwingUtilities.invokeLater(() -> {

            final JFrame frame = new JFrame(String.format("%d/%d unique hashes", uniqueHashes, numberOfHashes));
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

        Thread.sleep(20_000);
    }
}
