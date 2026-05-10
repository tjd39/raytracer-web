package org.raytracerweb.api;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.imageio.ImageIO;

import org.raytracerweb.raytracer.IRayTracer;
import org.raytracerweb.raytracer.engine.core.CommandHandle;

public class RenderJob {

    public enum Status { PENDING, RUNNING, COMPLETE, ERROR }

    private final String id = UUID.randomUUID().toString();
    private final int totalPixels;
    private final int sampleCount;
    private final AtomicInteger completedPixels = new AtomicInteger(0);
    private final AtomicInteger completedSamples = new AtomicInteger(0);
    private final AtomicReference<Status> status = new AtomicReference<>(Status.PENDING);
    private volatile CommandHandle commandHandle;
    private volatile String errorMessage;

    // Holds the tracer while rendering; nulled after PNG is encoded to free heap.
    private volatile IRayTracer rayTracer;
    // Encoded PNG; set once on completion, tracer is then released.
    private volatile byte[] pngBytes;

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

    /** Encodes the canvas to PNG, stores the bytes, and releases the tracer/canvas from heap. */
    public void encodePngAndRelease() {
        IRayTracer tracer = this.rayTracer;
        if (tracer == null) return;
        try {
            BufferedImage image = tracer.canvas().getBufferedImage();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            this.pngBytes = baos.toByteArray();
        } catch (IOException e) {
            this.errorMessage = "PNG encoding failed: " + e.getMessage();
            this.status.set(Status.ERROR);
        } finally {
            this.rayTracer = null;
        }
    }

    public byte[] getPngBytes() { return pngBytes; }

    public String getId() { return id; }
    public Status getStatus() { return status.get(); }
    public void setStatus(Status s) { status.set(s); }
    public CommandHandle getCommandHandle() { return commandHandle; }
    public void setCommandHandle(CommandHandle handle) { this.commandHandle = handle; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String msg) { this.errorMessage = msg; }
}
