package dev.bottega.streams.collections;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class ImmutableCollections {

    @Test
    public void unmodifiableCollections() throws Exception {

        // interfaces of all collections in java allow to modify entries
        List<String> list = new ArrayList<>();
        list.add("some");
        list.add("some other");

        // if we want prevent further modifications of that list
        // we can wrap it - in unmodifiable decorator

        List<String> unmodifiable = Collections.unmodifiableList(list);

        // now we can get entries or iterate
        for (String string : unmodifiable) {
            System.out.println(string);
        }
        assertThat(unmodifiable)
                .containsExactly("some", "some other");

        // but any modification will throw error UnsupportedOperationException

        Assertions.assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> unmodifiable.add("another"));

        // similar with
        // Collections.unmodifiableCollection(anyCollection);
        // Collections.unmodifiableMap(map);
        // Collections.unmodifiableSet(set);
    }

    @Test
    public void manyConvenienceMethodsReturnsUnmodifiableCollection() throws Exception {

        // many convenience methods returns unmodifiable collection

        // try it by uncommenting add call
        Collections.emptyList()
        //        .add("will brake")
        ;
        Collections.singletonList("single entry")
        //        .add("will brake")
        //        .set(0, "will brake")
        ;
        Arrays.asList("entry one", "entry two")
        //        .add("will break")
        ;

    }

    @Test
    public void notTrulyImmutableCollections() throws Exception {

        // collection will be truly immutable
        // if we cannot edit entries in collection
        // and each entry is immutable

        // two instances of lists but equal
        List<Date> dateOnList1 = Collections.singletonList(new Date(2016 - 1900, 6 - 1, 15));
        List<Date> dateOnList2 = Collections.singletonList(new Date(2016 - 1900, 6 - 1, 15));
        assertThat(dateOnList1).isEqualTo(dateOnList2);

        // old mutable java.util.Date representation
        Date date20160615 = dateOnList2.get(0);

        // change value of year
        date20160615.setYear(2020 - 1900);
        // same date instance but internal state is changed
        assertThat(date20160615).isSameAs(dateOnList2.get(0));

        // unfortunately lists are no longer equals
        // even if lists are unmodifiable
        assertThat(dateOnList1).isNotEqualTo(dateOnList2);

        System.out.println(dateOnList1);
        System.out.println(dateOnList2);
    }

    @Test
    public void allUnmodifiableCollections() throws Exception {
        Collection<String> sourceCollection = new ArrayList<>();
        Set<String> sourceSet = new HashSet<>();
        List<String> sourceList = new ArrayList<>();
        Map<String, String> sourceMap = new HashMap<>();
        SortedSet<String> sourceSortedSet = new TreeSet<>();
        SortedMap<String,String> sourceSortedMap = new TreeMap<>();

        Collection<String> collection = Collections.unmodifiableCollection(sourceCollection);
        Set<String> set = Collections.unmodifiableSet(sourceSet);
        List<String> list = Collections.unmodifiableList(sourceList);
        Map<String, String> map = Collections.unmodifiableMap(sourceMap);
        SortedSet<String> sortedSet = Collections.unmodifiableSortedSet(sourceSortedSet);
        SortedMap<String, String> sortedMap = Collections.unmodifiableSortedMap(sourceSortedMap);

        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> collection.add("Nop !"));
        assertThat(collection).isEmpty();

        sourceCollection.add("Yep !");
        assertThat(sourceCollection).containsExactly("Yep !");

        assertThat(collection).containsExactly("Yep !");
    }
}
