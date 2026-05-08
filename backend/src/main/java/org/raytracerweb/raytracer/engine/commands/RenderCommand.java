package org.raytracerweb.raytracer.engine.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.raytracerweb.preview.canvas.Pixel;
import org.raytracerweb.raytracer.IRayTracer;
import org.raytracerweb.raytracer.engine.core.CommandContext;
import org.raytracerweb.raytracer.engine.core.CommandHandle;

/**
 * A RenderCommand that starts the rendering process using the provided ray tracer.
 * It distributes pixel tracing tasks across multiple threads, using a pixel queue.
 */
public class RenderCommand extends AbstractCommand {
    private final IRayTracer rayTracer;
    private Pixel canvasSizeTracker;
    private ConcurrentLinkedQueue<Pixel> originalSharedPixelQueue;
    private ConcurrentLinkedQueue<Pixel> workingSharedPixelQueue;

    /**
     * Creates a new StartRenderCommand with the specified handle and ray tracer.
     *
     * @param handle    The RenderCommandHandle to manage this command.
     * @param rayTracer The ray tracer to be used for rendering.
     */
    public RenderCommand(final CommandHandle handle, final CommandContext context, final IRayTracer rayTracer) {
        super(handle, context);
        this.rayTracer = rayTracer;
        resetSharedPixelQueue();
    }

    @Override
    public void execute() {
        if (handle.isCancelled()) {
            context.triggerOnCancel();
        }

        resetSharedPixelQueue();
        rayTracer.validateCache();
        List<Thread> threads = new ArrayList<>();

        // Build threads, these pick pixels off a shared queue and call the rayTracer to render that pixel
        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
            Thread t = new Thread(() -> {
                try {
                    while (true) {
                        Pixel pixel = workingSharedPixelQueue.poll();
                        if (pixel == null) {
                            // Pixel queue empty, no more work to do
                            context.reportWorkerDone();
                            break;
                        }

                        // Cancellation is not checked within the ray tracer logic, only here.
                        if (handle.isCancelled()) {
                            context.triggerOnCancel();
                            break;
                        } else {
                            rayTracer.pixelTrace(pixel);
                            context.triggerOnPixelComplete();
                        }
                    }
                } catch (Exception e) {
                    context.triggerOnError(e);
                }
            }, "Worker-" + i);
            threads.add(t);
        }

        // Start all threads once they are created.
        for (Thread t : threads) {
            try {
                t.start();
            } catch (Exception e) {
                context.triggerOnError(e);
            }
        }

        context.setWorkerThreads(threads);
    }

    private ConcurrentLinkedQueue<Pixel> buildSharedPixelQueue() {
        final int imageWidth = rayTracer.graphicsSettings().imageWidth();
        final int imageHeight = rayTracer.graphicsSettings().imageHeight();
        final ConcurrentLinkedQueue<Pixel> sharedPixelQueue = new ConcurrentLinkedQueue<>();

        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                sharedPixelQueue.add(new Pixel(x, y));
            }
        }

        this.canvasSizeTracker = new Pixel(imageWidth, imageHeight);
        return sharedPixelQueue;
    }

    private void resetSharedPixelQueue() {
        if (originalSharedPixelQueue == null
                || canvasSizeTracker == null
                || canvasSizeTracker.x() != rayTracer.graphicsSettings().imageWidth()
                || canvasSizeTracker.y() != rayTracer.graphicsSettings().imageHeight()) {
            this.originalSharedPixelQueue = buildSharedPixelQueue();
        }
        workingSharedPixelQueue = new ConcurrentLinkedQueue<>(originalSharedPixelQueue);
    }
}
