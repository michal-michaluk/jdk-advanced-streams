package dev.bottega.streams.collectionsprocessing;

import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DirectCreationOfStreamTest {

    @Test
    public void of() throws Exception {
        Stream.of(1.0, 2.0, 3.0)
                .mapToInt(Double::intValue)
                .mapToObj(i -> "a" + i)
                .forEach(System.out::println);
    }

    @Test
    public void range() throws Exception {
        IntStream.range(1, 40)
                .mapToObj(i -> "a" + i)
                .forEach(System.out::println);
    }

    @Test
    public void builder() throws Exception {
        DoubleStream.builder()
                .add(1.0)
                .add(2.0)
                .add(3.0)
                .build()
                .forEach(System.out::println);
    }

    @Test
    public void random() throws Exception {
        new Random().doubles(1_000)
                .mapToObj(d -> "next random value is: " + d)
                .collect(Collectors.toList());
    }

    @Test
    public void iterate() throws Exception {
        Stream.iterate(1, integer -> (integer + 10) / integer)
                .limit(100)
                .forEach(System.out::println);
    }
}
