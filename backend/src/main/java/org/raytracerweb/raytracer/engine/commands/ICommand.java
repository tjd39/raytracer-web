package org.raytracerweb.raytracer.engine.commands;

import org.raytracerweb.raytracer.engine.core.CommandContext;
import org.raytracerweb.raytracer.engine.core.CommandHandle;

/**
 * Represents a generic command.
 * Each instance is single-use, subsequent commands of the same type must be new objects.
 * Commands make use of:
 * - {@link CommandHandle} to track command cancellation status. Note these are externally-visible and cancellation can be triggered by upstream callers.
 * - {@link CommandContext} to internally track command completion.
 *
 */
public interface ICommand {
    CommandHandle getHandle();

    void execute();
}
