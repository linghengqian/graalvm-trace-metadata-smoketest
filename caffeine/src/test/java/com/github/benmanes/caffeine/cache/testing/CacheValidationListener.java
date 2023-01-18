
package com.github.benmanes.caffeine.cache.testing;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Policy.Eviction;
import com.github.benmanes.caffeine.cache.Policy.FixedExpiration;
import com.github.benmanes.caffeine.cache.Policy.VarExpiration;
import com.github.benmanes.caffeine.cache.Reset;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.CacheExecutor;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.CacheExpiry;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.CacheScheduler;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.ExecutorFailure;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.joor.Reflect;
import org.mockito.Mockito;
import org.testng.*;
import org.testng.internal.TestResult;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.benmanes.caffeine.cache.testing.AsyncCacheSubject.assertThat;
import static com.github.benmanes.caffeine.cache.testing.CacheContextSubject.assertThat;
import static com.github.benmanes.caffeine.cache.testing.CacheSubject.assertThat;
import static com.github.benmanes.caffeine.testing.Awaits.await;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.testng.ITestResult.FAILURE;
import static uk.org.lidalia.slf4jext.ConventionalLevelHierarchy.TRACE_LEVELS;


public final class CacheValidationListener implements ISuiteListener, IInvokedMethodListener {
    private static final Cache<Object, String> simpleNames = Caffeine.newBuilder().build();
    private static final ITestContext testngContext = Mockito.mock(ITestContext.class);
    private static final AtomicBoolean detailedParams = new AtomicBoolean(false);
    private static final Object[] EMPTY_PARAMS = {};

    private final List<Collection<?>> resultQueues = new CopyOnWriteArrayList<>();
    private final AtomicBoolean beforeCleanup = new AtomicBoolean();

    @Override
    public void onStart(ISuite suite) {
        if (suite instanceof SuiteRunner) {
            var invokedMethods = Reflect.on(suite).fields().get("invokedMethods");
            if ((invokedMethods != null) && (invokedMethods.get() instanceof Collection)) {
                resultQueues.add(invokedMethods.get());
            }
        }
    }

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        TestLoggerFactory.getAllTestLoggers().values().forEach(logger -> logger.setEnabledLevels(TRACE_LEVELS));
        TestLoggerFactory.clear();
        if (beforeCleanup.get() || !beforeCleanup.compareAndSet(false, true)) {
            return;
        }
        if (testResult.getTestContext() instanceof TestRunner runner) {
            runner.getTestListeners().stream()
                    .filter(listener -> listener.getClass() == TestListenerAdapter.class)
                    .flatMap(listener -> Reflect.on(listener).fields().values().stream())
                    .filter(field -> field.get() instanceof Collection)
                    .forEach(field -> resultQueues.add(field.get()));
            resultQueues.add(runner.getFailedButWithinSuccessPercentageTests().getAllResults());
            resultQueues.add(runner.getSkippedTests().getAllResults());
            resultQueues.add(runner.getPassedTests().getAllResults());
            resultQueues.add(runner.getFailedTests().getAllResults());
            var invokedMethods = Reflect.on(runner).fields().get("m_invokedMethods");
            if ((invokedMethods != null) && (invokedMethods.get() instanceof Collection)) {
                resultQueues.add(invokedMethods.get());
            }
        }
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        try {
            if (testResult.isSuccess()) {
                validate(testResult);
            } else if (!detailedParams.get()) {
                detailedParams.set(true);
            }
        } catch (Throwable caught) {
            testResult.setStatus(FAILURE);
            testResult.setThrowable(new AssertionError(getTestName(method), caught));
        } finally {
            cleanUp(testResult);
        }
    }

    private void validate(ITestResult testResult) {
        CacheContext context = Arrays.stream(testResult.getParameters())
                .filter(param -> param instanceof CacheContext)
                .map(param -> (CacheContext) param)
                .findFirst().orElse(null);
        if (context != null) {
            awaitExecutor(context);

            checkCache(context);
            checkNoStats(testResult, context);
            checkExecutor(testResult, context);
            checkNoEvictions(testResult, context);
        }
        checkLogger(testResult);
    }

    private void awaitExecutor(CacheContext context) {
        if (context.executor() != null) {
            context.executor().resume();

            if ((context.cacheExecutor != CacheExecutor.DIRECT)
                    && (context.cacheExecutor != CacheExecutor.DISCARDING)
                    && (context.executor().submitted() != context.executor().completed())) {
                await().pollInSameThread().until(() ->
                        context.executor().submitted() == context.executor().completed());
            }
        }
    }

    private static String getTestName(IInvokedMethod method) {
        return StringUtils.substringAfterLast(method.getTestMethod().getTestClass().getName(), ".")
                + "#" + method.getTestMethod().getConstructorOrMethod().getName();
    }

    private static void checkExecutor(ITestResult testResult, CacheContext context) {
        var testMethod = testResult.getMethod().getConstructorOrMethod().getMethod();
        var cacheSpec = testMethod.getAnnotation(CacheSpec.class);
        if ((cacheSpec == null) || (context.executor() == null)) {
            return;
        }
        if (cacheSpec.executorFailure() == ExecutorFailure.EXPECTED) {
            assertThat(context.executor().failed()).isGreaterThan(0);
        } else if (cacheSpec.executorFailure() == ExecutorFailure.DISALLOWED) {
            assertThat(context.executor().failed()).isEqualTo(0);
        }
    }

    private void checkCache(CacheContext context) {
        if (context.cache != null) {
            assertThat(context.cache).isValid();
        } else if (context.asyncCache != null) {
            assertThat(context.asyncCache).isValid();
        } else {
            Assert.fail("Test requires that the CacheContext holds the cache under test");
        }
    }

    private static void checkNoStats(ITestResult testResult, CacheContext context) {
        var testMethod = testResult.getMethod().getConstructorOrMethod().getMethod();
        boolean checkNoStats = testMethod.isAnnotationPresent(CheckNoStats.class)
                || testResult.getTestClass().getRealClass().isAnnotationPresent(CheckNoStats.class);
        if (!checkNoStats) {
            return;
        }

        assertThat(context).stats().hits(0).misses(0).success(0).failures(0);
    }

    private static void checkNoEvictions(ITestResult testResult, CacheContext context) {
        var testMethod = testResult.getMethod().getConstructorOrMethod().getMethod();
        boolean checkNoEvictions = testMethod.isAnnotationPresent(CheckNoEvictions.class)
                || testResult.getTestClass().getRealClass().isAnnotationPresent(CheckNoEvictions.class);
        if (!checkNoEvictions) {
            return;
        }
        assertThat(context).removalNotifications().hasNoEvictions();
        assertThat(context).evictionNotifications().isEmpty();
    }

    private static void checkLogger(ITestResult testResult) {
        var testMethod = testResult.getMethod().getConstructorOrMethod().getMethod();
        var checkMaxLogLevel = Optional.ofNullable(testMethod.getAnnotation(CheckMaxLogLevel.class))
                .orElse(testResult.getTestClass().getRealClass().getAnnotation(CheckMaxLogLevel.class));
        if (checkMaxLogLevel != null) {
            var events = TestLoggerFactory.getLoggingEvents().stream()
                    .filter(event -> event.getLevel().ordinal() > checkMaxLogLevel.value().ordinal())
                    .collect(toImmutableList());
            assertWithMessage("maxLevel=%s", checkMaxLogLevel.value()).that(events).isEmpty();
        }
    }

    private void cleanUp(ITestResult testResult) {
        resultQueues.forEach(Collection::clear);
        TestLoggerFactory.clear();
        resetMocks(testResult);
        resetCache(testResult);
        boolean briefParams = !detailedParams.get();
        if (testResult.isSuccess() && briefParams) {
            clearTestResults(testResult);
        }
        stringifyParams(testResult, briefParams);
        dedupTestName(testResult, briefParams);
        CacheContext.interner().clear();
    }

    private void dedupTestName(ITestResult testResult, boolean briefParams) {
        if ((testResult.getName() != null) && briefParams) {
            testResult.setTestName(simpleNames.get(testResult.getName(), Object::toString));
        }
    }

    @SuppressWarnings("unchecked")
    private void resetMocks(ITestResult testResult) {
        for (Object param : testResult.getParameters()) {
            if (param instanceof CacheContext context) {
                if (context.expiryType() == CacheExpiry.MOCKITO) {
                    Mockito.clearInvocations(context.expiry());
                }
                if (context.cacheScheduler == CacheScheduler.MOCKITO) {
                    Mockito.clearInvocations(context.scheduler());
                }
            }
        }
    }

    private void resetCache(ITestResult testResult) {
        for (Object param : testResult.getParameters()) {
            if (param instanceof CacheContext context) {
                if (context.isCaffeine()) {
                    Reset.destroy(context.cache());
                }
            }
        }
    }

    private void clearTestResults(ITestResult testResult) {
        var result = (TestResult) testResult;
        result.setParameters(EMPTY_PARAMS);
        result.setContext(testngContext);
    }

    private void stringifyParams(ITestResult testResult, boolean briefParams) {
        Object[] params = testResult.getParameters();
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            if ((param instanceof AsyncCache<?, ?>) || (param instanceof Cache<?, ?>)
                    || (param instanceof Map<?, ?>) || (param instanceof Eviction<?, ?>)
                    || (param instanceof FixedExpiration<?, ?>) || (param instanceof VarExpiration<?, ?>)
                    || ((param instanceof CacheContext) && briefParams)) {
                params[i] = simpleNames.get(param.getClass(), key -> ((Class<?>) key).getSimpleName());
            } else if (param instanceof CacheContext) {
                params[i] = simpleNames.get(param.toString(), Object::toString);
            } else {
                params[i] = Objects.toString(param);
            }
        }
    }
}
