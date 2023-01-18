
package com.github.benmanes.caffeine.cache.buffer;

import java.util.function.Supplier;


@SuppressWarnings("ImmutableEnumChecker")
public enum BufferType {
    Ticket(TicketBuffer::new),
    FastFlow(FastFlowBuffer::new),
    MpscArray(MpscArrayBuffer::new),
    ManyToOne(ManyToOneBuffer::new),
    ManyToOne_spaced(ManyToOneSpacedBuffer::new),
    MpmcArray(MpmcArrayBuffer::new),
    MpscCompound(MpscCompoundBuffer::new);

    private final Supplier<ReadBuffer<Boolean>> factory;

    BufferType(Supplier<ReadBuffer<Boolean>> factory) {
        this.factory = factory;
    }

    public ReadBuffer<Boolean> create() {
        return factory.get();
    }
}
