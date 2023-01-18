
package com.github.benmanes.caffeine.cache.buffer;

import org.jctools.queues.MpscCompoundQueue;


final class MpscCompoundBuffer<E> extends ReadBuffer<E> {
    final MpscCompoundQueue<E> queue;
    long reads;

    MpscCompoundBuffer() {
        queue = new MpscCompoundQueue<>(BUFFER_SIZE);
    }

    @Override
    public int offer(E e) {
        return queue.offer(e) ? SUCCESS : FULL;
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
