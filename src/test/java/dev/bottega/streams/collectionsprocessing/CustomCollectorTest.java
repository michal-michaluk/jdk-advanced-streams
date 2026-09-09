package dev.bottega.streams.collectionsprocessing;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Hand-written, parallel-safe {@link Collector} — the "roll your own collector"
 * side of the stateful terminal API. A collector is parallel-safe when its
 * {@code combiner} is <em>associative</em>: partial results computed on different
 * threads (or chunks) can be merged in any order and still produce the same result.
 *
 * <p>Here {@link #summaryCollector()} is a small {@code Collector<Integer, Accum, Summary>}
 * built with {@code Collector.of(...)} and declared {@code UNORDERED}. We prove the combiner
 * is associative by collecting the same data sequentially and in parallel and asserting
 * identical results.
 */
public class CustomCollectorTest {

    /** Immutable value produced by the custom collector. */
    record Summary(int count, int sum) {
    }

    /** Mutable accumulator backing the custom collector. */
    static final class Accum {
        private int count;
        private int sum;

        void add(int value) {
            count++;
            sum += value;
        }

        /** Associative merge of two partial accumulators. */
        Accum merge(Accum other) {
            count += other.count;
            sum += other.sum;
            return this;
        }

        Summary finish() {
            return new Summary(count, sum);
        }
    }

    /** A parallel-safe collector: {@code UNORDERED}, associative combiner. */
    static Collector<Integer, Accum, Summary> summaryCollector() {
        return Collector.of(
                Accum::new,
                Accum::add,
                Accum::merge,
                Accum::finish,
                Collector.Characteristics.UNORDERED);
    }

    @Test
    public void customCollectorSequential() {
        Summary summary = IntStream.rangeClosed(1, 4).boxed().collect(summaryCollector());
        assertThat(summary).isEqualTo(new Summary(4, 10));
    }

    @Test
    public void customCollectorParallelMatchesSequential() {
        Summary sequential = IntStream.rangeClosed(1, 4).boxed().collect(summaryCollector());
        Summary parallel = IntStream.rangeClosed(1, 4).boxed().parallel().collect(summaryCollector());
        assertThat(parallel).isEqualTo(sequential);
    }

    @Test
    public void customCollectorParallelOnLargeRangeMatchesSequential() {
        // The combiner really is exercised on a range large enough to split into chunks.
        Summary sequential = IntStream.range(0, 100_000).boxed().collect(summaryCollector());
        Summary parallel = IntStream.range(0, 100_000).boxed().parallel().collect(summaryCollector());
        assertThat(parallel.count).isEqualTo(100_000);
        assertThat(parallel.sum).isEqualTo(sequential.sum);
        assertThat(parallel).isEqualTo(sequential);
    }

    @Test
    public void customCollectorOnEmptyStream() {
        Summary summary = List.<Integer>of().stream().collect(summaryCollector());
        assertThat(summary).isEqualTo(new Summary(0, 0));
    }

    @Test
    public void customCollectorIsOrderIndependent() {
        // Because the operation is commutative and the collector is UNORDERED,
        // the result is the same regardless of the source order.
        List<Integer> a = List.of(1, 2, 3, 4, 5);
        List<Integer> b = List.of(5, 4, 3, 2, 1);
        assertThat(a.stream().collect(summaryCollector()))
                .isEqualTo(b.stream().collect(summaryCollector()));
    }
}
