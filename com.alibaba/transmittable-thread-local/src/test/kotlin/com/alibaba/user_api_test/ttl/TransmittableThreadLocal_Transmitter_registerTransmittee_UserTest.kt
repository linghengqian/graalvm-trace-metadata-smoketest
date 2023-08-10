package com.alibaba.user_api_test.ttl

import com.alibaba.noTtlAgentRun
import com.alibaba.ttl.TransmittableThreadLocal.Transmitter
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.core.test.config.TestCaseConfig
import io.kotest.matchers.booleans.shouldBeTrue
import io.mockk.*
import org.apache.commons.lang3.JavaVersion
import org.apache.commons.lang3.SystemUtils

class TransmittableThreadLocal_Transmitter_registerTransmittee_UserTest : AnnotationSpec() {
    @Suppress("OVERRIDE_DEPRECATION")
    override fun defaultTestCaseConfig(): TestCaseConfig {
        if (SystemUtils.isJavaVersionAtMost(JavaVersion.JAVA_1_8)) {
            return TestCaseConfig(enabled = true)
        }
        return TestCaseConfig(enabled = noTtlAgentRun())
    }

    @Test
    fun test_registerTransmittee_crr() {
        val transmittee = mockk<Transmitter.Transmittee<List<String>, Set<Int>>>()
        @Suppress("UnusedEquals", "ReplaceCallWithBinaryOperator")
        excludeRecords {
            transmittee.equals(any())
            transmittee.hashCode()
        }
        every { transmittee.capture() } returns listOf("42", "43")
        every { transmittee.replay(listOf("42", "43")) } returns setOf(42, 43)
        every { transmittee.restore(setOf(42, 43)) } just Runs
        try {
            Transmitter.registerTransmittee(transmittee).shouldBeTrue()
            val captured = Transmitter.capture()
            val backup = Transmitter.replay(captured)
            Transmitter.restore(backup)
            verifySequence {
                transmittee.capture()
                transmittee.replay(any())
                transmittee.restore(any())
            }
            confirmVerified(transmittee)
        } finally {
            Transmitter.unregisterTransmittee(transmittee).shouldBeTrue()
        }
    }

    @Test
    fun test_registerTransmittee_clear_restore() {
        val transmittee = mockk<Transmitter.Transmittee<List<String>, Set<Int>>>()
        @Suppress("UnusedEquals", "ReplaceCallWithBinaryOperator")
        excludeRecords {
            transmittee.equals(any())
            transmittee.hashCode()
        }
        every { transmittee.clear() } returns setOf(42, 43)
        every { transmittee.restore(setOf(42, 43)) } just Runs
        try {
            Transmitter.registerTransmittee(transmittee).shouldBeTrue()
            val backup = Transmitter.clear()
            Transmitter.restore(backup)
            verifySequence {
                transmittee.clear()
                transmittee.restore(any())
            }
            confirmVerified(transmittee)
        } finally {
            Transmitter.unregisterTransmittee(transmittee).shouldBeTrue()
        }
    }
}
