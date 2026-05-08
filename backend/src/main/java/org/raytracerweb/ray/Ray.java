package org.raytracerweb.ray;

import java.util.Objects;

import org.raytracerweb.geometry.vector.Vec4;

// immutable ray representation
public record Ray(Vec4 origin, Vec4 direction) {
    public Ray(final Vec4 origin, final Vec4 direction) {
        this.origin = origin;
        this.direction = direction.normalise();
    }

    public Vec4 getPositionAt(final float t) {
        return origin.plus(direction.scale(t));
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin, direction);
    }
}
