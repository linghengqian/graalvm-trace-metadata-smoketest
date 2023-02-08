

package com.lingh;

import org.apache.commons.logging.impl.SimpleLog;

import java.io.Serial;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class StackMessageLog extends SimpleLog {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final Stack<String> messageStack = new Stack<>();
    private static final Lock lock = new ReentrantLock();

    public static void clear() {
        lock.lock();
        try {
            messageStack.clear();
        } finally {
            lock.unlock();
        }
    }

    public static List<String> getAll() {
        final Iterator<String> iterator = messageStack.iterator();
        final List<String> messages = new ArrayList<>();
        while (iterator.hasNext()) {
            messages.add(iterator.next());
        }
        return messages;
    }

    public static boolean isEmpty() {
        return messageStack.isEmpty();
    }

    public static void lock() {
        lock.lock();
    }

    public static String popMessage() {
        String ret = null;
        lock.lock();
        try {
            ret = messageStack.pop();
        } catch (final EmptyStackException ignored) {
        } finally {
            lock.unlock();
        }
        return ret;
    }

    public static void unLock() {
        try {
            lock.unlock();
        } catch (final IllegalMonitorStateException ignored) {
        }
    }

    public StackMessageLog(final String name) {
        super(name);
    }

    @Override
    protected void log(final int type, final Object message, final Throwable t) {
        lock.lock();
        try {
            final StringBuilder buf = new StringBuilder();
            buf.append(message.toString());
            if (t != null) {
                buf.append(" <");
                buf.append(t.toString());
                buf.append(">");
                final java.io.StringWriter sw = new java.io.StringWriter(1024);
                final java.io.PrintWriter pw = new java.io.PrintWriter(sw);
                t.printStackTrace(pw);
                pw.close();
                buf.append(sw);
            }
            messageStack.push(buf.toString());
        } finally {
            lock.unlock();
        }
    }
}
