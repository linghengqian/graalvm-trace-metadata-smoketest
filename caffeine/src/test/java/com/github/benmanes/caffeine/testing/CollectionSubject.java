
package com.github.benmanes.caffeine.testing;

import com.google.common.truth.FailureMetadata;

import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.truth.Truth.assertAbout;

public class CollectionSubject extends com.google.common.truth.IterableSubject {
    private final Collection<?> actual;

    public CollectionSubject(FailureMetadata metadata, Collection<?> subject) {
        super(metadata, subject);
        this.actual = subject;
    }

    public static Factory<CollectionSubject, Collection<?>> collection() {
        return CollectionSubject::new;
    }

    public static <E> CollectionSubject assertThat(Collection<E> actual) {
        return assertAbout(collection()).that(actual);
    }

    public final void hasSize(long expectedSize) {
        hasSize(Math.toIntExact(expectedSize));
    }

    public void hasSizeLessThan(long other) {
        checkArgument(other >= 0, "expectedSize (%s) must be >= 0", other);
        check("size()").that(actual.size()).isLessThan(Math.toIntExact(other));
    }

    public void isExhaustivelyEmpty() {
        checkIterable();
        checkCollection();
        if (actual instanceof Set<?>) {
            checkSet((Set<?>) actual);
        }
        if (actual instanceof List<?>) {
            checkList((List<?>) actual);
        }
        if (actual instanceof Queue<?>) {
            checkQueue((Queue<?>) actual);
        }
        if (actual instanceof Deque<?>) {
            checkDeque((Deque<?>) actual);
        }
    }

    private void checkIterable() {
        check("iterator().hasNext()").that(actual.iterator().hasNext()).isFalse();
    }

    private void checkCollection() {
        check("size()").that(actual).hasSize(0);
        check("isEmpty()").that(actual).isEmpty();
        check("toArray()").that(actual.toArray()).isEmpty();
        check("toArray(E[])").that(actual.toArray(new Object[0])).isEmpty();
        check("toArray(IntFunction)").that(actual.toArray(Object[]::new)).isEmpty();
    }

    private void checkSet(Set<?> set) {
        check("actual.equals(empty)").that(set).isEqualTo(Set.of());
        check("empty.equals(actual)").that(Set.of()).isEqualTo(set);
        check("hashCode()").that(set.hashCode()).isEqualTo(Set.of().hashCode());
    }

    private void checkList(List<?> list) {
        check("actual.equals(empty)").that(list).isEqualTo(List.of());
        check("empty.equals(actual)").that(List.of()).isEqualTo(list);
        check("hashCode()").that(list.hashCode()).isEqualTo(List.of().hashCode());
    }

    private void checkQueue(Queue<?> queue) {
        check("peek()").that(queue.peek()).isNull();
        try {
            failWithActual("remove()", queue.remove());
        } catch (NoSuchElementException expected) {
        }
        try {
            failWithActual("element()", queue.element());
        } catch (NoSuchElementException expected) {
        }
    }

    private void checkDeque(Deque<?> deque) {
        check("peekFirst()").that(deque.peekFirst()).isNull();
        check("peekLast()").that(deque.peekLast()).isNull();
        check("descendingIterator().hasNext()").that(deque.descendingIterator().hasNext()).isFalse();
    }
}
