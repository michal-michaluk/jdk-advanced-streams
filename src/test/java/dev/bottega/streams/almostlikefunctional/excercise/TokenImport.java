package dev.bottega.streams.almostlikefunctional.excercise;

import java.util.*;
import java.util.stream.*;

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
        return lines.stream()
                .map(Parser::row)
                .map(Parser::parseToken)
                .gather(Gatherers.windowFixed(batchSize))
                .map(inserter::insert)
                .flatMap(Collection::stream)
                .collect(Collectors.teeing(
                        // success side: inserted count per token kind
                        Collectors.filtering(
                                outcome -> outcome instanceof Result.Ok<?>,
                                Collectors.groupingBy(
                                        outcome -> ((Result.Ok<Token>) outcome).value().kind(),
                                        Collectors.counting())),
                        // problem side: problem messages, forwarded as-is
                        Collectors.mapping(
                                outcome -> outcome instanceof Result.Err<Token> err ? err.problem().message() : null,
                                Collectors.filtering(Objects::nonNull, Collectors.toList())),
                        // merge both tallies into one immutable report
                        (byKind, problems) -> {
                            int inserted = (int) byKind.values().stream().mapToLong(Long::longValue).sum();
                            int rejected = problems.size();
                            return new ImportReport(inserted + rejected, inserted, rejected, problems, Map.copyOf(byKind));
                        }))
                ;
    }


    record Row(String raw, String[] columns) {
        String kind() {
            return columns.length == 0 ? "" : columns[0].strip();
        }
    }

    // ===== nested supporting types =====

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
     * and returns one {@link Result} per input element — everything "as it came in".
     */
    @FunctionalInterface
    public interface BatchInserter {

        List<Result<Token>> insert(List<Result<Token>> batch);

        /** A thread-safe in-memory "database" keyed on {@link Token#key()}. */
        static BatchInserter into(Set<String> alreadyStored) {
            return batch -> batch.stream()
                    .flatMap(outcome -> switch (outcome) {
                        case Result.Err<Token> problem -> Stream.of(problem);   // problems pass through untouched
                        case Result.Ok<Token> ok -> Stream.of(alreadyStored.add(ok.value().key())
                                ? ok                                            // fresh -> success
                                : Result.<Token>err(new Problem.Duplicate(ok.value().key()))); // duplicate -> problem
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

        public ImportReport accept(Result<Token> outcome) {
            return switch (outcome) {
                case Result.Ok<Token> ok -> inserted(ok);
                case Result.Err<Token> err -> rejected(err);
            };
        }

        private ImportReport inserted(Result.Ok<Token> ok) {
            return new ImportReport(total + 1, inserted + 1, rejected, problems,
                    bumped(byKind, ok.value().kind()));
        }

        private ImportReport rejected(Result.Err<Token> err) {
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
