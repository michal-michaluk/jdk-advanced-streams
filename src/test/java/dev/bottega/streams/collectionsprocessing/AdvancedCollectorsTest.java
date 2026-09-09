package dev.bottega.streams.collectionsprocessing;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Advanced {@code Collectors} downstream combinators: {@code teeing} (two collectors
 * in one pass), {@code partitioningBy} (split by predicate) and {@code toMap} with an
 * explicit {@code merge} function (resolve key clashes).
 */
public class AdvancedCollectorsTest {

    /**
     * Value produced by {@code teeing}: count + total in one pass.
     */
    record CountTotal(long count, double total) {
    }

    // ---- teeing: two collectors, one pass ----

    @Test
    public void teeingCountAndTotalInOnePass() {
        CountTotal result = List.of("a", "bb", "ccc").stream()
                .collect(Collectors.teeing(
                        Collectors.counting(),
                        Collectors.summingDouble(String::length),
                        CountTotal::new));
        assertThat(result).isEqualTo(new CountTotal(3, 6.0));
    }

    @Test
    public void teeingOnEmptyStream() {
        CountTotal result = List.<String>of().stream()
                .collect(Collectors.teeing(
                        Collectors.counting(),
                        Collectors.summingDouble(String::length),
                        CountTotal::new));
        assertThat(result).isEqualTo(new CountTotal(0, 0.0));
    }

    // ---- partitioningBy: predicate split ----

    @Test
    public void partitioningBySplitsIntoTwoGroups() {
        Map<Boolean, List<Integer>> groups = List.of(1, 2, 3, 4, 5, 6).stream()
                .collect(Collectors.partitioningBy(n -> n % 2 == 0));
        assertThat(groups.get(true)).containsExactly(2, 4, 6);
        assertThat(groups.get(false)).containsExactly(1, 3, 5);
    }

    @Test
    public void partitioningByWithDownstreamCounting() {
        Map<Boolean, Long> counts = List.of(1, 2, 3, 4, 5, 6).stream()
                .collect(Collectors.partitioningBy(n -> n % 2 == 0, Collectors.counting()));
        assertThat(counts).containsEntry(true, 3L).containsEntry(false, 3L);
    }

    // ---- toMap with a merge function: resolve key clashes ----

    @Test
    public void toMapWithMergeKeepsHigherValueOnClash() {
        Map<String, Integer> byName = List.of("A=1", "B=2", "A=5", "B=4").stream()
                .map(entry -> entry.split("="))
                .collect(Collectors.toMap(
                        parts -> parts[0],
                        parts -> Integer.valueOf(parts[1]),
                        Integer::max));
        assertThat(byName).hasSize(2)
                .containsEntry("A", 5)
                .containsEntry("B", 4);
    }

    @Test
    public void toMapWithMergeConcatenatesOnClash() {
        Map<String, String> byKey = List.of("a=1", "a=2", "b=3").stream()
                .map(entry -> entry.split("="))
                .collect(Collectors.toMap(
                        parts -> parts[0],
                        parts -> parts[1],
                        (x, y) -> x + "+" + y));
        assertThat(byKey).containsEntry("a", "1+2").containsEntry("b", "3");
    }
}
