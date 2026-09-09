package dev.bottega.streams.almostlikefunctional.excercise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Gatherers;
import java.util.stream.Stream;

/**
 * High-level token import pipeline. Self-contained: the only external type it relies on
 * is the sealed {@link Token} interface (in its own file). Everything else — the
 * outcome (success / problem-as-value), the batch-insert seam, and the immutable report
 * — lives here.
 *
 * <pre>{@code line -> row -> outcome -> window -> batch outcome -> report}</pre>
 *
 * Business complexity (the case switch over token kinds, the switch over success/problem)
 * stays visible inline; technical plumbing (column split, the {@link Gatherers#windowFixed}
 * gatherer) is hidden behind named steps. Problems are values — never thrown.
 */
public final class TokenImport {

    private TokenImport() {
    }

    public static ImportReport importTokens(List<String> lines, int batchSize, BatchInserter inserter) {
        return null;
        // TODO
        // line -> row
        // row -> outcome
        // outcome -> window (tokens + problems)
        // window -> batch outcome (problems filtered internally)
        // -> outcome stream
        // -> report
    }

    private static Row row(String rawLine) {
        return new Row(rawLine, rawLine.split("\\s*,\\s*"));
    }

    /**
     * Business dispatch over every token kind (inline switch). Each per-kind business rule
     * lives inside its own {@code parse*} function, so a {@link Token} is only ever built
     * when it passes validation.
     */
    private static Outcome<Token> outcome(Row row) {
        return switch (row.kind()) {
            case "CARD"         -> parseCard(row.columns(), row.raw());
            case "MOBILE_APP"   -> parseMobileApp(row.columns(), row.raw());
            case "CREDIT_CARD"  -> parseCreditCard(row.columns(), row.raw());
            case "DEVICE_MODEM" -> parseDeviceModem(row.columns(), row.raw());
            default             -> Outcome.err(new Problem.UnknownKind(row.kind()));
        };
    }

    // ===== per-type parsing details (parse + validate in one step) =====

    private static Outcome<Token> parseCard(String[] c, String line) {
        if (c.length < 4) {
            return Outcome.err(new Problem.InvalidFormat(line, "CARD requires rfid, visual number and holder"));
        }
        String rfid = c[1], visual = c[2], holder = c[3];
        if (rfid.isBlank() || visual.isBlank()) {
            return Outcome.err(new Problem.BusinessRule("card requires a non-blank rfid and visual number"));
        }
        return Outcome.ok(new Token.Card(rfid, visual, holder));
    }

    private static Outcome<Token> parseMobileApp(String[] c, String line) {
        if (c.length < 4) {
            return Outcome.err(new Problem.InvalidFormat(line, "MOBILE_APP requires deviceId, pushToken, platform"));
        }
        String deviceId = c[1], pushToken = c[2];
        if (pushToken.isBlank()) {
            return Outcome.err(new Problem.BusinessRule("mobile-app requires a non-blank push token"));
        }
        return parsePlatform(c[3], line).map(platform -> new Token.MobileApp(deviceId, pushToken, platform));
    }

    private static Outcome<Token> parseCreditCard(String[] c, String line) {
        if (c.length < 3) {
            return Outcome.err(new Problem.InvalidFormat(line, "CREDIT_CARD requires pan and expiry"));
        }
        String pan = c[1], expiry = c[2];
        if (!pan.matches("\\d{13,19}") || expiry.isBlank()) {
            return Outcome.err(new Problem.BusinessRule("credit-card pan must be 13-19 digits and expiry non-blank"));
        }
        return Outcome.ok(new Token.CreditCard(pan, expiry));
    }

    private static Outcome<Token> parseDeviceModem(String[] c, String line) {
        if (c.length < 4) {
            return Outcome.err(new Problem.InvalidFormat(line, "DEVICE_MODEM requires imsi, iccid, serial"));
        }
        String imsi = c[1];
        if (imsi.isBlank()) {
            return Outcome.err(new Problem.BusinessRule("device-modem requires a non-blank imsi"));
        }
        return Outcome.ok(new Token.DeviceModem(imsi, c[2], c[3]));
    }

    private static Outcome<Token.Platform> parsePlatform(String raw, String line) {
        return switch (raw.strip().toUpperCase()) {
            case "ANDROID" -> Outcome.ok(Token.Platform.ANDROID);
            case "IOS"     -> Outcome.ok(Token.Platform.IOS);
            default        -> Outcome.err(new Problem.InvalidFormat(line, "unknown platform: " + raw));
        };
    }

    private record Row(String raw, String[] columns) {
        private String kind() {
            return columns.length == 0 ? "" : columns[0].strip();
        }
    }

    // ===== nested supporting types =====

    /** Result as a value — success ({@link Ok}) or a {@link Problem} ({@link Err}); nothing throws. */
    public sealed interface Outcome<T> {

        record Ok<T>(T value) implements Outcome<T> {}

        record Err<T>(Problem problem) implements Outcome<T> {}

        static <T> Outcome<T> ok(T value) {
            return new Ok<>(value);
        }

        static <T> Outcome<T> err(Problem problem) {
            return new Err<>(problem);
        }

        default <R> Outcome<R> map(Function<? super T, ? extends R> f) {
            return switch (this) {
                case Ok<T> ok -> Outcome.ok(f.apply(ok.value()));
                case Err<T> err -> Outcome.err(err.problem());
            };
        }

        default <R> Outcome<R> flatMap(Function<? super T, Outcome<R>> f) {
            return switch (this) {
                case Ok<T> ok -> f.apply(ok.value());
                case Err<T> err -> Outcome.err(err.problem());
            };
        }
    }

    /** Domain problems represented as values — never thrown as exceptions. */
    public sealed interface Problem {

        String message();

        record InvalidFormat(String raw, String reason) implements Problem {
            @Override
            public String message() {
                return "invalid format (" + reason + ") <" + raw + ">";
            }
        }

        record UnknownKind(String kind) implements Problem {
            @Override
            public String message() {
                return "unknown token kind: " + kind;
            }
        }

        record Duplicate(String key) implements Problem {
            @Override
            public String message() {
                return "duplicate token, already stored: " + key;
            }
        }

        record BusinessRule(String reason) implements Problem {
            @Override
            public String message() {
                return "business rule: " + reason;
            }
        }
    }

    /**
     * The higher-order seam: a batch insert receives a window of outcomes (tokens mixed
     * with problems), keeps the problems as they are, applies the insert to the tokens,
     * and returns one {@link Outcome} per input element — everything "as it came in".
     */
    @FunctionalInterface
    public interface BatchInserter {

        List<Outcome<Token>> insert(List<Outcome<Token>> batch);

        /** A thread-safe in-memory "database" keyed on {@link Token#key()}. */
        static BatchInserter into(Set<String> alreadyStored) {
            return batch -> batch.stream()
                    .flatMap(outcome -> switch (outcome) {
                        case Outcome.Err<Token> problem -> Stream.of(problem);   // problems pass through untouched
                        case Outcome.Ok<Token> ok -> Stream.of(alreadyStored.add(ok.value().key())
                                ? ok                                            // fresh -> success
                                : Outcome.<Token>err(new Problem.Duplicate(ok.value().key()))); // duplicate -> problem
                    })
                    .toList();
        }
    }

    /** Immutable aggregation of a whole import run. */
    public record ImportReport(
            int total,
            int inserted,
            int rejected,
            List<String> problems,
            Map<Token.Kind, Long> byKind) {

        public ImportReport {
            problems = List.copyOf(problems);
            byKind = Map.copyOf(byKind);
        }

        public static ImportReport empty() {
            return new ImportReport(0, 0, 0, List.of(), Map.of());
        }

        public ImportReport accept(Outcome<Token> outcome) {
            return switch (outcome) {
                case Outcome.Ok<Token> ok -> inserted(ok);
                case Outcome.Err<Token> err -> rejected(err);
            };
        }

        private ImportReport inserted(Outcome.Ok<Token> ok) {
            return new ImportReport(total + 1, inserted + 1, rejected, problems,
                    bumped(byKind, ok.value().kind()));
        }

        private ImportReport rejected(Outcome.Err<Token> err) {
            return new ImportReport(total + 1, inserted, rejected + 1,
                    appended(problems, err.problem().message()), byKind);
        }

        public ImportReport merge(ImportReport other) {
            return new ImportReport(
                    total + other.total,
                    inserted + other.inserted,
                    rejected + other.rejected,
                    concatenated(problems, other.problems),
                    mergedKinds(byKind, other.byKind));
        }

        /**
         * {@link Collectors#teeing} collector: one downstream counts the successful inserts
         * per token kind, the other collects the problem messages, and the merge folds both
         * tallies into a single immutable {@link ImportReport}.
         */
        public static Collector<Outcome<Token>, ?, ImportReport> collector() {
            return Collectors.teeing(
                    // success side: inserted count per token kind
                    Collectors.filtering(
                            outcome -> outcome instanceof Outcome.Ok<?>,
                            Collectors.groupingBy(
                                    outcome -> ((Outcome.Ok<Token>) outcome).value().kind(),
                                    Collectors.counting())),
                    // problem side: problem messages, forwarded as-is
                    Collectors.mapping(
                            outcome -> outcome instanceof Outcome.Err<Token> err ? err.problem().message() : null,
                            Collectors.filtering(Objects::nonNull, Collectors.toList())),
                    // merge both tallies into one immutable report
                    (byKind, problems) -> {
                        int inserted = (int) byKind.values().stream().mapToLong(Long::longValue).sum();
                        int rejected = problems.size();
                        return new ImportReport(inserted + rejected, inserted, rejected, problems, Map.copyOf(byKind));
                    });
        }

        private static Map<Token.Kind, Long> bumped(Map<Token.Kind, Long> source, Token.Kind kind) {
            HashMap<Token.Kind, Long> copy = new HashMap<>(source);
            copy.merge(kind, 1L, Long::sum);
            return copy;
        }

        private static Map<Token.Kind, Long> mergedKinds(Map<Token.Kind, Long> left, Map<Token.Kind, Long> right) {
            HashMap<Token.Kind, Long> copy = new HashMap<>(left);
            right.forEach((kind, count) -> copy.merge(kind, count, Long::sum));
            return copy;
        }

        private static List<String> appended(List<String> source, String message) {
            if (source.isEmpty()) {
                return List.of(message);
            }
            ArrayList<String> copy = new ArrayList<>(source);
            copy.add(message);
            return List.copyOf(copy);
        }

        private static List<String> concatenated(List<String> left, List<String> right) {
            if (left.isEmpty()) {
                return right;
            }
            if (right.isEmpty()) {
                return left;
            }
            ArrayList<String> copy = new ArrayList<>(left);
            copy.addAll(right);
            return List.copyOf(copy);
        }
    }
}
