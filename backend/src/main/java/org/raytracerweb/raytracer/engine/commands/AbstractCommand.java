package org.raytracerweb.raytracer.engine.commands;

import org.raytracerweb.raytracer.engine.core.CommandContext;
import org.raytracerweb.raytracer.engine.core.CommandHandle;

public abstract class AbstractCommand implements ICommand {
    final protected CommandHandle handle;
    final protected CommandContext context;

    public AbstractCommand(final CommandHandle handle, final CommandContext context) {
        this.handle = handle;
        this.context = context;
    }

    @Override
    public CommandHandle getHandle() {
        return handle;
    }
}
