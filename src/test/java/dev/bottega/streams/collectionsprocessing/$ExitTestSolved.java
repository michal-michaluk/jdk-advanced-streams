package dev.bottega.streams.collectionsprocessing;

import dev.bottega.streams.boilerplatefree.ExampleMovies;
import dev.bottega.streams.boilerplatefree.Movie;
import dev.bottega.streams.boilerplatefree.Person;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Gatherers;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

public class $ExitTestSolved {
    // using ExampleMovies ...

    private static Stream<Movie> movies() {
        return ExampleMovies.allMovies().stream().map(Movie.MovieBuilder::build);
    }

    private static int crewSize(Movie movie) {
        return movie.actors().size() + movie.director().size() + movie.writer().size();
    }

    // Task 1.
    // flat-map without flatMap: collect every distinct crew member (directors + writers + actors),
    // sorted by name, using the JDK 16+ mapMulti() for multiple outputs per input.

    @Test
    public void distinctCrewViaMapMulti() {
        List<String> crew = movies()
                .mapMulti((Movie movie, Consumer<String> out) ->
                        Stream.concat(
                                        movie.director().stream(),
                                        Stream.concat(movie.writer().stream(), movie.actors().stream()))
                                .map(Person::name)
                                .forEach(out))
                .distinct()
                .sorted()
                .toList();

        assertThat(crew)
                .isSorted()
                .doesNotHaveDuplicates()
                .contains("Quentin Tarantino", "Uma Thurman");
    }

    // Task 2.
    // one pass over the database: count movies AND total runtime, using Collectors.teeing().

    @Test
    public void countAndTotalRuntimeInOnePass() {
        record CountAndTotal(long count, Duration total) {
        }

        CountAndTotal result = movies()
                .collect(Collectors.teeing(
                        Collectors.counting(),
                        Collectors.mapping(Movie::runtime, Collectors.reducing(Duration.ZERO, Duration::plus)),
                        CountAndTotal::new));

        assertThat(result.count()).isEqualTo(movies().count());
        assertThat(result.total())
                .isEqualTo(movies().map(Movie::runtime).reduce(Duration.ZERO, Duration::plus));
    }

    // Task 3.
    // chunk the stream with a Gatherer: for every non-overlapping window of 3 movies,
    // report the highest imdbRating inside the window (Gatherers.windowFixed).
    // 9 movies -> 3 windows of 3: [8.1,6.6,7.7] [6.7,8.1,7.7] [7.6,7.5,5.7]
    // 8.1, 8.1, 7.6

    @Test
    public void maxRatingPerWindowOfThree() {
        List<Double> maxPerWindow = movies()
                .map(Movie::imdbRating)
                .gather(Gatherers.windowFixed(3))
                .map(window -> window.stream().max(Double::compare).orElseThrow())
                .toList();

        // 9 movies -> 3 windows of 3: [8.1,6.6,7.7] [6.7,8.1,7.7] [7.6,7.5,5.7]
        assertThat(maxPerWindow).containsExactly(8.1, 8.1, 7.6);
    }

    // Task 4.
    // index movies by title with Collectors.toMap and an explicit merge function,
    // so a key clash would keep the higher rating.

    @Test
    public void indexByTitleKeepingHighestRated() {
        Map<String, Double> byTitle = movies()
                .collect(Collectors.toMap(Movie::title, Movie::imdbRating, Math::max));

        assertThat(byTitle)
                .hasSize(9)
                .containsEntry("Kill Bill: Vol. 1", 8.1)
                .containsEntry("Titanic", 7.7);
    }

    // Task 5.
    // write your own parallel-safe Collector and prove it is associative:
    // count, total runtime and largest crew, collected sequentially equals collected in parallel.

    @Test
    public void customCollectorSequentialEqualsParallel() {
        MovieSummary sequential = movies().collect(movieSummaryCollector());
        MovieSummary parallel = movies().parallel().collect(movieSummaryCollector());

        assertThat(parallel.count()).isEqualTo(sequential.count());
        assertThat(parallel.totalRuntime()).isEqualTo(sequential.totalRuntime());
        assertThat(parallel.maxCrew()).isEqualTo(sequential.maxCrew());
    }

    // Task 6.
    // sort by rating (desc) and take the leading prefix of "top tier" movies (rating >= 7.0),
    // using takeWhile() on the ordered stream.

    @Test
    public void topTierPrefixViaTakeWhile() {
        List<Movie> topTier = movies()
                .sorted(Comparator.comparing(Movie::imdbRating).reversed())
                .takeWhile(m -> m.imdbRating() >= 7.0)
                .toList();

        assertThat(topTier).isNotEmpty();
        assertThat(topTier)
                .isSortedAccordingTo(Comparator.comparing(Movie::imdbRating).reversed());
        assertThat(topTier).noneMatch(m -> m.imdbRating() < 7.0);
    }

    // Task 7.
    // partition the database into "long" (>120 min) and "short" movies with
    // Collectors.partitioningBy().

    @Test
    public void longVsShortViaPartitioningBy() {
        Map<Boolean, List<Movie>> byLength = movies()
                .collect(Collectors.partitioningBy(m -> m.runtime().toMinutes() > 120));

        assertThat(byLength.get(true)).allMatch(m -> m.runtime().toMinutes() > 120);
        assertThat(byLength.get(false)).noneMatch(m -> m.runtime().toMinutes() > 120);
        assertThat(byLength.get(true).size() + byLength.get(false).size()).isEqualTo(9);
    }

    // Task 8.
    // running (cumulative) total runtime across the database, via Gatherers.scan.

    @Test
    public void runningCumulativeRuntimeViaScan() {
        List<Duration> running = movies()
                .map(Movie::runtime)
                .gather(Gatherers.scan(() -> Duration.ZERO, Duration::plus))
                .toList();

        assertThat(running).hasSize(9);
        assertThat(running).isSortedAccordingTo(Comparator.naturalOrder());
    }

    // Task 9.
    // average runtime per genre — a movie with several genres is counted in each of them,
    // using flatMap to fan out genres and groupingBy + averagingDouble downstream.

    @Test
    public void averageRuntimeByGenre() {
        Map<Movie.Genre, Double> avgByGenre = movies()
                .flatMap(movie -> movie.genre().stream().map(genre -> entry(genre, movie.runtime().toMinutes())))
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.averagingDouble(Map.Entry::getValue)));

        assertThat(avgByGenre).isNotEmpty();
        assertThat(avgByGenre).doesNotContainValue(0.0);
    }

    // ----------------------------------------------------------------------
    // helpers for the custom parallel-safe collector
    // ----------------------------------------------------------------------

    record MovieSummary(int count, Duration totalRuntime, int maxCrew) {
    }

    private static final class Accum {
        private int count;
        private Duration total = Duration.ZERO;
        private int maxCrew;

        private void add(Movie movie) {
            count++;
            total = total.plus(movie.runtime());
            maxCrew = Math.max(maxCrew, crewSize(movie));
        }

        /**
         * Associative merge of two partial accumulators (called under parallel()).
         */
        private Accum merge(Accum other) {
            count += other.count;
            total = total.plus(other.total);
            maxCrew = Math.max(maxCrew, other.maxCrew);
            return this;
        }

        private MovieSummary finish() {
            return new MovieSummary(count, total, maxCrew);
        }
    }

    private static Collector<Movie, Accum, MovieSummary> movieSummaryCollector() {
        return Collector.of(
                Accum::new,
                Accum::add,
                Accum::merge,
                Accum::finish,
                Collector.Characteristics.UNORDERED);
    }
}
