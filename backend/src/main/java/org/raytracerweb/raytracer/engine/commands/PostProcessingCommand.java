package org.raytracerweb.raytracer.engine.commands;

import java.util.List;

import org.raytracerweb.raytracer.engine.core.CommandContext;
import org.raytracerweb.raytracer.engine.core.CommandAbortedException;
import org.raytracerweb.raytracer.engine.core.CommandHandle;
import org.raytracerweb.raytracer.postprocessors.PostProcessor;

/**
 * Single-threaded post-processor command accepting a {@link PostProcessor} implementation.
 */
public class PostProcessingCommand extends AbstractCommand {
    private final PostProcessor postProcessor;

    public PostProcessingCommand(final CommandHandle handle, final CommandContext context, final PostProcessor postProcessor) {
        super(handle, context);
        this.postProcessor = postProcessor;
    }

    @Override
    public void execute() {
        Thread singleCommandThread = new Thread(() -> {
            try {
                // Cancellation is checked only within the post-processor, not here.
                postProcessor.process(handle);
                context.reportWorkerDone();
            } catch (CommandAbortedException e) {
                context.triggerOnCancel();
            } catch (Exception e) {
                context.triggerOnError(e);
            }
        }, "Worker-0");

        try {
            singleCommandThread.start();
        } catch (Exception e) {
            context.triggerOnError(e);
        }

        context.setWorkerThreads(List.of(singleCommandThread));
    }
}
