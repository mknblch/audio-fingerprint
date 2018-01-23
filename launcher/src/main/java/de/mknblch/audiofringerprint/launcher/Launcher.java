package de.mknblch.audiofringerprint.launcher;

import de.mknblch.params.Params;

/**
 * @author mknblch
 */
public class Launcher {

    public static void main(String[] args) {


        new Params()
                .add(ShowCommand.class)
                .runOrDie(args);
    }
}
