package com.lingh.impl;

import io.etcd.jetcd.support.Errors;
import io.grpc.Status;
import io.grpc.StatusException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// `@org.junit.jupiter.api.Timeout(value = 30)` can't be used in the nativeTest GraalVM CE 22.3
class UtilTest {

    @Test
    public void testAuthStoreExpired() {
        Status authExpiredStatus = Status.INVALID_ARGUMENT.withDescription(Errors.ERROR_AUTH_STORE_OLD);
        Status status = Status.fromThrowable(new StatusException(authExpiredStatus));
        assertThat(Errors.isAuthStoreExpired(status)).isTrue();
    }

    @Test
    public void testAuthErrorIsNotRetryable() {
        Status authErrorStatus = Status.UNAUTHENTICATED.withDescription("etcdserver: invalid auth token");
        Status status = Status.fromThrowable(new StatusException(authErrorStatus));
        assertThat(Errors.isRetryable(status)).isTrue();
    }

    @Test
    public void testUnavailableErrorIsRetryable() {
        Status status = Status.fromThrowable(new StatusException(Status.UNAVAILABLE));
        assertThat(Errors.isRetryable(status)).isTrue();
    }
}
