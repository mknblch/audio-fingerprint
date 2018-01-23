package de.mknblch.audiofingerprint.launcher;

import de.mknblch.params.Params;
import de.mknblch.params.Unreliable;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author mknblch
 */
public class ShowCommandTest {

    public static final Path SUNNY = Paths.get("C:/data/test/tracks/Bobby Hebb - Sunny (Anaa Remix).mp3");

    @Test
    public void test() throws Exception {

        final Unreliable show = new Params()
                .add(ShowCommand.class)
                .build("show", "-p", SUNNY.toAbsolutePath().toString());

        show.run();

        Thread.sleep(20000);
    }

}