/*
 * =================================================
 * Copyright 2013 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package de.mknblch.audiofp.common;

import com.tagtraum.jipes.SignalSource;
import com.tagtraum.jipes.audio.AudioBuffer;
import com.tagtraum.jipes.audio.AudioSignalSource;

import javax.sound.sampled.AudioInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Timestamp limited {@link SignalSource}.
 * Delivers {@link AudioBuffer}s as long as the source delivers them and
 * {@link AudioBuffer#getTimestamp(TimeUnit)} &lt;
 *
 * @param <T> subtype of {@link AudioBuffer}
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>, mknblch
 */
public class TimestampSignalSource<T extends AudioBuffer> implements SignalSource<T>, Closeable {

    private final SignalSource<T> signalSource;
    private final long maxTimestamp;
    private final TimeUnit timeUnit;
    private final long minTimestampInMS;
    private boolean closed;

    private boolean skipped = false;

    /**
     * Creates a timestamp limited signal source.
     *
     */
    public TimestampSignalSource(final AudioInputStream inputStream, final long minTimestampInMS, final long length) {
        this(inputStream, minTimestampInMS, length, TimeUnit.MILLISECONDS);
    }

    /**
     * Creates a timestamp limited signal source.
     *
     * @param timeUnit time unit for the timestamp
     */
    public TimestampSignalSource(final AudioInputStream inputStream, final long minTimestampInMS, final long length, final TimeUnit timeUnit) {
        this.signalSource = (SignalSource<T>) new AudioSignalSource(inputStream);
        this.timeUnit = timeUnit;
        this.minTimestampInMS = minTimestampInMS;
        this.maxTimestamp = minTimestampInMS + length;
    }

    /**
     * Warpped signal source.
     *
     * @return signal source
     */
    public SignalSource<T> getSignalSource() {
        return signalSource;
    }

    @Override
    public T read() throws IOException {
        if (!skipped) {
            skip();
        }
        final T buffer = signalSource.read();
        final T result = buffer != null && buffer.getTimestamp(timeUnit) < maxTimestamp ? buffer : null;
        // auto close, when we're done
        if (result == null && !closed) {
            close();
        }
        return result;
    }

    private void skip() throws IOException {
        skipped = true;
        T buffer;
        do {
            buffer = signalSource.read();
        } while (buffer != null && buffer.getTimestamp(timeUnit) < minTimestampInMS);
    }

    public void reset() {
        signalSource.reset();
    }

    /**
     * Closes the underlying {@link SignalSource}, if it implements {@link Closeable}.
     *
     * @throws IOException
     */
    public void close() throws IOException {
        if (signalSource != null && signalSource instanceof Closeable && !closed) {
            closed = true;
            ((Closeable) signalSource).close();
        }
    }

    @Override
    public String toString() {
        return "TimestampLimitedSignalSource{" +
                "maxTimestamp=" + maxTimestamp +
                " " + timeUnit.toString().toLowerCase() +
                ", signalSource=" + signalSource +
                '}';
    }
}
