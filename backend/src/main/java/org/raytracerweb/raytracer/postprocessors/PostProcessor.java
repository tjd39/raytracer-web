package org.raytracerweb.raytracer.postprocessors;

import java.awt.image.BufferedImage;

import org.raytracerweb.raytracer.engine.commands.PostProcessingCommand;
import org.raytracerweb.raytracer.engine.core.CommandAbortedException;
import org.raytracerweb.raytracer.engine.core.CommandHandle;

/**
 * Generic interruptible post-processor for use in a {@link PostProcessingCommand}.
 */
public interface PostProcessor {
    void process(final CommandHandle handle) throws CommandAbortedException;
    BufferedImage getOutput();
}
