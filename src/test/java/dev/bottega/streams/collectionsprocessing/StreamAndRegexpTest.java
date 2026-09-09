package dev.bottega.streams.collectionsprocessing;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamAndRegexpTest {

    private final Pattern pattern = Pattern.compile(":");

    @Test
    public void splitAsStream() throws Exception {
        String sorted = pattern
                .splitAsStream("foobar:foo:bar")
                .filter(s -> s.contains("bar"))
                .collect(Collectors.joining(":"));
        Assertions.assertThat(sorted).isEqualTo("foobar:bar");
    }

    @Test
    public void patternAsPredicate() throws Exception {
        Pattern pattern = Pattern.compile(".*@gmail\\.com");
        List<String> gmails = Stream.of("bob@gmail.com", "alice@hotmail.com")
                .filter(pattern.asPredicate())
                .collect(Collectors.toList());
        Assertions.assertThat(gmails).containsExactly("bob@gmail.com");
    }
}
