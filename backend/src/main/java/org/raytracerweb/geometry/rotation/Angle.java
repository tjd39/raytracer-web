package org.raytracerweb.geometry.rotation;

public record Angle(float degrees, float radians) {
    public static Angle degrees(final float degrees) {
        return new Angle(degrees, (float) (degrees * Math.PI / 180));
    }

    public static Angle radians(final float radians) {
        return new Angle((float) (radians * 180 / Math.PI), radians);
    }
}
