package de.mknblch.audiofp;


import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author mknblch
 */
public class AudioSource  {

    public static final AudioSource INSTANCE = new AudioSource();

    // defaults
    private float sampleRate = 44100;
    private int sampleSizeInBits = 16;
    private int channels = 2;
    private int frameSize = 4;
    private boolean bigEndian = false;
    private float frameRate = sampleRate;
    private AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;

    public AudioSource withSampleRate(float sampleRate) {
        this.sampleRate = sampleRate;
        return this;
    }
    public AudioSource withSampleAndFrameRate(float rate) {
        this.sampleRate = rate;
        this.frameRate = rate;
        return this;
    }

    public AudioSource withSampleSizeInBits(int sampleSizeInBits) {
        this.sampleSizeInBits = sampleSizeInBits;
        return this;
    }

    public AudioSource withChannels(int channels) {
        this.channels = channels;
        return this;
    }

    public AudioSource withFrameSize(int frameSize) {
        this.frameSize = frameSize;
        return this;
    }

    public AudioSource withBigEndian(boolean bigEndian) {
        this.bigEndian = bigEndian;
        return this;
    }

    public AudioSource withFrameRate(float frameRate) {
        this.frameRate = frameRate;
        return this;
    }

    public AudioSource withEncoding(AudioFormat.Encoding encoding) {
        this.encoding = encoding;
        return this;
    }

    public AudioInputStream open(String path) throws IOException, UnsupportedAudioFileException {
        return open(Paths.get(path));
    }

    public AudioInputStream open(Path path) throws IOException, UnsupportedAudioFileException {
        final AudioInputStream inputStream = AudioSystem.getAudioInputStream(path.toFile());
        final AudioFormat format = inputStream.getFormat();
        if (Math.abs(format.getSampleRate() - sampleRate) > 0 ||
                Math.abs(format.getFrameRate() - frameRate) > 0 ||
                format.getSampleSizeInBits() != sampleSizeInBits ||
                format.getChannels() != channels ||
                format.getFrameSize() != frameSize ||
                !format.getEncoding().equals(encoding) ||
                format.isBigEndian() != bigEndian) {
            return AudioSystem.getAudioInputStream(
                    new AudioFormat(
                            encoding,
                            sampleRate,
                            sampleSizeInBits,
                            channels,
                            frameSize,
                            frameRate,
                            bigEndian),
                    inputStream);
        }
        return inputStream;
    }

}
