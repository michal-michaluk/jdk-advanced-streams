package dev.bottega.streams.collectionsprocessing;


import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RegexpTest {

    private Pattern pattern = Pattern.compile(":");

    @Test
    public void splitAsStream() throws Exception {
        pattern
                .splitAsStream("foobar:foo:bar")
                .filter(s -> s.contains("bar"))
                .sorted()
                .collect(Collectors.joining(":"));
    }

    @Test
    public void patternAsPredicate() throws Exception {
        Pattern pattern = Pattern.compile(".*@gmail\\.com");
        Stream.of("bob@gmail.com", "alice@hotmail.com")
                .filter(pattern.asPredicate())
                .count();
    }
}
