package de.mknblch.audiofp.processor;

import com.tagtraum.jipes.SignalProcessor;
import com.tagtraum.jipes.SignalProcessorSupport;
import de.mknblch.audiofp.Feature;
import de.mknblch.audiofp.common.Ring;

import java.io.IOException;

/**
 * @author mknblch
 */
public abstract class FeatureQueue<T extends Feature> implements SignalProcessor<T, T> {

    protected final SignalProcessorSupport<T> signalProcessorSupport = new SignalProcessorSupport<>();
    protected final Object id;
    protected final Ring<Feature> queue;
    protected T output;

    public FeatureQueue(Object id, int size) {
        this.id = id;
        queue = new Ring<>(new Feature[size]);
    }

    public abstract T process(Ring<Feature> featureRingQueue);

    @Override
    public void process(T i) throws IOException {
        queue.put(i);
        final T process = process(queue);
        if (null != process) {
            signalProcessorSupport.process(process);
        }
    }

    public int size() {
        return queue.size();
    }

    @Override
    public void flush() throws IOException {
        signalProcessorSupport.flush();
    }

    @Override
    public T getOutput() throws IOException {
        return output;
    }

    @Override
    public Object getId() {
        return id;
    }

    public <O2> SignalProcessor<T, O2> connectTo(final SignalProcessor<T, O2> signalProcessor) {
        return signalProcessorSupport.connectTo(signalProcessor);
    }

    public <O2> SignalProcessor<T, O2> disconnectFrom(final SignalProcessor<T, O2> signalProcessor) {
        return signalProcessorSupport.disconnectFrom(signalProcessor);
    }

    public SignalProcessor<T, ?>[] getConnectedProcessors() {
        return signalProcessorSupport.getConnectedProcessors();
    }

}
