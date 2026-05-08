package org.raytracerweb.scene.objects;

import static java.lang.Math.PI;
import static java.lang.Math.atan2;

import org.raytracerweb.geometry.vector.Vec4;
import org.raytracerweb.ray.HitInfo;
import org.raytracerweb.ray.Ray;
import org.raytracerweb.scene.objects.material.Material;

public class Sphere extends SceneObject {
    private Vec4 origin;
    private float radius;

    public Sphere(final Material material, final Vec4 origin, final float radius, final boolean render) {
        super(material, render);
        this.origin = origin;
        this.radius = radius;
    }

    public Vec4 getOrigin() {
        return origin;
    }

    public float getRadius() {
        return radius;
    }

    public void setOrigin(Vec4 origin) {
        this.origin = origin;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    @Override
    public HitInfo checkIntersection(Ray ray) {
        if (!isRender()) {
            return null;
        }

        // direction is normalised so a=1; simplified form: t = (-b ± sqrt(b²-4c)) / 2
        Vec4 local = origin.to(ray.origin());
        float b = 2.0f * ray.direction().dot(local);
        float c = local.dot(local) - radius * radius;
        float discriminant = b * b - 4.0f * c;

        float distance;
        boolean frontFaceIntersection = true;
        if (discriminant < 0.0f) {
            return null;
        } else if (discriminant == 0.0f) {
            distance = -b * 0.5f;
        } else {
            float ds = (float) Math.sqrt(discriminant);
            float t0 = (-b - ds) * 0.5f;
            float t1 = (-b + ds) * 0.5f;
            if (t0 > 0.0f && t1 > 0.0f) {
                distance = Math.min(t0, t1);
            } else if (t0 < 0.0f && t1 < 0.0f) {
                return null;
            } else {
                distance = Math.max(t0, t1);
                frontFaceIntersection = false;
            }
        }
        if (distance <= 0.0f) return null;
        return new HitInfo(distance, this, normalAt(ray.getPositionAt(distance)), frontFaceIntersection);
    }

    @Override
    public Vec4 getUVCoords(Vec4 position) {
        Vec4 normal = normalAt(position);
        float u = (float) (atan2(normal.x(), normal.z()) / (2 * PI) + 0.5f);
        float v = normal.y() * 0.5f + 0.5f;
        return new Vec4(u, v);
    }

    @Override
    public Vec4 normalAt(Vec4 position) {
        return origin.to(position).normalise();
    }
}
