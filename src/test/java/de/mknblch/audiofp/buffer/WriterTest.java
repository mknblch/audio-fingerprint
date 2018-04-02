package de.mknblch.audiofp.buffer;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * @author mknblch
 */
public class WriterTest {

    @Test
    public void test() throws Exception {


        final ArrayList<Object> objects = new ArrayList<>();
        objects.add(42);
        objects.add("Hello World");
        objects.add(3.14f);


        final ArrayList<Object> sub = new ArrayList<>();
        sub.add(1);
        sub.add(2);
        sub.add(3);
        objects.add(sub);


        final ByteBuffer buff = Writer.write(objects);

        System.out.println(Arrays.toString(buff.array()));

        buff.flip();

        final Object read = Reader.read(buff);

        System.out.println(read);
    }

    @Test
    public void testMap() throws Exception {

        final HashMap<Object, Object> map = new HashMap<>();
        map.put(1, "hello");
        map.put(42, "world");


        final ByteBuffer buff = Writer.write(map);

        System.out.println(Arrays.toString(buff.array()));

        buff.flip();

        final Object read = Reader.read(buff);

        System.out.println(read);
    }
}