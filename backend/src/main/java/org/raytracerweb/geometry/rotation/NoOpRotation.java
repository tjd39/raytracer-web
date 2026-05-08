package org.raytracerweb.geometry.rotation;

import org.raytracerweb.geometry.vector.Vec4;

public class NoOpRotation extends RotationMatrix {
    public NoOpRotation() {
        super(null);
    }

    @Override
    public Vec4 apply(Vec4 target) {
        return target; // no-op
    }

    @Override
    public Vec4 applyInverse(Vec4 target) {
        return target; // no-op
    }
}
