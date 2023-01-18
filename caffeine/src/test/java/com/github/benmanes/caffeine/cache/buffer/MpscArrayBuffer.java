
package com.github.benmanes.caffeine.cache.buffer;

import org.jctools.queues.MpscArrayQueue;


final class MpscArrayBuffer<E> extends ReadBuffer<E> {
    final MpscArrayQueue<E> queue;
    long reads;

    MpscArrayBuffer() {
        queue = new MpscArrayQueue<>(BUFFER_SIZE);
    }

    @Override
    public int offer(E e) {
        return queue.failFastOffer(e);
    }

    @Override
    public void drainTo(Consumer<E> consumer) {
        reads += queue.drain(consumer);
    }

    @Override
    public long reads() {
        return reads;
    }

    @Override
    public long writes() {
        return reads + queue.size();
    }
}
