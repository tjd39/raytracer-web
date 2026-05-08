package org.raytracerweb.raytracer.engine.core;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class CommandContext {
    private final AtomicInteger completedWorkerThreads = new AtomicInteger(0);
    private volatile int expectedWorkers = 0;
    private final AtomicBoolean terminated = new AtomicBoolean(false);

    private final Runnable onComplete;
    private final Runnable onCancel;
    private final Consumer<Exception> onError;
    private final Runnable onPixelComplete;

    public CommandContext(final Runnable onComplete, final Runnable onCancel, final Consumer<Exception> onError) {
        this(onComplete, onCancel, onError, () -> {});
    }

    public CommandContext(final Runnable onComplete, final Runnable onCancel, final Consumer<Exception> onError, final Runnable onPixelComplete) {
        this.onComplete = onComplete;
        this.onCancel = onCancel;
        this.onError = onError;
        this.onPixelComplete = onPixelComplete;
    }

    public void triggerOnPixelComplete() {
        onPixelComplete.run();
    }

    /** Called by RenderCommand when using a thread pool (no thread refs available). */
    public void setWorkerCount(final int count) {
        this.expectedWorkers = count;
    }

    /** Kept for PostProcessingCommand compatibility. Does NOT store thread refs. */
    public void setWorkerThreads(final List<Thread> threads) {
        this.expectedWorkers = threads.size();
    }

    public void reportWorkerDone() {
        if (completedWorkerThreads.incrementAndGet() == expectedWorkers) {
            if (!terminated.get()) onComplete.run();
        }
    }

    public void triggerOnComplete() {
        onComplete.run();
    }

    public void triggerOnCancel() {
        if (terminated.compareAndSet(false, true)) onCancel.run();
    }

    public void triggerOnError(final Exception e) {
        if (terminated.compareAndSet(false, true)) onError.accept(e);
    }
}
