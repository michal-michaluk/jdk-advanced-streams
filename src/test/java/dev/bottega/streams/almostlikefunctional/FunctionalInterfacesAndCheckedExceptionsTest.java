package dev.bottega.streams.almostlikefunctional;

import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class FunctionalInterfacesAndCheckedExceptionsTest {

    @Test
    public void uncheckedException() throws Exception {
        Consumer<String> c = s -> {
            throw new RuntimeException("from lambda");
        };

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() ->
                        c.accept("hi !")
                );
    }

    @FunctionalInterface
    public interface ThrowingConsumer<T> {
        void accept(T value) throws Throwable;
    }

    @Test
    public void checkedException() throws Throwable {
        ThrowingConsumer<String> c = s -> {
            throw new Exception("from lambda");
        };

        assertThatExceptionOfType(Exception.class)
                .isThrownBy(() ->
                        c.accept("hi !")
                );
    }
}
