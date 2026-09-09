package dev.bottega.streams.almostlikefunctional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class PatternMatchingTest {

    sealed interface Entity {
    }

    record Person(String name, Integer age)  implements Entity {}
    record Animal(String name)  implements Entity {}

    static String recordInference(Entity pair){
        return switch (pair) {
            case Person(var name, var age) when age == null -> name;
            case Person(var name, var age) -> name + " " + age;
            case Animal a -> null;
        };
    }

    @Test
    public void testPattenMatching() throws Exception {
        Person michal = new Person("Michal", null);
        Assertions.assertThat(recordInference(michal)).isEqualTo("Michal");

        Person oldMichal = new Person("Michal", 40);
        Assertions.assertThat(recordInference(oldMichal)).isEqualTo("Michal 40");
    }

}
