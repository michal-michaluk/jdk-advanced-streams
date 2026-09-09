package dev.bottega.streams.collectionsprocessing;


import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Function;

import static java.util.stream.Collectors.*;

public class GroupingCompoundTest {
    private List<String> strings = List.of("a", "bb", "cc", "ddd");

    @Test
    public void example_1() {
        Map<Integer, TreeSet<String>> result = strings.stream()
                .collect(
                        groupingBy(String::length,
                                mapping(String::toUpperCase,
                                        filtering(s -> s.length() > 1,
                                                toCollection(TreeSet::new)))));

        System.out.println(result);
    }

    @Test
    public void example_2() {
        Map<Integer, String> result = strings.stream()
                .collect(
                        groupingBy(String::length,
                                mapping(toStringList(),
                                        flatMapping(s -> s.stream().distinct(),
                                                filtering(s -> s.length() > 0,
                                                        mapping(String::toUpperCase,
                                                                reducing("", String::concat)))))));

        System.out.println(result);
    }

    private static Function<String, List<String>> toStringList() {
        return s -> s.chars()
                .mapToObj(c -> (char) c)
                .map(Object::toString)
                .collect(toList());
    }
}


