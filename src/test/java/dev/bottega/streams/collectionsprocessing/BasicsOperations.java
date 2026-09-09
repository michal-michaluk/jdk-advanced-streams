package dev.bottega.streams.collectionsprocessing;

import org.junit.jupiter.api.Test;
import dev.bottega.streams.boilerplatefree.ExampleMovies;
import dev.bottega.streams.boilerplatefree.Movie;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

public class BasicsOperations {

    @Test
    public void justFun() throws Exception {
        List<String> myList =
                Arrays.asList("b1", "a2", "c2", "a1", "c1", "a2");

        myList
                .stream()
                .filter(s -> !s.startsWith("c"))
                .map(String::toUpperCase)
                .sorted()
                .peek((x) -> System.out.println(x + " " + x.hashCode()))
                .skip(1)
                .limit(3)
                .distinct()
                .flatMapToInt(CharSequence::chars)
                .peek(System.out::println)
                .count()
        ;
    }

    @Test
    public void defineMapInOneLine() throws Exception {
        Map<String, Movie> moviesById = ExampleMovies.allMovies()
                .stream()
                .map(Movie.MovieBuilder::build)
                .collect(toMap(Movie::imdbID, Function.identity()));

        System.out.println(moviesById);
    }

    public static List<String> genres() {
        return Arrays.stream(Movie.Genre.values())
                .map(Movie.Genre::name)
                .collect(Collectors.toList());
    }
}
