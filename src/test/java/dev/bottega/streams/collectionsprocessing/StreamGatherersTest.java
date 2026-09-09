package dev.bottega.streams.collectionsprocessing;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Gatherers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Stream Gatherers (JEP 485, JDK 24+) — the newest addition to the Streams API.
 *
 * <p>{@code Stream#gather(Gatherer)} composes a <em>stateful intermediate operation</em>.
 * This file exercises the built-in {@link Gatherers} helpers. A hand-written
 * {@code Gatherer} is covered in {@code CustomGathererTest}.
 */
public class StreamGatherersTest {

    private static final List<Integer> NUMBERS = List.of(1, 2, 3, 4, 5, 6, 7, 8);

    // ---- windowFixed: non-overlapping windows of exactly N elements ----

    @Test
    public void windowFixedChunksIntoNonOverlappingWindows() {
        assertThat(NUMBERS.stream().gather(Gatherers.windowFixed(3)).toList())
                .containsExactly(
                        List.of(1, 2, 3),
                        List.of(4, 5, 6),
                        List.of(7, 8));
    }

    @Test
    public void windowFixedWithExactMultipleLeavesNoPartialWindow() {
        List<Integer> even = List.of(1, 2, 3, 4);
        assertThat(even.stream().gather(Gatherers.windowFixed(2)).toList())
                .containsExactly(List.of(1, 2), List.of(3, 4));
    }

    @Test
    public void windowFixedSizeOneMakesSingletons() {
        assertThat(List.of(42, 7).stream().gather(Gatherers.windowFixed(1)).toList())
                .containsExactly(List.of(42), List.of(7));
    }

    // ---- windowSliding: overlapping windows, step 1 ----

    @Test
    public void windowSlidingProducesOverlappingWindows() {
        assertThat(NUMBERS.stream().gather(Gatherers.windowSliding(2)).toList())
                .containsExactly(
                        List.of(1, 2), List.of(2, 3), List.of(3, 4), List.of(4, 5),
                        List.of(5, 6), List.of(6, 7), List.of(7, 8));
    }

    @Test
    public void windowSlidingWindowSizeEqualsLengthYieldsSingleWindow() {
        assertThat(NUMBERS.stream().gather(Gatherers.windowSliding(8)).toList())
                .containsExactly(NUMBERS);
    }

    // ---- fold: one terminal value ----

    @Test
    public void foldReducesToSingleValue() {
        int sum = NUMBERS.stream()
                .gather(Gatherers.fold(() -> 0, (acc, e) -> acc + e))
                .findFirst().orElse(0);
        assertThat(sum).isEqualTo(36);
    }

    // ---- scan: running total after each element ----

    @Test
    public void scanProducesRunningTotal() {
        assertThat(NUMBERS.stream().gather(Gatherers.scan(() -> 0, (acc, e) -> acc + e)).toList())
                .containsExactly(1, 3, 6, 10, 15, 21, 28, 36);
    }

    // ---- empty streams ----

    @Test
    public void emptyStreamIsSafelyHandled() {
        List<Integer> empty = List.of();
        assertThat(empty.stream().gather(Gatherers.windowFixed(3)).toList()).isEmpty();
        assertThat(empty.stream().gather(Gatherers.windowSliding(3)).toList()).isEmpty();
        assertThat(empty.stream().gather(Gatherers.scan(() -> 0, (acc, e) -> acc + e)).toList()).isEmpty();
        assertThat(empty.stream()
                .gather(Gatherers.fold(() -> 0, (acc, e) -> acc + e))
                .findFirst().orElse(0)).isEqualTo(0);
    }

    // ---- mapConcurrent: bounded parallel mapping (order-preserving) ----

    @Test
    public void mapConcurrentPreservesOrderWithBoundedParallelism() {
        List<Integer> expected = NUMBERS.stream().map(n -> n * 2).toList();
        List<Integer> actual = NUMBERS.stream()
                .gather(Gatherers.mapConcurrent(4, n -> n * 2))
                .toList();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void mapConcurrentWithConcurrencyLargerThanElementCount() {
        List<String> words = List.of("a", "bb", "ccc");
        assertThat(words.stream().gather(Gatherers.mapConcurrent(10, String::toUpperCase)).toList())
                .containsExactly("A", "BB", "CCC");
    }

    @Test
    public void mapConcurrentLimitOneIsSequential() {
        List<Integer> actual = NUMBERS.stream()
                .gather(Gatherers.mapConcurrent(1, n -> n * 10))
                .toList();
        assertThat(actual).containsExactly(10, 20, 30, 40, 50, 60, 70, 80);
    }
}
