package dev.bottega.streams.collectionsprocessing;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The mechanism underneath parallel streams: a hand-written {@link Spliterator} wired
 * into a stream via {@code StreamSupport.stream(spliterator, true)}.
 *
 * <p>It shows the half-splitting {@code trySplit}, the element-wise {@code tryAdvance},
 * the reported {@code characteristics} and — on the running stream — that
 * {@code findFirst} is deterministic on an {@code ORDERED} stream whereas
 * {@code findAny} may return any element.
 */
public class SpliteratorAndStreamSupportTest {

    private static final List<Integer> NUMBERS = List.of(1, 2, 3, 4, 5, 6, 7, 8);

    /** A half-splitting spliterator over a {@code List}, reporting {@code ORDERED|SIZED|SUBSIZED|IMMUTABLE}. */
    static final class ListSpliterator<T> implements Spliterator<T> {
        private final List<T> data;
        private int index;
        private final int fence;

        ListSpliterator(List<T> data) {
            this(data, 0, data.size());
        }

        private ListSpliterator(List<T> data, int index, int fence) {
            this.data = data;
            this.index = index;
            this.fence = fence;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            if (index < fence) {
                action.accept(data.get(index));
                index++;
                return true;
            }
            return false;
        }

        @Override
        public Spliterator<T> trySplit() {
            int lo = index;
            int hi = fence;
            if (hi - lo < 2) {
                return null;
            }
            int mid = lo + (hi - lo) / 2;
            Spliterator<T> left = new ListSpliterator<>(data, lo, mid);
            this.index = mid; // this spliterator keeps the second half
            return left;
        }

        @Override
        public long estimateSize() {
            return fence - index;
        }

        @Override
        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.IMMUTABLE;
        }
    }

    @Test
    public void parallelCountOverCustomSpliterator() {
        long count = StreamSupport.stream(new ListSpliterator<>(NUMBERS), true).count();
        assertThat(count).isEqualTo(8);
    }

    @Test
    public void parallelSumMatchesSequential() {
        int sequential = NUMBERS.stream().mapToInt(Integer::intValue).sum();
        int parallel = StreamSupport.stream(new ListSpliterator<>(NUMBERS), true)
                .mapToInt(Integer::intValue).sum();
        assertThat(parallel).isEqualTo(sequential);
    }

    @Test
    public void findFirstIsDeterministicOnOrderedStream() {
        Integer first = StreamSupport.stream(new ListSpliterator<>(NUMBERS), true).findFirst().orElseThrow();
        assertThat(first).isEqualTo(1);
    }

    @Test
    public void findAnyReturnsAnElementOfTheStream() {
        Integer any = StreamSupport.stream(new ListSpliterator<>(NUMBERS), true).findAny().orElseThrow();
        assertThat(NUMBERS).contains(any);
    }

    @Test
    public void trySplitYieldsDisjointOrderedHalves() {
        Spliterator<Integer> root = new ListSpliterator<>(NUMBERS);
        Spliterator<Integer> left = root.trySplit();
        assertThat(left).isNotNull();

        List<Integer> leftElements = new ArrayList<>();
        left.forEachRemaining(leftElements::add);
        List<Integer> rightElements = new ArrayList<>();
        root.forEachRemaining(rightElements::add);

        List<Integer> both = new ArrayList<>(leftElements);
        both.addAll(rightElements);
        // The split is disjoint and preserves encounter order of the halves.
        assertThat(both).containsExactlyElementsOf(NUMBERS);
    }

    @Test
    public void trySplitReturnsNullWhenTooSmallToSplit() {
        Spliterator<Integer> single = new ListSpliterator<>(List.of(42));
        assertThat(single.trySplit()).isNull();
    }

    @Test
    public void characteristicsAreReported() {
        int chars = new ListSpliterator<>(NUMBERS).characteristics();
        assertThat(chars & Spliterator.ORDERED).isNotZero();
        assertThat(chars & Spliterator.SIZED).isNotZero();
        assertThat(chars & Spliterator.SUBSIZED).isNotZero();
        assertThat(chars & Spliterator.IMMUTABLE).isNotZero();
    }
}
