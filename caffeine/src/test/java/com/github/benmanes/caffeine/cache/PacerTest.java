
package com.github.benmanes.caffeine.cache;

import com.google.common.primitives.Ints;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.*;

@Test(singleThreaded = true)
public final class PacerTest {
    private static final long ONE_MINUTE_IN_NANOS = TimeUnit.MINUTES.toNanos(1);
    private static final Random random = new Random();
    private static final long NOW = random.nextLong();

    @Mock
    Scheduler scheduler;
    @Mock
    Executor executor;
    @Mock
    Runnable command;
    @Mock
    Future<?> future;

    Pacer pacer;

    @BeforeMethod
    public void beforeMethod() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        pacer = new Pacer(scheduler);
    }

    @Test
    public void schedule_initialize() {
        long delay = random.nextInt(Ints.saturatedCast(Pacer.TOLERANCE));
        doReturn(DisabledFuture.INSTANCE)
                .when(scheduler).schedule(executor, command, Pacer.TOLERANCE, TimeUnit.NANOSECONDS);
        pacer.schedule(executor, command, NOW, delay);

        assertThat(pacer.future).isSameInstanceAs(DisabledFuture.INSTANCE);
        assertThat(pacer.nextFireTime).isEqualTo(NOW + Pacer.TOLERANCE);
    }

    @Test
    public void schedule_initialize_recurse() {
        long delay = random.nextInt(Ints.saturatedCast(Pacer.TOLERANCE));
        doAnswer(invocation -> {
            assertThat(pacer.future).isNull();
            assertThat(pacer.nextFireTime).isNotEqualTo(0);
            pacer.schedule(executor, command, NOW, delay);
            return DisabledFuture.INSTANCE;
        }).when(scheduler).schedule(executor, command, Pacer.TOLERANCE, TimeUnit.NANOSECONDS);

        pacer.schedule(executor, command, NOW, delay);
        assertThat(pacer.future).isSameInstanceAs(DisabledFuture.INSTANCE);
        assertThat(pacer.nextFireTime).isEqualTo(NOW + Pacer.TOLERANCE);
    }

    @Test
    public void schedule_cancel_schedule() {
        long fireTime = NOW + Pacer.TOLERANCE;
        long delay = random.nextInt(Ints.saturatedCast(Pacer.TOLERANCE));
        doReturn(future)
                .when(scheduler).schedule(executor, command, Pacer.TOLERANCE, TimeUnit.NANOSECONDS);

        pacer.schedule(executor, command, NOW, delay);
        assertThat(pacer.nextFireTime).isEqualTo(fireTime);
        assertThat(pacer.future).isSameInstanceAs(future);

        pacer.cancel();
        verify(future).cancel(false);
        assertThat(pacer.nextFireTime).isEqualTo(0);
        assertThat(pacer.future).isNull();

        pacer.schedule(executor, command, NOW, delay);
        assertThat(pacer.nextFireTime).isEqualTo(fireTime);
        assertThat(pacer.future).isSameInstanceAs(future);
    }

    @Test
    public void scheduled_afterNextFireTime_skip() {
        pacer.nextFireTime = NOW + ONE_MINUTE_IN_NANOS;
        pacer.future = future;

        long expectedNextFireTime = pacer.nextFireTime;
        pacer.schedule(executor, command, NOW, ONE_MINUTE_IN_NANOS);

        assertThat(pacer.future).isSameInstanceAs(future);
        assertThat(pacer.nextFireTime).isEqualTo(expectedNextFireTime);
        verifyNoInteractions(scheduler, executor, command, future);
    }

    @Test
    public void schedule_beforeNextFireTime_skip() {
        pacer.nextFireTime = NOW + ONE_MINUTE_IN_NANOS;
        pacer.future = future;

        long expectedNextFireTime = pacer.nextFireTime;
        long delay = ONE_MINUTE_IN_NANOS
                - Math.max(1, random.nextInt(Ints.saturatedCast(Pacer.TOLERANCE)));
        pacer.schedule(executor, command, NOW, delay);

        assertThat(pacer.future).isSameInstanceAs(future);
        assertThat(pacer.nextFireTime).isEqualTo(expectedNextFireTime);
        verifyNoInteractions(scheduler, executor, command, future);
    }

    @Test
    public void schedule_beforeNextFireTime_minimumDelay() {
        pacer.nextFireTime = NOW + ONE_MINUTE_IN_NANOS;
        pacer.future = future;

        long delay = random.nextInt(Ints.saturatedCast(Pacer.TOLERANCE));
        doReturn(DisabledFuture.INSTANCE)
                .when(scheduler).schedule(executor, command, Pacer.TOLERANCE, TimeUnit.NANOSECONDS);
        pacer.schedule(executor, command, NOW, delay);

        assertThat(pacer.future).isSameInstanceAs(DisabledFuture.INSTANCE);
        assertThat(pacer.nextFireTime).isEqualTo(NOW + Pacer.TOLERANCE);

        verify(future).cancel(false);
        verify(scheduler).schedule(executor, command, Pacer.TOLERANCE, TimeUnit.NANOSECONDS);

        verifyNoInteractions(executor, command);
        verifyNoMoreInteractions(scheduler, future);
    }

    @Test
    public void schedule_beforeNextFireTime_customDelay() {
        pacer.nextFireTime = NOW + ONE_MINUTE_IN_NANOS;
        pacer.future = future;

        long delay = (Pacer.TOLERANCE + Math.max(1, random.nextInt()));
        doReturn(DisabledFuture.INSTANCE)
                .when(scheduler).schedule(executor, command, delay, TimeUnit.NANOSECONDS);
        pacer.schedule(executor, command, NOW, delay);

        assertThat(pacer.future).isSameInstanceAs(DisabledFuture.INSTANCE);
        assertThat(pacer.nextFireTime).isEqualTo(NOW + delay);

        verify(future).cancel(false);
        verify(scheduler).schedule(executor, command, delay, TimeUnit.NANOSECONDS);

        verifyNoInteractions(executor, command);
        verifyNoMoreInteractions(scheduler, future);
    }

    @Test
    public void cancel_initialize() {
        pacer.cancel();
        assertThat(pacer.nextFireTime).isEqualTo(0);
        assertThat(pacer.future).isNull();
    }

    @Test
    public void cancel_scheduled() {
        pacer.nextFireTime = NOW + ONE_MINUTE_IN_NANOS;
        pacer.future = future;

        pacer.cancel();
        verify(future).cancel(false);
        assertThat(pacer.future).isNull();
        assertThat(pacer.nextFireTime).isEqualTo(0);
    }
}
