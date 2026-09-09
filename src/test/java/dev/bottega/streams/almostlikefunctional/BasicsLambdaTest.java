package dev.bottega.streams.almostlikefunctional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;

public class BasicsLambdaTest {

    @Test
    public void classicAnonymousClass() throws Exception {
        Comparator<String> insensitive = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.toLowerCase().compareTo(o2.toLowerCase());
            }
        };

        String[] arr = {"Orange", "apple", "strawberry"};

        Arrays.sort(arr, insensitive);

        Assertions.assertThat(arr).containsExactly("apple", "Orange", "strawberry");
    }

    @Test
    public void fullyExpandedLambda() throws Exception {
        Comparator<String> insensitive = (String o1, String o2) -> {
            return o1.toLowerCase().compareTo(o2.toLowerCase());
        };

        String[] arr = {"Orange", "apple", "strawberry"};

        Arrays.sort(arr, insensitive);

        Assertions.assertThat(arr).containsExactly("apple", "Orange", "strawberry");
    }

    @Test
    public void lambda() throws Exception {
        Comparator<String> insensitive = (o1, o2) -> {

            return o1.toLowerCase().compareTo(o2.toLowerCase());
        };

        String[] arr = {"Orange", "apple", "strawberry"};

        Arrays.sort(arr, insensitive);

        Assertions.assertThat(arr).containsExactly("apple", "Orange", "strawberry");
    }

    @Test
    public void expressionLambda() throws Exception {
        Comparator<String> insensitive = (o1, o2) ->
                o1.toLowerCase().compareTo(o2.toLowerCase());
        // note: type inference requires typed target reference

        String[] arr = {"Orange", "apple", "strawberry"};

        Arrays.sort(arr, insensitive);

        Assertions.assertThat(arr)
                .containsExactly("apple", "Orange", "strawberry");
    }

    // use constant String.CASE_INSENSITIVE_ORDER instead defining own comparator
}
