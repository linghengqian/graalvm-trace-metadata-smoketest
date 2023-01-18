
package com.github.benmanes.caffeine.cache;

import com.github.benmanes.caffeine.testing.CollectionSubject;
import com.google.common.collect.Sets;
import com.google.common.truth.FailureMetadata;

import java.util.Iterator;

import static com.google.common.truth.Truth.assertAbout;


final class LinkedDequeSubject extends CollectionSubject {
    private final LinkedDeque<Object> actual;

    @SuppressWarnings("unchecked")
    private LinkedDequeSubject(FailureMetadata metadata, LinkedDeque<?> subject) {
        super(metadata, subject);
        this.actual = (LinkedDeque<Object>) subject;
    }

    public static Factory<LinkedDequeSubject, LinkedDeque<?>> deque() {
        return LinkedDequeSubject::new;
    }

    public static LinkedDequeSubject assertThat(LinkedDeque<?> actual) {
        return assertAbout(deque()).that(actual);
    }

    public void isValid() {
        if (actual.isEmpty()) {
            isExhaustivelyEmpty();
        }
        checkIterator(actual.iterator());
        checkIterator(actual.descendingIterator());
    }

    private void checkIterator(Iterator<?> iterator) {
        var seen = Sets.newIdentityHashSet();
        while (iterator.hasNext()) {
            var element = iterator.next();
            checkElement(element);
            check("loop").withMessage("Loop detected: %s in %s", element, seen)
                    .that(seen.add(element)).isTrue();
        }
        hasSize(seen.size());
    }

    private void checkElement(Object element) {
        var first = actual.peekFirst();
        var last = actual.peekLast();
        if (element == first) {
            check("getPrevious(e)").that(actual.getPrevious(element)).isNull();
        }
        if (element == last) {
            check("getNext(e)").that(actual.getNext(element)).isNull();
        }
        if ((element != first) && (element != last)) {
            check("getPrevious(e)").that(actual.getPrevious(element)).isNotNull();
            check("getNext(e)").that(actual.getNext(element)).isNotNull();
        }
    }
}
