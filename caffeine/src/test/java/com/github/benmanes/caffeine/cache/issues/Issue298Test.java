
package com.github.benmanes.caffeine.cache.issues;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.Policy.VarExpiration;
import com.github.benmanes.caffeine.testing.ConcurrentTestHarness;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.benmanes.caffeine.testing.Awaits.await;
import static com.github.benmanes.caffeine.testing.ConcurrentTestHarness.executor;
import static com.google.common.truth.Truth.assertThat;

@Test(groups = "isolated")
public final class Issue298Test {
    static final long EXPIRE_NS = Duration.ofDays(1).toNanos();

    AtomicBoolean startedLoad;
    AtomicBoolean doLoad;

    AtomicBoolean startedCreate;
    AtomicBoolean doCreate;

    AtomicBoolean startedRead;
    AtomicBoolean doRead;
    AtomicBoolean endRead;

    AsyncLoadingCache<String, String> cache;
    VarExpiration<String, String> policy;
    String key;

    @BeforeMethod
    public void before() {
        startedCreate = new AtomicBoolean();
        startedLoad = new AtomicBoolean();
        startedRead = new AtomicBoolean();
        doCreate = new AtomicBoolean();
        endRead = new AtomicBoolean();
        doLoad = new AtomicBoolean();
        doRead = new AtomicBoolean();

        key = "key";
        cache = makeAsyncCache();
        policy = cache.synchronous().policy().expireVariably().orElseThrow();
    }

    @AfterMethod
    public void after() {
        endRead.set(true);
    }

    @Test
    @SuppressWarnings("FutureReturnValueIgnored")
    public void readDuringCreate() {
        cache.get(key);
        await().untilTrue(startedLoad);
        doLoad.set(true);
        await().untilTrue(startedCreate);
        var reader = CompletableFuture.runAsync(() -> {
            do {
                cache.get(key);
            } while (!endRead.get());
        }, executor);
        doCreate.set(true);
        await().until(() -> policy.getExpiresAfter(key).orElseThrow().toNanos() <= EXPIRE_NS);
        await().untilTrue(startedRead);
        doRead.set(true);
        endRead.set(true);
        reader.join();
        assertThat(policy.getExpiresAfter(key).orElseThrow().toNanos()).isAtMost(EXPIRE_NS);
    }

    private AsyncLoadingCache<String, String> makeAsyncCache() {
        return Caffeine.newBuilder().executor(ConcurrentTestHarness.executor).expireAfter(new Expiry<String, String>() {
                    @Override
                    public long expireAfterCreate(@Nonnull String key,
                                                  @Nonnull String value, long currentTime) {
                        startedCreate.set(true);
                        await().untilTrue(doCreate);
                        return EXPIRE_NS;
                    }

                    @Override
                    public long expireAfterUpdate(@Nonnull String key,
                                                  @Nonnull String value, long currentTime, long currentDuration) {
                        return currentDuration;
                    }

                    @Override
                    public long expireAfterRead(@Nonnull String key,
                                                @Nonnull String value, long currentTime, long currentDuration) {
                        startedRead.set(true);
                        await().untilTrue(doRead);
                        return currentDuration;
                    }
                })
                .buildAsync(key -> {
                    startedLoad.set(true);
                    await().untilTrue(doLoad);
                    return key + "'s value";
                });
    }
}
