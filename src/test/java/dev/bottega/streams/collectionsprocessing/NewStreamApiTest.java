package dev.bottega.streams.collectionsprocessing;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Newer additions to the Streams API (JDK 9-16+, pre-gatherers):
 * {@code toList()}, {@code mapMulti()}, {@code ofNullable()}, {@code takeWhile}/{@code dropWhile},
 * the 3-arg {@code Stream.iterate} and {@code Stream.concat}.
 */
public class NewStreamApiTest {

    // ---- toList() (JDK 16): unmodifiable result ----

    @Test
    public void toListReturnsUnmodifiableList() {
        List<Integer> list = Stream.of(3, 1, 2).toList();
        assertThat(list).containsExactly(3, 1, 2);
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> list.add(4));
    }

    // ---- mapMulti (JDK 16): 0..n elements per input ----

    @Test
    public void mapMultiCanEmitSeveralElementsPerInput() {
        List<String> result = Stream.of("a", "bc")
                .mapMulti((String s, Consumer<String> downstream) -> {
                    downstream.accept(s);
                    downstream.accept(s.toUpperCase());
                })
                .toList();
        assertThat(result).containsExactly("a", "A", "bc", "BC");
    }

    @Test
    public void mapMultiCanDropElements() {
        List<Integer> result = Stream.of(1, 2, 3, 4, 5)
                .mapMulti((Integer n, Consumer<Integer> downstream) -> {
                    if (n % 2 == 0) {
                        downstream.accept(n);
                    }
                })
                .toList();
        assertThat(result).containsExactly(2, 4);
    }

    // ---- ofNullable (JDK 9) ----

    @Test
    public void ofNullableEmitsElementOrNothing() {
        assertThat(Stream.ofNullable("x").toList()).containsExactly("x");
        assertThat(Stream.ofNullable(null).toList()).isEmpty();
    }

    // ---- takeWhile / dropWhile (JDK 9): prefix operations ----

    @Test
    public void takeWhileStopsAtFirstMismatch() {
        assertThat(Stream.of(1, 2, 3, 4, 5, 0, 6).takeWhile(n -> n < 4).toList())
                .containsExactly(1, 2, 3);
    }

    @Test
    public void dropWhileDropsPrefixUpToFirstMatch() {
        assertThat(Stream.of(1, 2, 3, 4, 5, 0, 6).dropWhile(n -> n < 4).toList())
                .containsExactly(4, 5, 0, 6);
    }

    // ---- Stream.iterate(seed, hasNext, next) (JDK 9): bounded sequence ----

    @Test
    public void iterateWithPredicateProducesFiniteSequence() {
        assertThat(Stream.iterate(1, n -> n <= 16, n -> n * 2).toList())
                .containsExactly(1, 2, 4, 8, 16);
    }

    // ---- Stream.concat (JDK 9) ----

    @Test
    public void concatCombinesTwoStreamsInOrder() {
        assertThat(Stream.concat(Stream.of(1, 2), Stream.of(3, 4)).toList())
                .containsExactly(1, 2, 3, 4);
    }
}
