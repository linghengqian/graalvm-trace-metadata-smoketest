package com.alibaba.demo.coroutine.ttl_intergration

import com.alibaba.ttl.TransmittableThreadLocal.Transmitter.*
import kotlinx.coroutines.ThreadContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun ttlContext(): CoroutineContext = TtlElement()

internal class TtlElement : ThreadContextElement<Any> {
    companion object Key : CoroutineContext.Key<TtlElement>

    override val key: CoroutineContext.Key<*> get() = Key
    private var captured: Any = capture()
    override fun updateThreadContext(context: CoroutineContext): Any = replay(captured)
    override fun restoreThreadContext(context: CoroutineContext, oldState: Any) {
        captured = capture()
        restore(oldState)
    }

    override fun minusKey(key: CoroutineContext.Key<*>): CoroutineContext = if (Key == key) EmptyCoroutineContext else this
    override operator fun <E : CoroutineContext.Element> get(key: CoroutineContext.Key<E>): E? =
            @Suppress("UNCHECKED_CAST") if (Key == key) this as E else null
}
