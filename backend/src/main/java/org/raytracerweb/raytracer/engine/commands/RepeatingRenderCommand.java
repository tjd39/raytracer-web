package org.raytracerweb.raytracer.engine.commands;

import java.util.List;

import org.raytracerweb.raytracer.IRayTracer;
import org.raytracerweb.raytracer.engine.core.CommandContext;
import org.raytracerweb.raytracer.engine.core.CommandHandle;

public class RepeatingRenderCommand extends AbstractCommand {
    private final IRayTracer rayTracer;

    public RepeatingRenderCommand(final CommandHandle handle, final CommandContext context, final IRayTracer rayTracer) {
        super(handle, context);
        this.rayTracer = rayTracer;
    }

    @Override
    public void execute() {
        final CommandHandle singleRunHandle = new CommandHandle();
        final CommandContext singleRunContext = new CommandContext(this::onComplete, () -> {}, this::onError);
        final ICommand singleRenderCommand = new RenderCommand(singleRunHandle, singleRunContext, rayTracer);

        Thread singleCommandThread = new Thread(() -> {
            try {
                singleRenderCommand.execute();
                // Note that we never report the worker thread as done as we don't want to trigger the repeating task complete callback
            } catch (Exception e) {
                onError(e);
            }
        });

        try {
            singleCommandThread.start();
        } catch (Exception e) {
            singleRunContext.triggerOnError(e);
        }

        context.setWorkerThreads(List.of(singleCommandThread));
    }

    // Technically, this will report that the repeating task (instead of the singleRunTask) is complete, which is impossible
    private void onComplete() {
        context.triggerOnComplete();

        // Repeat the task if the caller handle isn't cancelled
        if (handle.isCancelled()) {
            context.triggerOnCancel();
        } else {
            this.execute();
        }
    }

    private void onError(final Exception e) {
        // The singleRunCommand errored, propagate this upwards to caller.
        context.triggerOnError(e);
    }
}
