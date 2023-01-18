
package com.github.benmanes.caffeine.cache;

import com.google.common.testing.NullPointerTester;
import com.google.common.util.concurrent.Futures;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.benmanes.caffeine.testing.Awaits.await;
import static com.github.benmanes.caffeine.testing.ConcurrentTestHarness.executor;
import static com.github.benmanes.caffeine.testing.ConcurrentTestHarness.scheduledExecutor;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.util.concurrent.testing.TestingExecutors.sameThreadScheduledExecutor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


@SuppressWarnings("FutureReturnValueIgnored")
public final class SchedulerTest {
    private final NullPointerTester npeTester = new NullPointerTester();

    @Test(dataProvider = "schedulers")
    public void scheduler_null(Scheduler scheduler) {
        npeTester.testAllPublicInstanceMethods(scheduler);
    }

    @Test(dataProvider = "runnableSchedulers")
    public void scheduler_exception(Scheduler scheduler) {
        var executed = new AtomicBoolean();
        Executor executor = task -> {
            executed.set(true);
            throw new IllegalStateException();
        };
        scheduler.schedule(executor, () -> {
        }, 1L, TimeUnit.NANOSECONDS);
        await().untilTrue(executed);
    }

    @Test(dataProvider = "runnableSchedulers")
    public void scheduler(Scheduler scheduler) {
        var executed = new AtomicBoolean();
        Runnable task = () -> executed.set(true);
        scheduler.schedule(executor, task, 1L, TimeUnit.NANOSECONDS);
        await().untilTrue(executed);
    }

    @Test
    public void disabledScheduler() {
        var future = Scheduler.disabledScheduler().schedule(Runnable::run, () -> {
        }, 1, TimeUnit.MINUTES);
        assertThat(future).isSameInstanceAs(DisabledFuture.INSTANCE);
    }

    @Test
    public void disabledFuture() {
        assertThat(DisabledFuture.INSTANCE.get(0, TimeUnit.SECONDS)).isNull();
        assertThat(DisabledFuture.INSTANCE.isCancelled()).isFalse();
        assertThat(DisabledFuture.INSTANCE.cancel(false)).isFalse();
        assertThat(DisabledFuture.INSTANCE.cancel(true)).isFalse();
        assertThat(DisabledFuture.INSTANCE.isDone()).isTrue();
        assertThat(DisabledFuture.INSTANCE.get()).isNull();
    }

    @Test
    public void disabledFuture_null() {
        npeTester.testAllPublicInstanceMethods(DisabledFuture.INSTANCE);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void guardedScheduler_null() {
        Scheduler.guardedScheduler(null);
    }

    @Test
    public void guardedScheduler_nullFuture() {
        var scheduledExecutor = Mockito.mock(ScheduledExecutorService.class);
        var scheduler = Scheduler.forScheduledExecutorService(scheduledExecutor);
        var executor = Mockito.mock(Executor.class);
        Runnable command = () -> {
        };
        var future = Scheduler.guardedScheduler(scheduler).schedule(executor, command, 1L, TimeUnit.MINUTES);
        verify(scheduledExecutor).schedule(any(Runnable.class), eq(1L), eq(TimeUnit.MINUTES));
        assertThat(future).isSameInstanceAs(DisabledFuture.INSTANCE);
    }

    @Test
    public void guardedScheduler() {
        var future = Scheduler.guardedScheduler((r, e, d, u) -> Futures.immediateVoidFuture()).schedule(Runnable::run, () -> {
        }, 1, TimeUnit.MINUTES);
        assertThat(future).isSameInstanceAs(Futures.immediateVoidFuture());
    }

    @Test
    public void guardedScheduler_exception() {
        var future = Scheduler.guardedScheduler((r, e, d, u) -> {
            throw new RuntimeException();
        }).schedule(Runnable::run, () -> {
        }, 1, TimeUnit.MINUTES);
        assertThat(future).isSameInstanceAs(DisabledFuture.INSTANCE);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void scheduledExecutorService_null() {
        Scheduler.forScheduledExecutorService(null);
    }

    @Test
    public void scheduledExecutorService_schedule() {
        var scheduledExecutor = Mockito.mock(ScheduledExecutorService.class);
        var task = ArgumentCaptor.forClass(Runnable.class);
        var executor = Mockito.mock(Executor.class);
        Runnable command = () -> {
        };
        var scheduler = Scheduler.forScheduledExecutorService(scheduledExecutor);
        var future = scheduler.schedule(executor, command, 1L, TimeUnit.MINUTES);
        assertThat(future).isNotSameInstanceAs(DisabledFuture.INSTANCE);
        verify(scheduledExecutor).isShutdown();
        verify(scheduledExecutor).schedule(task.capture(), eq(1L), eq(TimeUnit.MINUTES));
        verifyNoMoreInteractions(scheduledExecutor);
        task.getValue().run();
        verify(executor).execute(command);
        verifyNoMoreInteractions(executor);
    }

    @Test
    public void scheduledExecutorService_shutdown() {
        var scheduledExecutor = Mockito.mock(ScheduledExecutorService.class);
        var executor = Mockito.mock(Executor.class);
        when(scheduledExecutor.isShutdown()).thenReturn(true);
        var scheduler = Scheduler.forScheduledExecutorService(scheduledExecutor);
        var future = scheduler.schedule(executor, () -> {
        }, 1L, TimeUnit.MINUTES);
        assertThat(future).isSameInstanceAs(DisabledFuture.INSTANCE);
        verify(scheduledExecutor).isShutdown();
        verifyNoMoreInteractions(scheduledExecutor);
        verifyNoInteractions(executor);
    }

    @DataProvider(name = "schedulers")
    public Iterator<Scheduler> providesSchedulers() {
        var schedulers = Set.of(
                Scheduler.forScheduledExecutorService(sameThreadScheduledExecutor()),
                Scheduler.forScheduledExecutorService(scheduledExecutor),
                Scheduler.disabledScheduler(),
                Scheduler.systemScheduler());
        return schedulers.iterator();
    }

    @DataProvider(name = "runnableSchedulers")
    public Iterator<Scheduler> providesRunnableSchedulers() {
        var schedulers = Set.of(
                Scheduler.forScheduledExecutorService(sameThreadScheduledExecutor()),
                Scheduler.forScheduledExecutorService(scheduledExecutor),
                Scheduler.systemScheduler());
        return schedulers.stream()
                .filter(scheduler -> scheduler != Scheduler.disabledScheduler())
                .iterator();
    }
}
