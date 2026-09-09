package dev.bottega.streams.almostlikefunctional.excercise;

import java.util.function.Function;

/**
 * Result as a value — success ({@link Ok}) or a {@link TokenImport.Problem} ({@link Err}); nothing throws.
 */
public sealed interface Result<T> {

    record Ok<T>(T value) implements Result<T> {}

    record Err<T>(TokenImport.Problem problem) implements Result<T> {}

    static <T> Result<T> ok(T value) {
        return new Ok<>(value);
    }

    static <T> Result<T> err(TokenImport.Problem problem) {
        return new Err<>(problem);
    }

    default <R> Result<R> map(Function<? super T, ? extends R> f) {
        return switch (this) {
            case Ok<T> ok -> Result.ok(f.apply(ok.value()));
            case Err<T> err -> Result.err(err.problem());
        };
    }

    default <R> Result<R> flatMap(Function<? super T, Result<R>> f) {
        return switch (this) {
            case Ok<T> ok -> f.apply(ok.value());
            case Err<T> err -> Result.err(err.problem());
        };
    }
}
