package de.mknblch.audiofingerprint.launcher;

import de.mknblch.params.transformer.Transformer;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author mknblch
 */
public class FileTypeTransformer implements Transformer<List<String>> {

    @Override
    public List<String> transform(String[] strings) throws Exception {
        return Stream.of(strings[0].split(","))
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }
}
