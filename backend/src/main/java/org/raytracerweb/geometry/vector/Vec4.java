package org.raytracerweb.geometry.vector;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static org.raytracerweb.geometry.Axis.X;
import static org.raytracerweb.geometry.Axis.Y;
import static org.raytracerweb.geometry.Axis.Z;

import java.util.concurrent.ThreadLocalRandom;

import org.raytracerweb.geometry.Axis;
import org.raytracerweb.geometry.rotation.RotationMatrix;
import org.raytracerweb.math.MathHelper;

/**
 * Represents an immutable 4-dimensional (Homogenous) vector.
 * This is the generic vector representation (Vec3 and Vec2 obsolete).
 */
public record Vec4(float x, float y, float z, float a) {
    public Vec4() {
        this(0f, 0f, 0f, 1f);
    }

    public Vec4(float x, float y) {
        this(x, y, 0f, 0f);
    }

    public Vec4(float x, float y, float z) {
        this(x, y, z, 1f);
    }

    public Vec4(Vec4 other) {
        this(other.x(), other.y(), other.z(), other.a());
    }

    public Vec4(Axis axis) {
        this(switch (axis) {
            case X -> new Vec4(1f, 0f, 0f, 0f);
            case Y -> new Vec4(0f, 1f, 0f, 0f);
            case Z -> new Vec4(0f, 0f, 1f, 0f);
        });
    }

    public Vec4(float x, float y, float z, float a) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.a = a;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public float z() {
        return z;
    }

    public float a() {
        return a;
    }

    public float get(Axis axis) {
        return switch (axis) {
            case X -> x;
            case Y -> y;
            case Z -> z;
        };
    }

    public Vec4 scale(float scale) {
        return new Vec4(x * scale, y * scale, z * scale, a);
    }

    public Vec4 reverse() {
        return scale(-1f);
    }

    public Vec4 normalise() {
        float l = (float) sqrt(x * x + y * y + z * z);
        return new Vec4(
                x / l,
                y / l,
                z / l,
                a
        );
    }

    public Vec4 plus(Vec4 other) {
        return new Vec4(x + other.x, y + other.y, z + other.z, a);
    }

    public float dot(Vec4 other) {
        return x * other.x + y * other.y + z * other.z;
    }

    public Vec4 cross(Vec4 other) {
        return new Vec4(
                (y * other.z) - (other.y * z),
                (z * other.x) - (other.z * x),
                (x * other.y) - (other.x * y),
                a
        );
    }

    public Vec4 to(Vec4 other) {
        return new Vec4(
                other.x - x,
                other.y - y,
                other.z - z,
                a
        );
    }

    public Vec4 rotate(RotationMatrix rotation) {
        return rotation.apply(this);
    }

    public Vec4 mask(Axis axis) {
        return switch (axis) {
            case X -> new Vec4(x, 0f, 0f, a);
            case Y -> new Vec4(0f, y, 0f, a);
            case Z -> new Vec4(0f, 0f, z, a);
        };
    }

    public Vec4 cosinePowerSample(float power) {
        float u = ThreadLocalRandom.current().nextFloat();
        float v = ThreadLocalRandom.current().nextFloat();
        float theta = (float) (2f * PI * v);
        float cosTheta = (float) pow(u, 1.0f / (power + 1f));
        float r = (float) sqrt(1f - cosTheta * cosTheta);
        float x = (float) (r * cos(theta));
        float y = (float) (r * sin(theta));

        final Vec4 helper;
        if (abs(this.x) > 0.9)
            helper = new Vec4(0f, 1f, 0f);
        else
            helper = new Vec4(1f, 0f, 0f);

        Vec4 T = this.cross(helper).normalise();
        Vec4 B = this.cross(T);
        return T.scale(x)
                .plus(B.scale(y))
                .plus(scale(cosTheta));
    }

    public Vec4 lerp(Vec4 other, float t) {
        return new Vec4(
                MathHelper.lerp(x, other.x, t),
                MathHelper.lerp(y, other.y, t),
                MathHelper.lerp(z, other.z, t),
                a
        );
    }

    /**
     * Assumes a 3-dimensional vector (a is ignored)
     */
    public Axis longestAxis() {
        if (x > y && x > z) {
            return X;
        } else if (y > x && y > z) {
            return Y;
        } else {
            return Z; // default if all axes equal in length
        }
    }

    public float length() {
        return (float) sqrt(x * x + y * y + z * z);
    }

    public static Vec4 randomHemisphere() {
        return new Vec4(
                2f * (ThreadLocalRandom.current().nextFloat() - 0.5f),
                2f * (ThreadLocalRandom.current().nextFloat() - 0.5f),
                2f * (ThreadLocalRandom.current().nextFloat() - 0.5f)
        ).normalise();
    }
}
