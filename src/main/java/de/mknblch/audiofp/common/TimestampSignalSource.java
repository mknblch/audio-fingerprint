/*
 * =================================================
 * Copyright 2013 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package de.mknblch.audiofp.common;

import com.tagtraum.jipes.SignalSource;
import com.tagtraum.jipes.audio.AudioBuffer;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Time limited audio source
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>, mknblch
 */
public class TimestampSignalSource<T extends AudioBuffer> implements SignalSource<T>, Closeable {

    private final SignalSource<T> signalSource;
    private final long maxTimestamp;
    private final long startTimestamp;
    private final TimeUnit timeUnit;
    private boolean closed;
    private boolean skipped;

    /**
     * Creates a timestamp limited signal source.
     *
     * @param signalSource signal source to read from
     * @param length in ms
     */
    public TimestampSignalSource(final SignalSource<T> signalSource, final long startTimestampInMS, final long length) {
        this(signalSource, startTimestampInMS, length, TimeUnit.MILLISECONDS);
    }

    /**
     * Creates a timestamp limited signal source.
     *
     * @param signalSource signal source to read from
     * @param timeUnit time unit for the timestamp
     * @param length in ms
     */
    public TimestampSignalSource(final SignalSource<T> signalSource, final long startTimestampInMS, final long length, final TimeUnit timeUnit) {
        this.signalSource = signalSource;
        this.startTimestamp = startTimestampInMS;
        this.timeUnit = timeUnit;
        this.maxTimestamp = startTimestampInMS + length;
        this.skipped = false;
    }

    /**
     * @return start timestamp
     */
    public long getStartTimestamp() {
        return startTimestamp;
    }

    /**
     * Max timestamp up to which to read.
     *
     * @param timeUnit desired unit
     * @return timestamp in ms
     */
    public long getMaxTimestamp(final TimeUnit timeUnit) {
        return timeUnit.convert(maxTimestamp, this.timeUnit);
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
        } while (buffer != null && buffer.getTimestamp(timeUnit) < startTimestamp);
    }

    public void reset() {
        signalSource.reset();
        skipped = false;
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
