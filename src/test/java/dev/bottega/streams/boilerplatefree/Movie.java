package dev.bottega.streams.boilerplatefree;

import lombok.Builder;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Year;
import java.util.EnumSet;
import java.util.List;

@Builder
public record Movie(
        String imdbID,
        String title,
        Year year,
        String rated,
        LocalDate released,
        Duration runtime,
        EnumSet<Genre> genre,
        List<Person> director,
        List<Person> writer,
        List<Person> actors,
        String plot,
        String language,
        String country,
        String awards,
        URI poster,
        Double metascore,
        Double imdbRating,
        Double imdbVotes
) {

    public enum Genre {
        Action,
        Biography,
        Animated,
        Adventure,
        ChickFlicks,
        Comedy,
        Detective,
        Mystery,
        Children,
        Animation,
        Family,
        Gangster,
        Disaster,
        Classic,
        Drama,
        Fantasy,
        Cult,
        Epics,
        History,
        FilmNoir,
        Documentary,
        Horror,
        Guy,
        Serial,
        Musicals,
        Dance,
        Melodramas,
        Sexual,
        Erotic,
        ScienceFiction,
        Road,
        Silent,
        WarAntiWar,
        Romance,
        Westerns,
        Sports,
        Supernatural,
        Thriller
    }
}
