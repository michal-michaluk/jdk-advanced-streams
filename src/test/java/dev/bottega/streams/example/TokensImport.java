package dev.bottega.streams.example;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TokensImport {

    static class Messages {
        static final String PARSE_ISSUE = "requires two columns separated by , or ; (colon or semicolon)";
        static final String WRONG_RFID = "second column need to be proper rfid, hexadecimal positive value (containing characters 0-9 A-F)";
        static final String WRONG_NUMBER = "first column need to be proper visual number, containing only digits, uppercase letters and dash";
        static final String SKIPPED_EXISTING = "skipped due to previous existence of given rfid in database";
    }

    private static class Validations {
        static final Predicate<String> number = Pattern.compile("[0-9A-Z-]+").asMatchPredicate();
        static final Predicate<String> rfid = Validations::isPositiveHexadecimal;

        private static boolean isPositiveHexadecimal(String rfid) {
            if (rfid == null || rfid.isEmpty()) {
                return false;
            }
            if (!rfid.chars().allMatch(Validations::isHexDigit)) {
                return false;
            }
            try {
                return Long.valueOf(rfid, 16) > 0;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        private static boolean isHexDigit(int ch) {
            return switch (ch) {
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> true;
                case 'a', 'b', 'c', 'd', 'e', 'f' -> true;
                case 'A', 'B', 'C', 'D', 'E', 'F' -> true;
                default -> false;
            };
        }
    }

    private final Function<TokenDetails, Boolean> create;

    TokensImport(Function<TokenDetails, Boolean> create) {
        this.create = create;
    }

    ImportReport importLines(Stream<String> lines) throws ExecutionException, InterruptedException {
        ForkJoinPool threads = new ForkJoinPool(16);
        return threads.submit(() -> lines
                .filter(line -> !line.isBlank())
                .map(Line::of)
                .parallel()
                .map(line -> switch (line) {
                    case LineToImport l -> importLine(l);
                    case LineIssue i -> i;
                }).collect(ImportReport.collector())).get();
    }

    private ImportStatus importLine(LineToImport line) {
        try {
            TokenDetails tokenDetails = line.toDetails();
            if (create.apply(tokenDetails)) {
                return line.success();
            } else {
                return line.skipped();
            }
        } catch (Exception e) {
            return line.exception(e);
        }
    }

    public record LineInfo(String line, String message) {
    }

    private sealed interface Line {
        private static Line of(String line) {
            String[] split = line.split("[;,]");
            if (split.length != 2) {
                return new LineIssue(line, Messages.PARSE_ISSUE);
            } else {
                String number = split[0].strip();
                String rfid = split[1].strip();
                if (!Validations.number.test(number)) {
                    return new LineIssue(line, Messages.WRONG_NUMBER);
                } else if (!Validations.rfid.test(rfid)) {
                    return new LineIssue(line, Messages.WRONG_RFID);
                } else {
                    return new LineToImport(line, number, rfid);
                }
            }
        }
    }

    private record LineToImport(String line, String number, String rfid) implements Line {
        private TokenDetails toDetails() {
            return TokenDetails.card(rfid, number);
        }

        private ImportStatus success() {
            return new ImportSuccess(this);
        }

        private ImportStatus skipped() {
            return new ImportSkipped(this);
        }

        private ImportStatus exception(Exception e) {
            return new ImportError(this, e);
        }
    }

    private sealed interface ImportStatus {
    }

    private record LineIssue(String line, String message) implements Line, ImportStatus {
        private LineInfo describe() {
            return new LineInfo(line, message);
        }
    }

    private record ImportSuccess(LineToImport line) implements ImportStatus {
    }

    private record ImportSkipped(LineToImport line) implements ImportStatus {
        private LineInfo describe() {
            return new LineInfo(line.line, Messages.SKIPPED_EXISTING);
        }
    }

    private record ImportError(LineToImport line, Exception exception) implements ImportStatus {
        private LineInfo describe() {
            return new LineInfo(line.line, exception.getMessage()
                    + Optional.ofNullable(exception.getCause())
                    .map(Throwable::getMessage)
                    .map(message -> ", cause: " + message)
                    .orElse("")
            );
        }
    }

    @ToString
    @EqualsAndHashCode
    public static class ImportReport {
        private int all;
        private int success;
        private final List<LineInfo> skipped;
        private final List<LineInfo> errors;

        public int allLinesCount() {
            return all;
        }

        public int successCount() {
            return success;
        }

        public String skippedAsText(String delimiter, Function<LineInfo, String> mapper) {
            return skipped.stream()
                    .map(mapper)
                    .collect(Collectors.joining(delimiter));
        }

        public String errorsAsText(String delimiter, Function<LineInfo, String> mapper) {
            return errors.stream()
                    .map(mapper)
                    .collect(Collectors.joining(delimiter));
        }

        public long skippedCount() {
            return skipped.size();
        }

        public long errorsCount() {
            return errors.size();
        }

        private static Collector<ImportStatus, ImportReport, ImportReport> collector() {
            return Collector.of(
                    ImportReport::new,
                    ImportReport::apply,
                    ImportReport::merge
            );
        }

        ImportReport() {
            all = 0;
            success = 0;
            skipped = new ArrayList<>();
            errors = new ArrayList<>();
        }

        ImportReport(int all, int success, List<LineInfo> skipped, List<LineInfo> errors) {
            this.all = all;
            this.success = success;
            this.skipped = skipped;
            this.errors = errors;
        }

        private void apply(ImportStatus line) {
            all++;
            switch (line) {
                case LineIssue issue -> errors.add(issue.describe());
                case ImportSuccess ignored -> success++;
                case ImportSkipped status -> skipped.add(status.describe());
                case ImportError status -> errors.add(status.describe());
            }
        }

        private ImportReport merge(ImportReport other) {
            ImportReport merged = new ImportReport();
            merged.all = this.all + other.all;
            merged.success = this.success + other.success;
            merged.skipped.addAll(this.skipped);
            merged.skipped.addAll(other.skipped);
            merged.errors.addAll(this.errors);
            merged.errors.addAll(other.errors);
            return merged;
        }
    }
}
