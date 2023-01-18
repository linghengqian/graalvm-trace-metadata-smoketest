package com.github.benmanes.caffeine.testing;

import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;

import java.time.Duration;

public final class Awaits {
    private static final Duration ONE_MILLISECOND = Duration.ofMillis(1);

    private Awaits() {
    }

    public static ConditionFactory await() {
        return Awaitility.with()
                .pollDelay(ONE_MILLISECOND)
                .pollInterval(ONE_MILLISECOND)
                .pollExecutorService(ConcurrentTestHarness.executor);
    }
}
