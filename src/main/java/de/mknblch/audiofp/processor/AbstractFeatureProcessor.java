package de.mknblch.audiofp.processor;

import com.tagtraum.jipes.SignalProcessor;
import com.tagtraum.jipes.SignalProcessorSupport;
import de.mknblch.audiofp.Feature;

import java.io.IOException;

/**
 * @author mknblch
 */
public abstract class AbstractFeatureProcessor implements SignalProcessor<Feature, Feature> {

    private final SignalProcessorSupport<Feature> support;
    private Feature feature;

    public AbstractFeatureProcessor() {
        support = new SignalProcessorSupport<>();
    }

    @Override
    public void flush() throws IOException {
        support.flush();
    }

    @Override
    public Object getId() {
        return this.getClass().getName();
    }

    @Override
    public Feature getOutput() throws IOException {
        return feature;
    }

    @Override
    public <O2> SignalProcessor<Feature, O2> connectTo(SignalProcessor<Feature, O2> signalProcessor) {
        return support.connectTo(signalProcessor);
    }

    @Override
    public <O2> SignalProcessor<Feature, O2> disconnectFrom(SignalProcessor<Feature, O2> signalProcessor) {
        return support.disconnectFrom(signalProcessor);
    }

    @Override
    public SignalProcessor<Feature, ?>[] getConnectedProcessors() {
        return support.getConnectedProcessors();
    }

    protected void emit(Feature feature) throws IOException {
        this.feature = feature;
        support.process(feature);
    }

}
