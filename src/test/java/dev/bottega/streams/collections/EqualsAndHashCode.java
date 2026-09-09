package dev.bottega.streams.collections;

import org.junit.jupiter.api.Test;
import dev.bottega.streams.boilerplatefree.Person;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class EqualsAndHashCode {

    // rules for implementing equals and hashCode methods:

    // equals is a
    // reflexive relation:
    //  a is equal to a
    // symmetric relation:
    //  a is equal to b, so b is equal to a
    // transitive relation:
    //  a is equal to b, b is equal to c so a is equal to c

    // hashcode is consistent with equal method
    // if a is equal to b then a.hashcode is equal to b.hashcode

    // it is very important to have correct equals and hashcode
    // for classes used in Set and as keys in Map

    @Test
    public void equalsByValue() throws Exception {

        // String have value equality semantic

        Set<String> persons = new HashSet<>();
        persons.add(new String("Bella"));
        persons.add(new String("Cody"));
        persons.add(new String("Cody")); // another name Cody

        for (String person : persons) {
            System.out.println(person);
        }
        assertThat(persons)
                .hasSize(2);

        assertThat(new String("Cody")).
                isNotSameAs(new String("Cody"));
    }

    @Test
    public void lombokValueSemantic() throws Exception {

        // Default implementation from Object class
        // have reference equality semantic
        // object is equal only to itself

        Set<Person> persons = new HashSet<>();
        persons.add(new Person("Bella"));
        persons.add(new Person("Cody"));
        persons.add(new Person("Cody")); // another person called Cody

        for (Person person : persons) {
            System.out.println(person.name());
        }
        assertThat(persons).hasSize(2);
    }

    // Plain class, no equals/hashCode override -> Object's default reference equality.
    // (A record would use value equality and collapse the two "Cody" instances.)
    private static class Dog {
        final String name;
        Dog(String name) {
            this.name = name;
        }
    }

    private record Cat(String name) {


        @Override
        public boolean equals(Object o) {
            return this == o;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }
    }

    @Test
    public void defaultImplementations() throws Exception {

        // Default implementation from Object class
        // have reference equality semantic
        // object is equal only to itself

        Set<Dog> dogs = new HashSet<>();
        dogs.add(new Dog("Bella"));
        dogs.add(new Dog("Cody"));
        dogs.add(new Dog("Cody")); // another person called Cody

        for (Dog dog : dogs) {
            System.out.println(dog.name);
        }
        assertThat(dogs).hasSize(3);
    }

    // other important thing is to make those classes immutable
    @Test
    public void mutableMapKeysProblem() throws Exception {

        // old mutable java.util.Date representation
        Date date20160615 = new Date(2016 - 1900, 6 - 1, 15);

        Map<Date, Person> birthdays = new HashMap<>();
        birthdays.put(date20160615, new Person("Spark"));

        // obviously it is there
        assertThat(birthdays).containsKey(date20160615);

        // change value of year
        Date duplicate = date20160615;
        date20160615.setYear(2020 - 1900);
        // same date instance but internal state is changed
        assertThat(date20160615).isSameAs(duplicate);

        // but we will not find it in map
        //Assertions.assertThat(birthdays).containsKey(date20160615);

        birthdays.put(date20160615, new Person("Key"));
        assertThat(birthdays).containsKey(date20160615);
        assertThat(birthdays).hasSize(2);
        System.out.println(birthdays);
    }
}
