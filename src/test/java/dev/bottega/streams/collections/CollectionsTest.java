package dev.bottega.streams.collections;

import org.junit.jupiter.api.Test;
import dev.bottega.streams.boilerplatefree.Person;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class CollectionsTest {

    // List is most commonly used collection
    // ArrayList is most commonly used implementation
    private List<Person> arrayList = new ArrayList<>();

    // List implemented as linked elements - random access is slow
    private List<Person> linkedList = new LinkedList<>();

    // Sets are optimised for add and contains operation
    // hash set has 'random' order when looping
    // equals and hashCode methods are very important in that case
    private Set<String> hashSet = new HashSet<>();

    // Tree set has looping order over elements
    // related to implementation of compareTo() in stored Classes
    // hashed implementation has better performance then that
    private SortedSet<String> treeSet = new TreeSet<>();

    // Linked set has looping order over elements
    // related to order of put operations (like queue or list)
    // similar performance like hashed implementation
    private Set<String> linkedSet = new LinkedHashSet<>();

    // Maps are optimised for put and key lookup operation
    // hash map has 'random' order when looping
    // equals and hashCode methods of keys are very important in that case
    private Map<String, Person> hashMap = new HashMap<>();

    // Tree map has looping order over elements
    // related to implementation of compareTo() of keys
    // hashed implementation has better performance then that
    private Map<String, Person> treeMap = new TreeMap<>();

    // Linked map has looping order over elements
    // related to order of put operations (like queue or list)
    // similar performance like hashed implementation
    private Map<String, Person> linkedMap = new LinkedHashMap<>();

    // lets make usage of few persons
    private Person cody = new Person("Cody");
    private Person buster = new Person("Buster");
    private Person bella = new Person("Bella");

    @Test
    public void usageOfListAccumulatingObjects() {

        // typical usage:
        // if we want to store all persons we have
        arrayList.add(cody);
        arrayList.add(bella);

        // benefit of list - we can access element at any index
        assertThat(arrayList.get(0)).isSameAs(cody);
        assertThat(arrayList.get(1)).isSameAs(bella);

        // order of adding elements is preserved on the list
        for (Person element : arrayList) {
            System.out.println(element.name());
        }
    }

    @Test
    public void usageOfListEditingElementAtIndex() {
        arrayList.add(cody);

        // benefit of list - we can edit element at any index
        assertThat(arrayList.get(0)).isSameAs(cody);
        arrayList.set(0, bella);
        assertThat(arrayList.get(0))
                .isNotSameAs(cody)
                .isSameAs(bella);

        for (Person element : arrayList) {
            System.out.println(element.name());
        }
    }

    @Test
    public void usageOfListRemovingAtIndex() {
        arrayList.add(cody);
        arrayList.add(buster);
        arrayList.add(bella);

        // benefit of list - we can remove element at any index
        arrayList.remove(1); // buster is under index 1

        for (Person element : arrayList) {
            System.out.println(element.name());
        }

        // list reindex elements, to keep consecutive indexes
        assertThat(arrayList.get(0)).isSameAs(cody);
        assertThat(arrayList.get(1)).isSameAs(bella);
        assertThat(arrayList).doesNotContain(buster);

    }

    @Test
    public void usageOfSet() {

        // typical usage:
        // if we want to keep track of all names used already for persons
        // to prevent duplicates
        hashSet.add("Bella");
        hashSet.add("Cody");
        hashSet.add("Buster");

        if (hashSet.contains("Buster")) {
            System.out.println("Buster is already in set");
        }
        assertThat(hashSet).contains("Buster");

        if (! hashSet.contains("Max")) {
            System.out.println("Max is not in set");
        }
        assertThat(hashSet).doesNotContain("Max");

        // but in hashSet order of elements is unpredictable
        for (String element : hashSet) {
            System.out.println(element);
        }
    }

    @Test
    public void usageOfSetRemovingFromSet() {

        // we can add and remove from set
        hashSet.add("Cody");
        hashSet.add("Buster");
        hashSet.add("Bella");

        if (hashSet.contains("Buster")) {
            System.out.println("Buster is in set");
        }
        assertThat(hashSet).contains("Buster");

        hashSet.remove("Buster");

        if (! hashSet.contains("Buster")) {
            System.out.println("Buster is no more in set");
        }
        assertThat(hashSet).doesNotContain("Buster");

        for (String element : hashSet) {
            System.out.println(element);
        }
    }

    @Test
    public void usageOfTreeSet() {

        // TreeSet keeps elements ordered
        treeSet.add("Bella");
        treeSet.add("Cody");
        treeSet.add("Buster");


        for (String element : treeSet) {
            System.out.println(element);
        }

        // but in natural order of entries, not in order we added elements
        // in case of Strings - lexicographic order
        assertThat(treeSet)
                .containsExactly("Bella", "Buster", "Cody");
    }

    @Test
    public void usageOfMap() {

        // typical usage:
        // if we want find values based on keys
        hashMap.put("Cody", cody);
        hashMap.put("Buster", buster);
        hashMap.put("Bella", bella);

        // we can find our person by name
        Person person = hashMap.get("Buster");

        assertThat(person.name())
                .isEqualTo("Buster");
        assertThat(person).isSameAs(buster);
    }

    @Test
    public void usageOfMapIteratingOverMap() throws Exception {
        hashMap.put("Cody", cody);
        hashMap.put("Buster", buster);
        hashMap.put("Bella", bella);

        // we can iterate on keys
        for (String name : hashMap.keySet()) {
            System.out.println(name + " => " + hashMap.get(name));
        }

        for (Person person : hashMap.values()) {
            System.out.println("??? => " + person.name());
        }

        for (Map.Entry<String, Person> entry : hashMap.entrySet()) {
            System.out.println(entry.getKey() + " => " + entry.getValue());
        }
    }

}
