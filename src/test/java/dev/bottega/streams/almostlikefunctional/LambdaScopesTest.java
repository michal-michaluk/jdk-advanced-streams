package dev.bottega.streams.almostlikefunctional;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class LambdaScopesTest {

    LocalDate today = LocalDate.now();

    @Test
    public void noScope() throws Exception {
        Supplier<Integer> year = () -> LocalDate.now().getYear();
        Function<LocalDate, Integer> getYear = LocalDate::getYear;

        assertThat(year.get())
                .isGreaterThanOrEqualTo(2016);

        Integer gotYear = getYear.apply(
                LocalDate.of(2015, Month.DECEMBER, 24)
        );

        assertThat(gotYear)
                .isEqualTo(2015);
    }

    @Test
    public void localVariableInScope() throws Exception {
        LocalDate now = LocalDate.now();
        Supplier<Integer> year = () -> now.getYear();

        assertThat(year.get())
                .isGreaterThanOrEqualTo(2016)
                .isEqualTo(now.getYear());

        // must be final or implicitly final
        //now = LocalDate.of(2015, Month.DECEMBER, 24);
    }

    @Test
    public void fieldInScope() throws Exception {
        Supplier<Integer> year = () -> today.getYear();

        assertThat(year.get())
                .isGreaterThanOrEqualTo(2016)
                .isEqualTo(today.getYear());

        today = LocalDate.of(2015, Month.DECEMBER, 24);

        assertThat(year.get())
                .isEqualTo(2015);
                //.isEqualTo(2016);
    }

    @Test
    public void staticFieldInScope() throws Exception {
        Supplier<Integer> year = () -> Year.MAX_VALUE;

        assertThat(year.get())
                .isGreaterThanOrEqualTo(2016)
                .isEqualTo(999_999_999);
    }

    @Test
    public void funky() throws Exception {
        LocalDate now = LocalDate.now();
        Supplier<Integer> year = now::getYear;
        // but check that:
        //Supplier<Integer> year = () -> today.getYear();

        assertThat(year.get())
                .isGreaterThanOrEqualTo(2016)
                .isEqualTo(now.getYear());

        now = LocalDate.of(2015, Month.DECEMBER, 24);
        //today = LocalDate.of(2015, Month.DECEMBER, 24);

        assertThat(year.get())
                .isGreaterThanOrEqualTo(2016);
                //.isEqualTo(2015);
        System.out.println(now);
    }

    @Test // don't use neither Double nor BigDecimals to money computation,
    // go for javax.money money-api or other money api
    public void higherOrderFunction() throws Exception {
        Function<String, Function<Double, Double>> taxFactory = this::tax;

        Double tax = taxFactory.apply("PL").apply(159.99);

        assertThat(tax)
                .isCloseTo(196.78, within(0.01));
    }

    private Function<Double, Double> tax(String region) {
        switch (region) {
            case "PL":
                return price -> 1.23 * price;
            case "DE":
                return price -> 1.19 * price;
            case "TaxFree":
                return price -> price;
            default:
                throw new IllegalArgumentException("unsupported " + region);
        }
    }
}
