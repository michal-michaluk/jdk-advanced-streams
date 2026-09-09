package dev.bottega.streams.almostlikefunctional;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.*;

import static org.assertj.core.api.Assertions.assertThat;

public class BasicsMethodReferencesTest {

    @Test
    public void methodReferenceAsComparator() throws Exception {
        Comparator<String> comparator = String::compareTo;

        String[] arr = {"apple", "strawberry", "Orange"};

        Arrays.sort(arr, comparator);

        assertThat(arr).containsExactly("Orange", "apple", "strawberry");
    }

    @Test
    public void methodReference() throws Exception {
        Consumer<String> printlnL = (x) -> System.out.println(x);
        Consumer<String> printlnMR = System.out::println;

        printlnMR.accept("hello world");
    }

    @Test
    public void staticReference() throws Exception {
        Function<Object, String> toStringL = (obj) -> String.valueOf(obj);
        Function<Object, String> toStringMR = String::valueOf;

        String string = toStringMR.apply(Arrays.asList(1, 2.3, "Michał"));
        assertThat(string).isEqualTo("[1, 2.3, Michał]");
    }

    @Test
    public void constructorReference() throws Exception {
        Supplier<List<String>> newListL = () -> new LinkedList<>();
        Supplier<List<String>> newListMR = LinkedList::new;

        List<String> strings1 = newListMR.get();
        List<String> strings2 = newListMR.get();

        assertThat(strings1)
                .isNotSameAs(strings2)
                .isInstanceOf(List.class)
                .isExactlyInstanceOf(LinkedList.class);
    }

    @Test
    public void instanceMethodReference() throws Exception {
        Predicate<String> isMichalL = (anObject) -> "Michał".equals(anObject);
        Predicate<String> isMichalMR = "Michał"::equals;

        boolean test = isMichalMR.test("Kasia");
        assertThat(test).isFalse();
    }

    @Test
    public void getterReferenceHookedToInstance() throws Exception {
        LocalDate now = LocalDate.now();

        Supplier<Integer> year = now::getYear;

        assertThat(year.get())
                .isEqualTo(now.getYear())
                .isGreaterThanOrEqualTo(2016);
    }

    @Test
    public void getterReferenceApplicableToManyInstances() throws Exception {
        LocalDate now = LocalDate.now();

        Function<LocalDate, Integer> year = LocalDate::getYear;

        assertThat(year.apply(now))
                .isGreaterThanOrEqualTo(2016);
    }

    @Test
    public void setterReferenceHookedToInstance() throws Exception {
        List<String> list = new LinkedList<>();

        Consumer<String> append = list::add;

        assertThat(list).isEmpty();

        append.accept("Hi all!");

        assertThat(list)
                .containsExactly("Hi all!");
    }

    @Test
    public void setterReferenceApplicableToManyInstances() throws Exception {
        List<String> list = new LinkedList<>();

        BiConsumer<List, String> append = List<String>::add;

        assertThat(list).isEmpty();

        append.accept(list, "Hi all!");

        assertThat(list)
                .containsExactly("Hi all!");
    }

}
