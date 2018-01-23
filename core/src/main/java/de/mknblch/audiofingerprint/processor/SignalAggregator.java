package de.mknblch.audiofingerprint.processor;

import com.tagtraum.jipes.SignalProcessor;
import com.tagtraum.jipes.SignalProcessorSupport;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.IntUnaryOperator;

/**
 * @author mknblch
 */
public abstract class SignalAggregator<I, O> implements SignalProcessor<I, O> {

    protected final Object id;
    private I[] data;
    private int offset = 0;
    private IntUnaryOperator growFunction = i -> i + i / 2;
    private O output = null;

    protected final SignalProcessorSupport<O> signalProcessorSupport = new SignalProcessorSupport<>();

    public SignalAggregator(Object id, I[] data) {
        this.id = id;
        this.data = data;
    }

    public void emit(O output) throws IOException {
        this.output = output;
        signalProcessorSupport.process(output);
    }

    protected abstract void process(I[] data) throws IOException;

    public SignalAggregator<I, O> reset() {
        Arrays.fill(data, null);
        offset = 0;
        return this;
    }

    @Override
    public O getOutput() throws IOException {
        return output;
    }

    @Override
    public void process(I i) throws IOException {
        add(i);
    }

    @Override
    public void flush() throws IOException {
        process(Arrays.copyOf(data, offset));
    }

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public <O2> SignalProcessor<O, O2> connectTo(SignalProcessor<O, O2> signalProcessor) {
        return signalProcessorSupport.connectTo(signalProcessor);
    }

    @Override
    public <O2> SignalProcessor<O, O2> disconnectFrom(SignalProcessor<O, O2> signalProcessor) {
        return signalProcessorSupport.disconnectFrom(signalProcessor);
    }

    @Override
    public SignalProcessor<O, ?>[] getConnectedProcessors() {
        return signalProcessorSupport.getConnectedProcessors();
    }

    private void add(I input) {
        if (data.length >= offset) {
            data = Arrays.copyOf(data, growFunction.applyAsInt(offset + 1));
        }
        data[offset++] = input;
    }
}
