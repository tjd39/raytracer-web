package org.raytracerweb.api;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.raytracerweb.raytracer.IRayTracer;
import org.raytracerweb.raytracer.engine.core.CommandHandle;

public class RenderJob {

    public enum Status { PENDING, RUNNING, COMPLETE, ERROR }

    private final String id = UUID.randomUUID().toString();
    private final IRayTracer rayTracer;
    private final int totalPixels;
    private final int sampleCount;
    private final AtomicInteger completedPixels = new AtomicInteger(0);
    private final AtomicInteger completedSamples = new AtomicInteger(0);
    private final AtomicReference<Status> status = new AtomicReference<>(Status.PENDING);
    private volatile CommandHandle commandHandle;
    private volatile String errorMessage;

    public RenderJob(IRayTracer rayTracer) { this(rayTracer, 1); }

    public RenderJob(IRayTracer rayTracer, int sampleCount) {
        this.rayTracer = rayTracer;
        this.sampleCount = sampleCount;
        this.totalPixels = rayTracer.graphicsSettings().imageWidth()
                * rayTracer.graphicsSettings().imageHeight()
                * sampleCount;
    }

    public void incrementPixel() { completedPixels.incrementAndGet(); }
    public void incrementSample() { completedSamples.incrementAndGet(); }

    public int getCompletedSamples() { return completedSamples.get(); }
    public int getSampleCount() { return sampleCount; }

    public int getProgressPercent() {
        if (totalPixels == 0) return 0;
        return (int) Math.min(100L, completedPixels.get() * 100L / totalPixels);
    }

    public String getId() { return id; }
    public IRayTracer getRayTracer() { return rayTracer; }
    public Status getStatus() { return status.get(); }
    public void setStatus(Status s) { status.set(s); }
    public CommandHandle getCommandHandle() { return commandHandle; }
    public void setCommandHandle(CommandHandle handle) { this.commandHandle = handle; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String msg) { this.errorMessage = msg; }
}
