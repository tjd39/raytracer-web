package org.raytracerweb.raytracer.engine.core;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Unique identifier for a task, allows cancelling of dispatched tasks.
 * Used by tasks to check interrupt (cancellation) status.
 */
public class CommandHandle {
    private final UUID uuid = UUID.randomUUID();
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    public synchronized void cancel() {
        cancelled.set(true);
    }

    public synchronized boolean isCancelled() {
        return cancelled.get();
    }

    @Override
    public String toString() {
        return uuid.toString();
    }
}
