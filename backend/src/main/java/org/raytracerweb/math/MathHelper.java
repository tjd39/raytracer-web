package org.raytracerweb.math;

public class MathHelper {
    // clamp value between min and max
    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    // Linear interpolation
    public static float lerp(final float first, final float second, final float t) {
        return first + (second - first) * t;
    }
}
