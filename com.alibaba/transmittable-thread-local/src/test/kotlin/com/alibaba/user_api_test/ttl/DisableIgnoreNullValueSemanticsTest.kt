package com.alibaba.user_api_test.ttl

import com.alibaba.ttl.TransmittableThreadLocal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.concurrent.FutureTask
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

class DisableIgnoreNullValueSemanticsTest {
    @Test
    fun test_TTL_not_disableIgnoreNullValueSemantics_defaultTtlBehavior() {
        val ttl = object : TransmittableThreadLocal<String?>() {
            override fun initialValue(): String {
                return "init"
            }

            override fun childValue(parentValue: String?): String {
                return "$parentValue + child"
            }
        }
        assertEquals("init", ttl.get())
        ttl.set(null)
        val task = FutureTask {
            ttl.get()
        }
        thread { task.run() }.join()
        assertEquals("init", ttl.get())
        assertEquals("init", task.get())
        val task2 = FutureTask {
            ttl.get()
        }
        thread { task2.run() }.join()
        assertEquals("init", ttl.get())
        assertEquals("init + child", task2.get())
    }

    @Test
    fun test_TTL_not_disableIgnoreNullValueSemantics_defaultTtlBehavior_getSafe_ForNullInit() {
        val count = AtomicInteger()
        val ttl = object : TransmittableThreadLocal<String?>() {
            override fun initialValue(): String? {
                count.getAndIncrement()
                return super.initialValue()
            }

            override fun childValue(parentValue: String?): String? {
                count.getAndSet(1000)
                return super.childValue(parentValue)
            }
        }
        assertNull(ttl.get())
        assertEquals(1, count.get())
        ttl.set(null)
        assertNull(ttl.get())
        assertEquals(2, count.get())
    }

    @Test
    fun test_TTL_disableIgnoreNullValueSemantics_sameAsThreadLocal() {
        val ttl = object : TransmittableThreadLocal<String?>(true) {
            override fun initialValue(): String {
                return "init"
            }

            override fun childValue(parentValue: String?): String {
                return "$parentValue + child"
            }
        }
        assertEquals("init", ttl.get())
        ttl.set(null)
        assertNull(ttl.get())
        val task = FutureTask {
            ttl.get()
        }
        thread { task.run() }.join()
        assertNull(ttl.get())
        assertEquals("null + child", task.get())
        val task2 = FutureTask {
            ttl.get()
        }
        thread { task2.run() }.join()
        assertNull(ttl.get())
        assertEquals("null + child", task.get())
    }

    @Test
    fun test_InheritableThreadLocal() {
        val ttl = object : InheritableThreadLocal<String?>() {
            override fun initialValue(): String {
                return "init"
            }

            override fun childValue(parentValue: String?): String {
                return "$parentValue + child"
            }
        }
        assertEquals("init", ttl.get())
        ttl.set(null)
        assertNull(ttl.get())
        val task = FutureTask {
            ttl.get()
        }
        thread { task.run() }.join()
        assertNull(ttl.get())
        assertEquals("null + child", task.get())
        val task2 = FutureTask {
            ttl.get()
        }
        thread { task2.run() }.join()
        assertNull(ttl.get())
        assertEquals("null + child", task.get())
    }
}
