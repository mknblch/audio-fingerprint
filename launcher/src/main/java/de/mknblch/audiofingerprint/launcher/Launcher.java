package de.mknblch.audiofingerprint.launcher;

import de.mknblch.params.Params;

/**
 * @author mknblch
 */
public class Launcher {

    private static final String DESCRIPTION = "audio fingerprint";

    public static void main(String[] args) {

        new Params()
                .setDescription(DESCRIPTION)
                .add(ShowCommand.class)
                .add(ImportCommand.class)
                .runOrDie(args);
    }
}
