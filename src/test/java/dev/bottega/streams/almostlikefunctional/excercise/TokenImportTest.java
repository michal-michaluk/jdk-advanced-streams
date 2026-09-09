package dev.bottega.streams.almostlikefunctional.excercise;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static dev.bottega.streams.almostlikefunctional.excercise.TokenImport.BatchInserter;
import static dev.bottega.streams.almostlikefunctional.excercise.TokenImport.ImportReport;
import static dev.bottega.streams.almostlikefunctional.excercise.TokenImport.Outcome;
import static dev.bottega.streams.almostlikefunctional.excercise.TokenImport.Problem;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end demonstration of the modern import pipeline:
 * row → outcome (record patterns + {@code when} guards) → batch insert (Gatherer
 * behind a higher-order {@link BatchInserter}) → immutable {@link ImportReport} via a
 * {@link java.util.stream.Collector}. Problems are values (never thrown), the whole
 * model is immutable, and only JDK-finish features are used.
 */
class TokenImportTest {

    @Test
    void parsesEveryKindAndReportsImports() {
        List<String> lines = List.of(
                "CARD, a1, 12345, Michal",
                "MOBILE_APP, dev-1, push-a, ANDROID",
                "CREDIT_CARD, 4111111111111111, 12/26",
                "DEVICE_MODEM, imsi-1, iccid-1, s1");

        ImportReport report = run(lines, 4, BatchInserter.into(newKeySet()));

        assertThat(report.total()).isEqualTo(4);
        assertThat(report.inserted()).isEqualTo(4);
        assertThat(report.rejected()).isZero();
        assertThat(report.byKind())
                .containsEntry(Token.Kind.CARD, 1L)
                .containsEntry(Token.Kind.MOBILE_APP, 1L)
                .containsEntry(Token.Kind.CREDIT_CARD, 1L)
                .containsEntry(Token.Kind.DEVICE_MODEM, 1L);
        assertThat(report.problems()).isEmpty();
    }

    @Test
    void rejectsProblemsAsValuesInsteadOfThrowing() {
        List<String> lines = List.of(
                "CARD, a1, 12345, Michal",
                "CARD, a1, 99999, Other",      // duplicate business key -> Duplicate
                "MOBILE_APP, dev-2, , IOS",    // blank push token -> BusinessRule (guard)
                "ALIEN, x",                    // unknown kind -> UnknownKind
                "CARD, onlyone");              // too few columns -> InvalidFormat

        ImportReport report = run(lines, 2, BatchInserter.into(newKeySet()));

        assertThat(report.total()).isEqualTo(5);
        assertThat(report.inserted()).isEqualTo(1);
        assertThat(report.rejected()).isEqualTo(4);
        assertThat(report.problems()).hasSize(4);
        assertThat(report.problems()).anyMatch(message -> message.contains("duplicate token"));
        assertThat(report.problems()).anyMatch(message -> message.contains("business rule"));
        assertThat(report.problems()).anyMatch(message -> message.contains("unknown token kind"));
        assertThat(report.problems()).anyMatch(message -> message.contains("invalid format"));
    }

    @Test
    void insertsInBatchesOfAtMostBatchSize() {
        List<List<Outcome<Token>>> submitted = new ArrayList<>();
        BatchInserter recordingInserter = batch -> {
            submitted.add(batch);
            return batch;
        };

        List<String> lines = List.of(
                "CARD, a1, 1, Michal",
                "CARD, a2, 2, Michal",
                "CARD, a3, 3, Michal",
                "CARD, a4, 4, Michal",
                "CARD, a5, 5, Michal");

        ImportReport report = run(lines, 2, recordingInserter);

        assertThat(submitted).extracting(List::size).containsExactly(2, 2, 1);
        assertThat(report.inserted()).isEqualTo(5);
        assertThat(report.byKind()).containsEntry(Token.Kind.CARD, 5L);
    }

    @Test
    void passesParseProblemsThroughWithoutInsertion() {
        List<List<Outcome<Token>>> submitted = new ArrayList<>();
        BatchInserter recordingInserter = batch -> {
            submitted.add(batch);
            return batch;
        };

        List<String> lines = List.of(
                "ALIEN, x",
                "CARD, a1, 1, Michal",
                "MOBILE_APP, d, t, IOS",
                "ALIEN, y");

        ImportReport report = run(lines, 10, recordingInserter);

        // the window handed to the inserter carries the problems alongside the tokens
        assertThat(submitted).hasSize(1);
        assertThat(submitted.getFirst()).filteredOn(outcome -> outcome instanceof Outcome.Err<?>).hasSize(2);
        assertThat(submitted.getFirst()).filteredOn(outcome -> outcome instanceof Outcome.Ok<?>).hasSize(2);
        assertThat(report.inserted()).isEqualTo(2);   // the two tokens
        assertThat(report.rejected()).isEqualTo(2);   // the two problems
    }

    @Test
    void reportIsImmutableAndUnmodifiable() {
        ImportReport report = ImportReport.empty()
                .accept(Outcome.ok(new Token.Card("a1", "123", "Michal")))
                .accept(Outcome.err(new Problem.Duplicate("a2")));

        Assertions.assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> report.problems().add("boom"));
        Assertions.assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> report.byKind().put(Token.Kind.CARD, 99L));
        assertThat(report.inserted()).isEqualTo(1);
        assertThat(report.rejected()).isEqualTo(1);
    }

    private static ImportReport run(List<String> lines, int batchSize, BatchInserter inserter) {
        return TokenImport.importTokens(lines, batchSize, inserter);
    }

    private static Set<String> newKeySet() {
        return ConcurrentHashMap.newKeySet();
    }
}
