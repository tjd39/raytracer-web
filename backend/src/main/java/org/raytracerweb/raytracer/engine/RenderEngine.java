package org.raytracerweb.raytracer.engine;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import org.raytracerweb.raytracer.IRayTracer;
import org.raytracerweb.raytracer.engine.commands.ICommand;
import org.raytracerweb.raytracer.engine.commands.PostProcessingCommand;
import org.raytracerweb.raytracer.engine.commands.RenderCommand;
import org.raytracerweb.raytracer.engine.commands.RepeatingRenderCommand;
import org.raytracerweb.raytracer.engine.core.CommandContext;
import org.raytracerweb.raytracer.engine.core.CommandHandle;
import org.raytracerweb.raytracer.postprocessors.PostProcessor;

/**
 * The RenderEngine is responsible for managing the rendering process using a specified ray tracer.
 * It supports executing single trace commands as well as repeating trace commands.
 */
public class RenderEngine {
    public CommandHandle executeSingleTrace(final IRayTracer rayTracer, final Runnable onComplete, final Runnable onCancel, final Consumer<Exception> onError) {
        return executeSingleTrace(rayTracer, onComplete, onCancel, onError, () -> {});
    }

    public CommandHandle executeSingleTrace(final IRayTracer rayTracer, final Runnable onComplete, final Runnable onCancel, final Consumer<Exception> onError, final Runnable onPixelComplete) {
        final CommandHandle handle = new CommandHandle();
        final CommandContext context = new CommandContext(onComplete, onCancel, onError, onPixelComplete);
        final ICommand renderCommand = new RenderCommand(handle, context, rayTracer);

        renderCommand.execute();
        return handle;
    }

    public CommandHandle executeContinuousTrace(final IRayTracer rayTracer, final Runnable onComplete, final Runnable onCancel, final Consumer<Exception> onError) {
        final CommandHandle handle = new CommandHandle();
        final CommandContext context = new CommandContext(onComplete, onCancel, onError);
        RepeatingRenderCommand repeatingRenderCommand = new RepeatingRenderCommand(handle, context, rayTracer);
        repeatingRenderCommand.execute();
        return handle;
    }

    /**
     * Executes {@code sampleCount} render passes sequentially on the same canvas, accumulating
     * colours per pixel.  {@code onSampleComplete} fires after each pass; {@code onComplete}
     * fires once all passes finish.
     */
    public CommandHandle executeAccumulatedTrace(final IRayTracer rayTracer, final int sampleCount,
            final Runnable onComplete, final Runnable onCancel, final Consumer<Exception> onError,
            final Runnable onPixelComplete, final Runnable onSampleComplete) {
        final CommandHandle handle = new CommandHandle();

        Thread coordinator = new Thread(() -> {
            try {
                for (int sample = 0; sample < sampleCount; sample++) {
                    if (handle.isCancelled()) { onCancel.run(); return; }

                    CountDownLatch latch = new CountDownLatch(1);
                    final boolean[] failed = {false};
                    CommandContext ctx = new CommandContext(
                            latch::countDown,
                            () -> { handle.cancel(); latch.countDown(); },
                            e -> { failed[0] = true; onError.accept(e); latch.countDown(); },
                            onPixelComplete);
                    new RenderCommand(handle, ctx, rayTracer).execute();
                    latch.await();
                    if (failed[0]) return;
                    onSampleComplete.run();
                }
                if (!handle.isCancelled()) onComplete.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                onCancel.run();
            }
        }, "AccumulatorCoordinator");
        coordinator.setDaemon(true);
        coordinator.start();

        return handle;
    }

    public CommandHandle executePostProcess(final PostProcessor postProcessor, final Runnable onComplete, final Runnable onCancel, final Consumer<Exception> onError) {
        final CommandHandle handle = new CommandHandle();
        final CommandContext context = new CommandContext(onComplete, onCancel, onError);
        final ICommand renderCommand = new PostProcessingCommand(handle, context, postProcessor);

        renderCommand.execute();
        return handle;
    }
}

