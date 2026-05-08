package org.raytracerweb.scene.objects;

import static org.raytracerweb.preview.colour.ColourPreset.debugNext;

import org.raytracerweb.geometry.Axis;
import org.raytracerweb.geometry.vector.Vec4;
import org.raytracerweb.ray.HitInfo;
import org.raytracerweb.ray.Ray;
import org.raytracerweb.scene.objects.material.Material;

public class AABB extends SceneObject {
    private float minX = Float.MAX_VALUE;
    private float minY = Float.MAX_VALUE;
    private float minZ = Float.MAX_VALUE;
    private float maxX = -Float.MAX_VALUE;
    private float maxY = -Float.MAX_VALUE;
    private float maxZ = -Float.MAX_VALUE;

    public AABB() {
        super(Material.transparent(debugNext(), 0.97f, 1.0f), true);
    }

    public Vec4 min() {
        return new Vec4(minX, minY, minZ);
    }

    public Vec4 max() {
        return new Vec4(maxX, maxY, maxZ);
    }

    public Axis longestAxis() {
        return min().to(max()).longestAxis();
    }

    public Vec4 center() {
        return min().plus(min().to(max()).scale(0.5f));
    }

    // Update this box with a single vector
    public void update(float x, float y, float z) {
        minX = Math.min(minX, x);
        minY = Math.min(minY, y);
        minZ = Math.min(minZ, z);

        maxX = Math.max(maxX, x);
        maxY = Math.max(maxY, y);
        maxZ = Math.max(maxZ, z);
    }

    public void addBias() {
        minX -= BIAS;
        minY -= BIAS;
        minZ -= BIAS;
        maxX += BIAS;
        maxY += BIAS;
        maxZ += BIAS;
    }

    // Combine another box into this one (for parallel streams)
    public void combine(AABB other) {
        minX = Math.min(minX, other.minX);
        minY = Math.min(minY, other.minY);
        minZ = Math.min(minZ, other.minZ);

        maxX = Math.max(maxX, other.maxX);
        maxY = Math.max(maxY, other.maxY);
        maxZ = Math.max(maxZ, other.maxZ);
    }

    /**
     * Returns the entry distance along the ray into this AABB, or -1 if the ray misses.
     * Cheaper than checkIntersection — no HitInfo/Vec4 allocation, no normal computation.
     * Used by BVH traversal for AABB culling.
     */
    public float hitDistance(final Ray ray) {
        float tNear = -Float.MAX_VALUE;
        float tFar  =  Float.MAX_VALUE;

        // X slab
        float ox = ray.origin().x(), dx = ray.direction().x();
        if (Math.abs(dx) < 1e-6f) {
            if (ox < minX || ox > maxX) return -1f;
        } else {
            float inv = 1f / dx;
            float t1 = (minX - ox) * inv;
            float t2 = (maxX - ox) * inv;
            if (t1 > t2) { float tmp = t1; t1 = t2; t2 = tmp; }
            tNear = Math.max(tNear, t1);
            tFar  = Math.min(tFar,  t2);
            if (tNear > tFar || tFar < 0f) return -1f;
        }

        // Y slab
        float oy = ray.origin().y(), dy = ray.direction().y();
        if (Math.abs(dy) < 1e-6f) {
            if (oy < minY || oy > maxY) return -1f;
        } else {
            float inv = 1f / dy;
            float t1 = (minY - oy) * inv;
            float t2 = (maxY - oy) * inv;
            if (t1 > t2) { float tmp = t1; t1 = t2; t2 = tmp; }
            tNear = Math.max(tNear, t1);
            tFar  = Math.min(tFar,  t2);
            if (tNear > tFar || tFar < 0f) return -1f;
        }

        // Z slab
        float oz = ray.origin().z(), dz = ray.direction().z();
        if (Math.abs(dz) < 1e-6f) {
            if (oz < minZ || oz > maxZ) return -1f;
        } else {
            float inv = 1f / dz;
            float t1 = (minZ - oz) * inv;
            float t2 = (maxZ - oz) * inv;
            if (t1 > t2) { float tmp = t1; t1 = t2; t2 = tmp; }
            tNear = Math.max(tNear, t1);
            tFar  = Math.min(tFar,  t2);
            if (tNear > tFar || tFar < 0f) return -1f;
        }

        return tNear >= 0f ? tNear : tFar;
    }

    public HitInfo checkIntersection(Ray ray) {
        float tNear = -Float.MAX_VALUE;
        float tFar = Float.MAX_VALUE;
        boolean frontFaceIntersection = true;

        for (Axis axis : Axis.values()) {
            float origin = ray.origin().get(axis);
            float direction = ray.direction().get(axis);
            float minVal = min().get(axis);
            float maxVal = max().get(axis);

            if (Math.abs(direction) < 1e-6f) {
                // Ray is parallel to this axis's planes
                if (origin < minVal || origin > maxVal) {
                    return null;
                }
            } else {
                // Calculate distance to the near and far planes of this "slab"
                float invDir = 1.0f / direction;
                float t1 = (minVal - origin) * invDir;
                float t2 = (maxVal - origin) * invDir;

                // Swap if t1 is further than t2
                if (t1 > t2) {
                    float temp = t1;
                    t1 = t2;
                    t2 = temp;
                }

                // Shrink the overall t-interval
                tNear = Math.max(tNear, t1);
                tFar = Math.min(tFar, t2);

                // Exit early if the intersection interval becomes empty
                if (tNear > tFar || tFar < 0) {
                    return null;
                }
            }
        }

        float tHit;
        if (tNear >= 0f) {
            tHit = tNear;
        } else {
            // Ray started inside the box
            tHit = tFar;
            frontFaceIntersection = false;
        }

        // We hit! Construct the HitInfo.
        // Usually, we pass the current AABB or its parent SceneObject here.
        return new HitInfo(tHit, this, normalAt(ray.getPositionAt(tHit)), frontFaceIntersection);
    }

    @Override
    public Vec4 normalAt(Vec4 position) {
        float epsilon = 0.0001f;

        for (Axis axis : Axis.values()) {
            if (Math.abs(position.get(axis) - min().get(axis)) < epsilon) {
                return createNormal(axis, 1.0f);
            }
            if (Math.abs(position.get(axis) - max().get(axis)) < epsilon) {
                return createNormal(axis, -1.0f);
            }
        }

        // Shouldn't get here.
        throw new IllegalStateException("Exception calculating AABB normal at " + position);
    }

    private Vec4 createNormal(Axis axis, float value) {
        return new Vec4(
                axis == Axis.X ? value : 0,
                axis == Axis.Y ? value : 0,
                axis == Axis.Z ? value : 0
        );
    }
}
