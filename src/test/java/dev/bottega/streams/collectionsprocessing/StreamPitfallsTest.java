package dev.bottega.streams.collectionsprocessing;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Examples from nice article:
 * https://blog.jooq.org/2014/06/13/java-8-friday-10-subtle-mistakes-when-using-the-streams-api/
 * <p>
 * <p>
 * Created by michal on 22.10.2016.
 */
public class StreamPitfallsTest {

    @Test
    public void reusingStreams() throws Exception {
        IntStream stream = IntStream.of(1, 2);
        stream.forEach(System.out::println);

        // That was fun! Let's do it again!
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() ->
                        stream.forEach(System.out::println)
                );

    }

    @Test
    @Disabled
    public void infiniteStreams() throws Exception {
        // Will run indefinitely
        IntStream.iterate(0, i -> i + 1)
                .forEach(System.out::println);

    }

    @Test
    @Disabled
    public void subtleInfiniteStreams() throws Exception {
        IntStream.iterate(0, i -> (i + 1) % 2)
                .distinct()
                .limit(10)
                .forEach(System.out::println);

    }

    @Test
    @Disabled
    public void parallelInfiniteStreams() throws Exception {
        IntStream.iterate(0, i -> (i + 1) % 2)
                .parallel()
                .distinct()
                .limit(10)
                .forEach(System.out::println);
    }

    @Test
    public void operationsOrder() throws Exception {
        IntStream.iterate(0, i -> (i + 1) % 2)
                .limit(10)
                .distinct()
                .forEach(System.out::println);

    }

    @Test
    public void operationsOrder2() throws Exception {
        IntStream.iterate(0, i -> i + 1)
                .limit(10) // LIMIT
                .skip(5)   // OFFSET
                .forEach(System.out::println);

    }

    @Test
    public void modifyingBackingCollection() throws Exception {
        // Of course, we create this list using streams:
        List<Integer> list =
                IntStream.range(0, 10)
                        .boxed()
                        .collect(Collectors.toCollection(ArrayList::new));

        assertThatExceptionOfType(ConcurrentModificationException.class)
                .isThrownBy(() ->
                        list.stream()
                                // remove(Object), not remove(int)!
                                .peek(list::remove)
                                .forEach(System.out::println)
                );
    }

    @Test
    public void modifyingBackingCollectionAfterSort() throws Exception {
        // Of course, we create this list using streams:
        List<Integer> list =
                IntStream.range(0, 10)
                        .boxed()
                        .collect(Collectors.toCollection(ArrayList::new));

        list.stream()
                .sorted()
                .peek(list::remove)
                .forEach(System.out::println);
    }

    @Test
    public void modifyingBackingCollectionInParallel() throws Exception {
        // Of course, we create this list using streams:
        List<Integer> list =
                IntStream.range(0, 10)
                        .boxed()
                        .collect(Collectors.toCollection(ArrayList::new));

        list.stream()
                .sorted()
                .parallel()
                .peek(list::remove)
                .forEach(System.out::println);

    }

    @Test
    public void notConsumed() throws Exception {
        IntStream.range(1, 5)
                .peek(System.out::println)
                .peek(i -> {
                    if (i == 5)
                        throw new RuntimeException("bang");
                });

    }

    @Test
    public void filesWalkIssue() throws Exception {
        Files.walk(Paths.get("."))
                .filter(p -> !p.toFile().getName().startsWith("."))
                .forEach(System.out::println);

        Files.walk(Paths.get("."))
                .filter(p -> !p.toString().contains(File.separator + "."))
                .forEach(System.out::println);

    }
}
