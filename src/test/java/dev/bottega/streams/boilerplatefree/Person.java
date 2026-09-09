package dev.bottega.streams.boilerplatefree;

import lombok.Builder;
import java.util.Optional;

@Builder
public record Person(String name, String involvement) {

    public Person(String name) {
        this(name, null);
    }

    public Optional<String> getInvolvement() {
        return Optional.ofNullable(involvement);
    }
}
