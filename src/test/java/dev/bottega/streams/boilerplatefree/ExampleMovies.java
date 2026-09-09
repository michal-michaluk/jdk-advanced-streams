package dev.bottega.streams.boilerplatefree;


import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static dev.bottega.streams.boilerplatefree.Movie.Genre.*;

public class ExampleMovies {

    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US);
    public static final Function<String, LocalDate> parseReleased = s -> LocalDate.parse(s, formatter);
    public static final Function<String, Duration> parseRuntime = s -> Duration.ofMinutes(Integer.valueOf(s.replace(" min", "")));


    public static Movie.MovieBuilder hollywoodMovie() {
        return Movie.builder()
                .imdbID("tt0266697")
                .title("Kill Bill: Vol. 1")
                .year(Year.of(2003))
                .rated("R")
                .released(parseReleased.apply("10 Oct 2003"))
                .runtime(parseRuntime.apply("111 min"))
                .genre(EnumSet.of(Action, Thriller))
                .director(singletonList(new Person("Quentin Tarantino")))
                .writer(asList(new Person("Quentin Tarantino"), new Person("Quentin Tarantino", "(character The Bride)"), new Person("Uma Thurman", "(character The Bride)")))
                .actors(asList(new Person("Uma Thurman"), new Person("Lucy Liu"), new Person("Vivica A. Fox"), new Person("Daryl Hannah")))
                .plot("The Bride wakens from a four-year coma. The child she carried in her womb is gone. Now she must wreak vengeance on the team of assassins who betrayed her - a team she was once part of.")
                .language("English, Japanese, French")
                .country("USA")
                .awards("Nominated for 1 Golden Globe. Another 25 wins & 84 nominations.")
                .poster(URI.create("https://images-na.ssl-images-amazon.com/images/M/MV5BMTU1NDg1Mzg4M15BMl5BanBnXkFtZTYwMDExOTc3._V1_SX300.jpg"))
                .metascore(69.)
                .imdbRating(8.1)
                .imdbVotes(745.289);
    }

    public static List<Movie.MovieBuilder> allMovies() {
                return asList(
                        hollywoodMovie()
                ,

                Movie.builder()
                        .imdbID("tt0076584")
                        .title("The Inglorious Bastards")
                        .year(Year.of(1978))
                        .rated("R")
                        .released(parseReleased.apply("01 Dec 1981"))
                        .runtime(parseRuntime.apply("99 min"))
                        .genre(EnumSet.of(Action, Adventure, Comedy))
                        .director(singletonList(new Person("Enzo G. Castellari")))
                        .writer(asList(new Person("Sandro Continenza"), new Person("Sergio Grieco"), new Person("Romano Migliorini"), new Person("Laura Toscano"), new Person("Franco Marotta"), new Person("Alberto Piferi", "(dialogue collaboration)")))
                        .actors(asList(new Person("Bo Svenson"), new Person("Peter Hooten"), new Person("Fred Williamson"), new Person("Michael Pergolani")))
                        .plot("In 1944, in France, the rogue American soldiers Lieutenant Robert Yeager, Private Fred Canfield, the murderer Tony, the thief Nick and the coward Berle are transported to a military prison....")
                        .language("English, German, French, Italian")
                        .country("Italy")
                        .awards("null")
                        .poster(URI.create("https://images-na.ssl-images-amazon.com/images/M/MV5BMTU1NjM5NTYwM15BMl5BanBnXkFtZTcwODYxMjQ3Mg@@._V1_SX300.jpg"))
                        .metascore(null)
                        .imdbRating(6.6)
                        .imdbVotes(7.413)
                ,

                Movie.builder()
                        .imdbID("tt0120338")
                        .title("Titanic")
                        .year(Year.of(1997))
                        .rated("PG-13")
                        .released(parseReleased.apply("19 Dec 1997"))
                        .runtime(parseRuntime.apply("194 min"))
                        .genre(EnumSet.of(Drama, Romance))
                        .director(singletonList(new Person("James Cameron")))
                        .writer(singletonList(new Person("James Cameron")))
                        .actors(asList(new Person("Leonardo DiCaprio"), new Person("Kate Winslet"), new Person("Billy Zane"), new Person("Kathy Bates")))
                        .plot("A seventeen-year-old aristocrat falls in love with a kind but poor artist aboard the luxurious, ill-fated R.M.S. Titanic.")
                        .language("English")
                        .country("USA")
                        .awards("Won 11 Oscars. Another 110 wins & 73 nominations.")
                        .poster(URI.create("https://images-na.ssl-images-amazon.com/images/M/MV5BZDNiMjE0NDgtZWRhNC00YTlhLTk2ZjItZTQzNTU2NjAzNWNkXkEyXkFqcGdeQXVyNjUwNzk3NDc@._V1_SX300.jpg"))
                        .metascore(74.)
                        .imdbRating(7.7)
                        .imdbVotes(802.161)
                ,

                Movie.builder()
                        .imdbID("tt0080923")
                        .title("Inferno")
                        .year(Year.of(1980))
                        .rated("R")
                        .released(parseReleased.apply("02 Apr 1980"))
                        .runtime(parseRuntime.apply("107 min"))
                        .genre(EnumSet.of(Horror, Mystery))
                        .director(singletonList(new Person("Dario Argento")))
                        .writer(singletonList(new Person("Dario Argento", "(story and screenplay)")))
                        .actors(asList(new Person("Leigh McCloskey"), new Person("Irene Miracle"), new Person("Eleonora Giorgi"), new Person("Daria Nicolodi")))
                        .plot("An American college student in Rome and his sister in New York investigate a series of killings in both locations where their resident addresses are the domain of two covens of witches.")
                        .language("English, Italian")
                        .country("Italy")
                        .awards("null")
                        .poster(URI.create("https://images-na.ssl-images-amazon.com/images/M/MV5BZTljNjEwZjAtYTcwYy00MDY3LTkxN2YtYWI1MTQ2OWUwY2EyXkEyXkFqcGdeQXVyMTQxNzMzNDI@._V1_SX300.jpg"))
                        .metascore(null)
                        .imdbRating(6.7)
                        .imdbVotes(12.425)
                ,

                Movie.builder()
                        .imdbID("tt0032138")
                        .title("The Wizard of Oz")
                        .year(Year.of(1939))
                        .rated("PASSED")
                        .released(parseReleased.apply("25 Aug 1939"))
                        .runtime(parseRuntime.apply("102 min"))
                        .genre(EnumSet.of(Adventure, Family, Fantasy))
                        .director(asList(new Person("Victor Fleming"), new Person("George Cukor"), new Person("Mervyn LeRoy"), new Person("Norman Taurog"), new Person("King Vidor")))
                        .writer(asList(new Person("Noel Langley", "(screenplay)"), new Person("Florence Ryerson", "(screenplay)"), new Person("Edgar Allan Woolf", "(screenplay)"), new Person("Noel Langley", "(adaptation)"), new Person("L. Frank Baum", "(from the book by)")))
                        .actors(asList(new Person("Judy Garland"), new Person("Frank Morgan"), new Person("Ray Bolger"), new Person("Bert Lahr")))
                        .plot("Dorothy Gale is swept away to a magical land in a tornado and embarks on a quest to see the Wizard who can help her return home.")
                        .language("English")
                        .country("USA")
                        .awards("Won 2 Oscars. Another 7 wins & 13 nominations.")
                        .poster(URI.create("http://ia.media-imdb.com/images/M/MV5BMjA4NjE1MjM2Ml5BMl5BanBnXkFtZTgwODgzMjM5MzE@._V1_SX300.jpg"))
                        .metascore(100.)
                        .imdbRating(8.1)
                        .imdbVotes(293.284)
                ,

                Movie.builder()
                        .imdbID("tt0029583")
                        .title("Snow White and the Seven Dwarfs")
                        .year(Year.of(1937))
                        .rated("APPROVED")
                        .released(parseReleased.apply("04 Feb 1938"))
                        .runtime(parseRuntime.apply("83 min"))
                        .genre(EnumSet.of(Animation, Family, Fantasy))
                        .director(asList(new Person("William Cottrell"), new Person("David Hand"), new Person("Wilfred Jackson"), new Person("Larry Morey"), new Person("Perce Pearce"), new Person("Ben Sharpsteen")))
                        .writer(asList(new Person("Jacob Grimm", "(fairy tales)"), new Person("Wilhelm Grimm", "(fairy tales)"), new Person("Ted Sears", "(story adaptation)"), new Person("Richard Creedon", "(story adaptation)"), new Person("Otto Englander", "(story adaptation)"), new Person("Dick Rickard", "(story adaptation)"), new Person("Earl Hurd", "(story adaptation)"), new Person("Merrill De Maris", "(story adaptation)"), new Person("Dorothy Ann Blank", "(story adaptation)"), new Person("Webb Smith", "(story adaptation)")))
                        .actors(asList(new Person("Roy Atwell"), new Person("Stuart Buchanan"), new Person("Adriana Caselotti"), new Person("Hall Johnson Choir")))
                        .plot("Snow White, pursued by a jealous queen, hides with the Dwarfs, the queen soon learns of this and prepares to feed her a poison apple.")
                        .language("English")
                        .country("USA")
                        .awards("Nominated for 1 Oscar. Another 11 wins & 4 nominations.")
                        .poster(URI.create("https://images-na.ssl-images-amazon.com/images/M/MV5BMTQwMzE2Mzc4M15BMl5BanBnXkFtZTcwMTE4NTc1Nw@@._V1_SX300.jpg"))
                        .metascore(null)
                        .imdbRating(7.7)
                        .imdbVotes(135.218)
                ,

                Movie.builder()
                        .imdbID("tt0112384")
                        .title("Apollo 13")
                        .year(Year.of(1995))
                        .rated("PG")
                        .released(parseReleased.apply("30 Jun 1995"))
                        .runtime(parseRuntime.apply("140 min"))
                        .genre(EnumSet.of(Adventure, Drama, History))
                        .director(singletonList(new Person("Ron Howard")))
                        .writer(asList(new Person("Jim Lovell", "(book)"), new Person("Jeffrey Kluger", "(book)"), new Person("William Broyles Jr.", "(screenplay)"), new Person("Al Reinert", "(screenplay)")))
                        .actors(asList(new Person("Tom Hanks"), new Person("Bill Paxton"), new Person("Kevin Bacon"), new Person("Gary Sinise")))
                        .plot("NASA must devise a strategy to return Apollo 13 to Earth safely after the spacecraft undergoes massive internal damage putting the lives of the three astronauts on board in jeopardy.")
                        .language("English")
                        .country("USA")
                        .awards("Won 2 Oscars. Another 24 wins & 49 nominations.")
                        .poster(URI.create("https://images-na.ssl-images-amazon.com/images/M/MV5BMmIyM2Y2NTQtZWM4ZC00YjFjLWFhMjgtNzNkOTQ0OGZlMTNhXkEyXkFqcGdeQXVyMTQxNzMzNDI@._V1_SX300.jpg"))
                        .metascore(77.)
                        .imdbRating(7.6)
                        .imdbVotes(211.006)
                ,

                Movie.builder()
                        .imdbID("tt0120684")
                        .title("Gods and Monsters")
                        .year(Year.of(1998))
                        .rated("R")
                        .released(parseReleased.apply("04 Nov 1998"))
                        .runtime(parseRuntime.apply("105 min"))
                        .genre(EnumSet.of(Biography, Drama))
                        .director(singletonList(new Person("Bill Condon")))
                        .writer(asList(new Person("Christopher Bram", "(novel)"), new Person("Bill Condon", "(screenplay)")))
                        .actors(asList(new Person("Ian McKellen"), new Person("Brendan Fraser"), new Person("Lynn Redgrave"), new Person("Lolita Davidovich")))
                        .plot("The last days of Frankenstein (1931) director James Whale are explored.")
                        .language("English, Hungarian")
                        .country("USA, UK")
                        .awards("Won 1 Oscar. Another 37 wins & 32 nominations.")
                        .poster(URI.create("http://ia.media-imdb.com/images/M/MV5BMTk0Mjk4NDIzNl5BMl5BanBnXkFtZTcwNTk0NjcyMQ@@._V1_SX300.jpg"))
                        .metascore(74.)
                        .imdbRating(7.5)
                        .imdbVotes(25.003)
                ,

                Movie.builder()
                        .imdbID("tt0486674")
                        .title("What Just Happened")
                        .year(Year.of(2008))
                        .rated("R")
                        .released(parseReleased.apply("31 Oct 2008"))
                        .runtime(parseRuntime.apply("104 min"))
                        .genre(EnumSet.of(Comedy, Drama))
                        .director(singletonList(new Person("Barry Levinson")))
                        .writer(asList(new Person("Art Linson"), new Person("Art Linson", "(book)")))
                        .actors(asList(new Person("Robert De Niro"), new Person("Sean Penn"), new Person("Catherine Keener"), new Person("Bruce Willis")))
                        .plot("Two weeks in the life of a fading Hollywood producer who's having a rough time trying to get his new picture made.")
                        .language("English, Hebrew, French")
                        .country("USA")
                        .awards("1 nomination.")
                        .poster(URI.create("http://ia.media-imdb.com/images/M/MV5BNTEyMDg3ODAwNl5BMl5BanBnXkFtZTcwMzM3MTc3MQ@@._V1_SX300.jpg"))
                        .metascore(null)
                        .imdbRating(5.7)
                        .imdbVotes(23.822)
        );
    }

    private ExampleMovies() {
    }
}
