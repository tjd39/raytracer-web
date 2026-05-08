package org.raytracerweb.raytracer.engine.commands;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.raytracerweb.preview.canvas.Pixel;
import org.raytracerweb.raytracer.IRayTracer;
import org.raytracerweb.raytracer.engine.core.CommandContext;
import org.raytracerweb.raytracer.engine.core.CommandHandle;

/**
 * A RenderCommand that starts the rendering process using the provided ray tracer.
 * It distributes pixel tracing tasks across multiple threads using a shared atomic index.
 */
public class RenderCommand extends AbstractCommand {
    private final IRayTracer rayTracer;

    private static final ExecutorService WORKER_POOL = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(),
            r -> {
                Thread t = new Thread(r, "RenderWorker");
                t.setDaemon(true);
                return t;
            });

    /**
     * Creates a new RenderCommand with the specified handle and ray tracer.
     *
     * @param handle    The CommandHandle to manage this command.
     * @param context   The CommandContext for lifecycle callbacks.
     * @param rayTracer The ray tracer to be used for rendering.
     */
    public RenderCommand(final CommandHandle handle, final CommandContext context, final IRayTracer rayTracer) {
        super(handle, context);
        this.rayTracer = rayTracer;
    }

    @Override
    public void execute() {
        if (handle.isCancelled()) {
            context.triggerOnCancel();
            return;
        }

        rayTracer.validateCache();

        final int w = rayTracer.graphicsSettings().imageWidth();
        final int h = rayTracer.graphicsSettings().imageHeight();
        final int totalPixels = w * h;
        final AtomicInteger pixelIndex = new AtomicInteger(0);
        final int nWorkers = Runtime.getRuntime().availableProcessors();

        context.setWorkerCount(nWorkers);

        for (int i = 0; i < nWorkers; i++) {
            WORKER_POOL.submit(() -> {
                try {
                    while (true) {
                        if (handle.isCancelled()) {
                            context.triggerOnCancel();
                            return;
                        }
                        int idx = pixelIndex.getAndIncrement();
                        if (idx >= totalPixels) {
                            context.reportWorkerDone();
                            return;
                        }
                        rayTracer.pixelTrace(new Pixel(idx % w, idx / w));
                        context.triggerOnPixelComplete();
                    }
                } catch (Exception e) {
                    context.triggerOnError(e);
                }
            });
        }
    }
}
