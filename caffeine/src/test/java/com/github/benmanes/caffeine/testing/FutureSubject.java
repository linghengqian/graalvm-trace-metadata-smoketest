
package com.github.benmanes.caffeine.testing;

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import com.google.common.truth.ThrowableSubject;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.google.common.truth.Truth.assertAbout;


public final class FutureSubject extends Subject {
  private final CompletableFuture<?> actual;

  private FutureSubject(FailureMetadata metadata, CompletableFuture<?> subject) {
    super(metadata, subject);
    this.actual = subject;
  }

  public static Factory<FutureSubject, CompletableFuture<?>> future() {
    return FutureSubject::new;
  }

  public static FutureSubject assertThat(CompletableFuture<?> actual) {
    return assertAbout(future()).that(actual);
  }

  public void isDone() {
    if (!actual.isDone()) {
      failWithActual("expected to be done", actual);
    }
  }

  public void isNotDone() {
    if (actual.isDone()) {
      failWithActual("expected to not be done", actual);
    }
  }

  public void hasCompletedExceptionally() {
    if (!actual.isCompletedExceptionally()) {
      failWithActual("expected to be completed exceptionally", actual.join());
    }
  }

  public void succeedsWith(int value) {
    var result = actual.join();
    if (result instanceof Int) {
      check("future").that(result).isEqualTo(Int.valueOf(value));
    } else {
      check("future").that(result).isEqualTo(value);
    }
  }

  public void succeedsWith(Object value) {
    check("future").that(actual.join()).isEqualTo(value);
  }

  public void succeedsWithNull() {
    check("future").that(actual.join()).isNull();
  }

  public ThrowableSubject failsWith(Class<? extends RuntimeException> clazz) {
    try {
      failWithActual("join", actual.join());
      throw new AssertionError();
    } catch (CompletionException | CancellationException e) {
      check("future").that(e).isInstanceOf(clazz);
      return check("future").that(e);
    }
  }
}
