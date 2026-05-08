package org.raytracerweb.raytracer.engine.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class CommandContext {
    private final AtomicInteger completedWorkerThreads = new AtomicInteger(0);
    private final List<Thread> workerThreads = new ArrayList<>();
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

    public synchronized void setWorkerThreads(final List<Thread> workerThreads) {
        this.workerThreads.clear();
        this.workerThreads.addAll(workerThreads);
    }

    public synchronized void reportWorkerDone() {
        if (completedWorkerThreads.incrementAndGet() == workerThreads.size()) {
            triggerOnComplete();
        }
    }

    public synchronized void triggerOnComplete() {
        onComplete.run();
    }

    public synchronized void triggerOnCancel() {
        workerThreads.forEach(Thread::interrupt);
        onCancel.run();
    }

    public synchronized void triggerOnError(Exception e) {
        workerThreads.forEach(Thread::interrupt);
        onError.accept(e);
    }
}

