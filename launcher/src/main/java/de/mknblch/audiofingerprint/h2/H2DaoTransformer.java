package de.mknblch.audiofingerprint.h2;

import de.mknblch.params.transformer.Transformer;

import java.nio.file.Paths;

/**
 * @author mknblch
 */
public class H2DaoTransformer implements Transformer<H2Dao> {
    @Override
    public H2Dao transform(String[] strings) throws Exception {
        return new H2Dao(Paths.get(strings[0]));
    }
}
