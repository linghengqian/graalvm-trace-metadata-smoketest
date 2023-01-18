
package com.github.benmanes.caffeine.testing;

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;

import static com.google.common.truth.Truth.assertAbout;

public final class IntSubject extends Subject {

    private IntSubject(FailureMetadata metadata, Int subject) {
        super(metadata, subject);
    }

    public static Factory<IntSubject, Int> integer() {
        return IntSubject::new;
    }

    public static IntSubject assertThat(Int actual) {
        return assertAbout(integer()).that(actual);
    }

    public void isEqualTo(int value) {
        isEqualTo(Int.valueOf(value));
    }
}
