
package com.github.benmanes.caffeine.testing;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Ordered;

import java.util.Map;

import static com.github.benmanes.caffeine.testing.CollectionSubject.collection;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.truth.Truth.assertAbout;

public class MapSubject extends com.google.common.truth.MapSubject {
    private final Map<?, ?> actual;

    public MapSubject(FailureMetadata metadata, Map<?, ?> subject) {
        super(metadata, subject);
        this.actual = subject;
    }

    public static Factory<MapSubject, Map<?, ?>> map() {
        return MapSubject::new;
    }

    public static <K, V> MapSubject assertThat(Map<K, V> actual) {
        return assertAbout(map()).that(actual);
    }


    public final void hasSize(long expectedSize) {
        super.hasSize(Math.toIntExact(expectedSize));
    }


    public void hasSizeLessThan(long other) {
        checkArgument(other >= 0, "expectedSize (%s) must be >= 0", other);
        check("size()").that(actual.size()).isLessThan(Math.toIntExact(other));
    }


    public void hasSizeIn(Range<Integer> range) {
        check("size()").that(actual.size()).isIn(range);
    }


    public Ordered containsExactlyKeys(Iterable<?> keys) {
        return check("containsKeys").that(actual.keySet())
                .containsExactlyElementsIn(ImmutableSet.copyOf(keys));
    }


    public void containsValue(Object value) {
        check("containsValue").that(actual.values()).contains(value);
    }


    public void doesNotContainValue(Object value) {
        check("containsValue").that(actual.values()).doesNotContain(value);
    }

    public void isExhaustivelyEmpty() {
        isEqualTo(Map.of());
        hasSize(0);
        isEmpty();
        check("isEmpty()").that(actual.isEmpty()).isTrue();
        check("toString()").that(actual.toString()).isEqualTo(Map.of().toString());
        check("hashCode()").that(actual.hashCode()).isEqualTo(Map.of().hashCode());
        check("keySet()").about(collection()).that(actual.keySet()).isExhaustivelyEmpty();
        check("values()").about(collection()).that(actual.values()).isExhaustivelyEmpty();
        check("entrySet()").about(collection()).that(actual.entrySet()).isExhaustivelyEmpty();
    }
}
