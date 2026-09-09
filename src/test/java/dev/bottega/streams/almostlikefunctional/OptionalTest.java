package dev.bottega.streams.almostlikefunctional;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class OptionalTest {

    @Test
    public void empty() throws Exception {
        Optional<Object> o = Optional.empty();

        assertThat(o)
                .isEmpty();
    }

    @Test
    public void ofValue() throws Exception {
        Optional<String> hi = Optional.of("Hi");

        assertThat(hi).hasValue("Hi");
    }

    @Test
    public void ofNull() throws Exception {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() ->
                        Optional.of(null)
                );
    }

    @Test
    public void ofNullable() throws Exception {
        Object object = null;
        Optional<Object> o = Optional.ofNullable(object);

        assertThat(o)
                .isEmpty();
    }

    Optional<String> valueOrNon(String in) {
        if (in == null || in.equals("")) {
            return Optional.empty();
        } else {
            return Optional.of(in);
        }
    }

    @Test
    public void getExisting() throws Exception {
        Optional<String> optional = Optional.of("value");
        String value = optional.get();
        assertThat(value).isEqualTo("value");
    }

    @Test
    public void getOrDefault() throws Exception {
        Optional<String> optional = Optional.empty();
        String value = optional.orElse("default");
        assertThat(value).isEqualTo("default");
    }

    @Test
    public void getOrDefaultLazy() throws Exception {
        Optional<String> optional = Optional.empty();
        String value = optional.orElseGet(() -> "default");
        assertThat(value).isEqualTo("default");
    }

    @Test
    public void getOrThrow() throws Exception {
        Optional<String> optional = Optional.empty();

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() ->
                        optional.orElseThrow(IllegalStateException::new)
                )
        ;
    }

    @Test
    public void getFromEmpty() throws Exception {
        Optional<String> optional = Optional.empty();
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(optional::get);
    }

    @Test
    public void checkPresence() throws Exception {
        Optional<String> optional = Optional.empty();
        if (optional.isPresent()) { // will not step in
            optional.get();
            throw new RuntimeException();
        }
    }

    @Test
    public void computeIfPresent() throws Exception {
        Optional<String> optional = Optional.empty();

        optional.ifPresent((value) -> {});
    }

    @Test
    public void predicate() throws Exception {
        Optional<String> optional = Optional.empty();

        Optional<String> next = optional
                .filter((value) -> "".equals(value));
    }

    @Test
    public void flatMap() throws Exception {
        Optional<String> optional = Optional.empty();

        Optional<Integer> integer = optional
                .flatMap((value) -> Optional.of(value.length()));
    }
}
