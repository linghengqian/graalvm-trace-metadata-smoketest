package com.alibaba.demo.session_cache

import com.alibaba.expandThreadPool
import com.alibaba.ttl.TransmittableThreadLocal
import com.alibaba.ttl.TtlRunnable
import com.alibaba.ttl.threadpool.TtlExecutors
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.reactivex.Flowable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.*


class SessionCacheDemo : AnnotationSpec() {
    @Test
    fun invokeInThreadOfThreadPool() {
        val bizService = BizService()
        val printer: () -> Unit = { System.out.printf("[%20s] cache: %s%n", Thread.currentThread().name, bizService.getCacheItem()) }
        executorService.submit(Callable {
            bizService.getItemByCache().also { printer() }
        }).get()
        printer()
        bizService.getItemByCache()
        printer()
    }

    @Test
    fun invokeInThreadOfRxJava() {
        val bizService = BizService()
        val printer: (Item) -> Unit = { System.out.printf("[%30s] cache: %s%n", Thread.currentThread().name, bizService.getCacheItem()) }
        Flowable.just(bizService).observeOn(Schedulers.io()).map(BizService::getItemByCache).doOnNext(printer).blockingSubscribe(printer)
        bizService.getItemByCache().let(printer)
    }

    @BeforeAll
    fun beforeAll() {
        (0 until Runtime.getRuntime().availableProcessors() * 2).map {
            FutureTask {
                Thread.sleep(10)
                it
            }.apply { Schedulers.io().scheduleDirect(this) }
        }.forEach { it.get() }
        RxJavaPlugins.setScheduleHandler(TtlRunnable::get)
    }

    @AfterAll
    fun afterAll() {
        executorService.shutdown()
        executorService.awaitTermination(1, TimeUnit.SECONDS).shouldBeTrue()
    }

    @After
    fun tearDown() {
        BizService.clearCache()
    }

    companion object {
        private val executorService = Executors.newFixedThreadPool(3).let {
            expandThreadPool(it)
            TtlExecutors.getTtlExecutorService(it)!!
        }
    }
}

private class BizService {
    init {
        getCache()
    }

    fun getItem(): Item = Item(ThreadLocalRandom.current().nextInt(0, 10_000))
    fun getItemByCache(): Item {
        return getCache().computeIfAbsent(ONLY_KEY) { getItem() }
    }

    fun getCacheItem(): Item? = getCache()[ONLY_KEY]

    companion object {
        private const val ONLY_KEY = "ONLY_KEY"
        private val cacheContext = object : TransmittableThreadLocal<ConcurrentMap<String, Item>>() {
            override fun initialValue(): ConcurrentMap<String, Item> = ConcurrentHashMap()
        }

        private fun getCache() = cacheContext.get()
        fun clearCache() {
            getCache().clear()
        }
    }
}

private data class Item(val id: Int)
