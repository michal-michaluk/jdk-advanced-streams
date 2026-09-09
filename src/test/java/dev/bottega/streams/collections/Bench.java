package dev.bottega.streams.collections;

import org.assertj.core.api.Assertions;
import org.openjdk.jmh.annotations.Benchmark;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;
import java.util.stream.Collectors;

public class Bench {

    public static final Collection<String> base = Collections.unmodifiableCollection(
            new Random().doubles(10_000)
            .mapToObj(d -> "next random value is: " + d)
            .collect(Collectors.toList())
    );

    @Benchmark // JMH benchmark
    public void arrayList() throws Exception {
        ArrayList<String> list = new ArrayList<>(0);
        for (String s : base) {
            list.add(s);
        }

        Assertions.assertThat(list).hasSize(10_000);
    }

    // Task. Fun with Benchmark
    // Bench test look vs. stream processing for similar cases
    // Bench test array list vs. hash set for contains
    // ... other ideas ?

}
