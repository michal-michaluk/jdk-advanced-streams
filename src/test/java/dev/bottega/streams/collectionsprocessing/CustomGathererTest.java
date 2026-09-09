package dev.bottega.streams.collectionsprocessing;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Hand-written stateful {@link Gatherer}s — the "you can write your own" side of
 * JEP 485. Two forms are shown:
 *
 * <ul>
 *   <li>{@code distinctBy} — an <em>integrator-only</em> gatherer (no finisher) that keeps
 *       a running set of seen keys.</li>
 *   <li>{@code batch} — a gatherer with a <em>finisher</em> that buffers elements and
 *       flushes a trailing partial batch at the end of the stream.</li>
 * </ul>
 */
public class CustomGathererTest {

    /**
     * Keeps the first element for each extracted key, preserving encounter order.
     * Uses {@code Gatherer.ofSequential(initializer, integrator)} — no finisher needed.
     */
    <T> Gatherer<T, Set<Object>, T> distinctBy(Function<? super T, ?> keyExtractor) {
        return Gatherer.ofSequential(
                HashSet::new,
                (Set<Object> seen, T element, Gatherer.Downstream<? super T> downstream) -> {
                    if (seen.add(keyExtractor.apply(element))) {
                        return downstream.push(element);
                    }
                    return true;
                });
    }

    /**
     * Buffers elements into batches of at most {@code maxSize}. A trailing partial batch
     * is emitted by the finisher, which runs once the upstream is exhausted.
     */
    <T> Gatherer<T, List<T>, List<T>> batch(int maxSize) {
        return Gatherer.ofSequential(
                ArrayList::new,
                (List<T> state, T element, Gatherer.Downstream<? super List<T>> downstream) -> {
                    state.add(element);
                    if (state.size() >= maxSize) {
                        List<T> batch = new ArrayList<>(state);
                        state.clear();
                        return downstream.push(batch);
                    }
                    return true;
                },
                (List<T> state, Gatherer.Downstream<? super List<T>> downstream) -> {
                    if (!state.isEmpty()) {
                        downstream.push(new ArrayList<>(state));
                        state.clear();
                    }
                });
    }

    // ---- distinctBy ----

    @Test
    public void distinctByKeepsFirstOccurrencePerKeyPreservingOrder() {
        List<String> words = List.of("apple", "avocado", "banana", "apricot", "berry");
        // keys: a,a,b,a,b -> first occurrence per key: "apple"(a), "banana"(b)
        assertThat(words.stream().gather(distinctBy(s -> s.charAt(0))).toList())
                .containsExactly("apple", "banana");
    }

    @Test
    public void distinctByWithAllDistinctKeysKeepsEverything() {
        assertThat(List.of("a", "bb", "ccc").stream().gather(distinctBy(String::length)).toList())
                .containsExactly("a", "bb", "ccc");
    }

    @Test
    public void distinctByEmptyStream() {
        assertThat(Stream.<String>empty().gather(distinctBy(String::length)).toList()).isEmpty();
    }

    // ---- batch (with finisher) ----

    @Test
    public void batchChunksIntoFixedSizeAndFlushesTrailingPartial() {
        assertThat(Stream.of(1, 2, 3, 4, 5).gather(batch(2)).toList())
                .containsExactly(
                        List.of(1, 2),
                        List.of(3, 4),
                        List.of(5));
    }

    @Test
    public void batchWithExactMultipleLeavesNoTrailingPartial() {
        assertThat(Stream.of(1, 2, 3, 4).gather(batch(2)).toList())
                .containsExactly(List.of(1, 2), List.of(3, 4));
    }

    @Test
    public void batchLargerThanStreamYieldsSingleTrailingBatch() {
        assertThat(Stream.of(1, 2, 3).gather(batch(10)).toList())
                .containsExactly(List.of(1, 2, 3));
    }

    @Test
    public void batchEmptyStreamYieldsNothing() {
        assertThat(Stream.<Integer>empty().gather(batch(3)).toList()).isEmpty();
    }
}
