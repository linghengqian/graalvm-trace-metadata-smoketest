package com.github.benmanes.caffeine.cache.buffer;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

final class TicketBuffer<E> extends ReadBuffer<E> {
    final AtomicLong writeCounter;
    final AtomicReference<Object>[] buffer;

    long readCounter;

    @SuppressWarnings({"unchecked"})
    TicketBuffer() {
        writeCounter = new AtomicLong();
        buffer = new AtomicReference[BUFFER_SIZE];
        for (int i = 0; i < BUFFER_SIZE; i++) {
            buffer[i] = new AtomicReference<>(new Turn(i));
        }
    }

    @Override
    public int offer(E e) {
        final long writeCount = writeCounter.get();

        final int index = (int) (writeCount & BUFFER_MASK);
        AtomicReference<Object> slot = buffer[index];
        Object value = slot.get();
        if (!(value instanceof Turn)) {
            return FULL;
        } else if (((Turn) value).id != writeCount) {
            return FAILED;
        }
        if (slot.compareAndSet(value, e)) {
            writeCounter.lazySet(writeCount + 1);
            return SUCCESS;
        }
        return FAILED;
    }

    @Override
    public void drainTo(Consumer<E> consumer) {
        for (int i = 0; i < BUFFER_SIZE; i++) {
            final int index = (int) (readCounter & BUFFER_MASK);
            final AtomicReference<Object> slot = buffer[index];
            if (slot.get() instanceof Turn) {
                break;
            }
            long next = readCounter + BUFFER_SIZE;
            slot.lazySet(new Turn(next));
            readCounter++;
        }
    }

    @Override
    public long reads() {
        return readCounter;
    }

    @Override
    public long writes() {
        return writeCounter.get();
    }

    static final class Turn {
        final long id;

        Turn(long id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return Long.toString(id);
        }
    }
}
