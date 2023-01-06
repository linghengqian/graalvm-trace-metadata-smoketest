package com.lingh.utils;

import org.apache.curator.RetryLoop;
import org.apache.curator.RetryPolicy;
import org.apache.curator.RetrySleeper;
import org.apache.curator.drivers.EventTrace;
import org.apache.curator.drivers.TracerDriver;
import org.apache.curator.utils.DebugUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("unused")
public class RetryLoopImpl extends RetryLoop {
    private boolean isDone = false;
    private int retryCount = 0;

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final long startTimeMs = System.currentTimeMillis();
    private final RetryPolicy retryPolicy;
    private final AtomicReference<TracerDriver> tracer;

    private static final RetrySleeper sleeper = (time, unit) -> unit.sleep(time);

    public RetryLoopImpl(RetryPolicy retryPolicy, AtomicReference<TracerDriver> tracer) {
        this.retryPolicy = retryPolicy;
        this.tracer = tracer;
    }

    static RetrySleeper getRetrySleeper() {
        return sleeper;
    }


    @Override
    public boolean shouldContinue() {
        return !isDone;
    }

    @Override
    public void markComplete() {
        isDone = true;
    }

    @Override
    public void takeException(Exception exception) throws Exception {
        boolean rethrow = true;
        if (retryPolicy.allowRetry(exception)) {
            if (!Boolean.getBoolean(DebugUtils.PROPERTY_DONT_LOG_CONNECTION_ISSUES)) {
                log.debug("Retry-able exception received", exception);
            }
            if (retryPolicy.allowRetry(retryCount++, System.currentTimeMillis() - startTimeMs, sleeper)) {
                new EventTrace("retries-allowed", tracer.get()).commit();
                if (!Boolean.getBoolean(DebugUtils.PROPERTY_DONT_LOG_CONNECTION_ISSUES)) {
                    log.debug("Retrying operation");
                }
                rethrow = false;
            } else {
                new EventTrace("retries-disallowed", tracer.get()).commit();
                if (!Boolean.getBoolean(DebugUtils.PROPERTY_DONT_LOG_CONNECTION_ISSUES)) {
                    log.debug("Retry policy not allowing retry");
                }
            }
        }
        if (rethrow) {
            throw exception;
        }
    }
}
