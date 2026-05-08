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

        // offset ray to local object co-ords
        Vec4 local = origin.to(ray.origin());

        // Calculate Quadratic Elements: x = (-b +- sqrt((b * b) - (4.0 * a * c))) / (2.0 * a)
        float a = ray.direction().dot(ray.direction());
        float b = 2.0f * (ray.direction().dot(local));
        float c = (local.dot(local)) - (radius * radius);
        float quadraticSolution = (float) ((b * b) - (4.0 * a * c));

        // Ray misses the sphere
        float distance;
        boolean frontFaceIntersection = true;
        if (quadraticSolution < 0.0f) {
            return null;
        } else if (quadraticSolution == 0.0f) {
            distance = -b / (2.0f * a);
        } else {
            float ds = (float) Math.sqrt(quadraticSolution);
            float t0 = ((-b - ds) / (2.0f * a));
            float t1 = ((-b + ds) / (2.0f * a));
            if (t0 > 0.0f && t1 > 0.0f) {
                distance = Math.min(t0, t1); // both are in front of the ray, take the closest one.
            } else if (t0 < 0.0f && t1 < 0.0f) {
                return null; // both are behind the ray, take neither.
            } else {
                distance = Math.max(t0, t1); // only one is in front of the ray, take that one.
                frontFaceIntersection = false;
            }
        }

        if (distance <= 0.0f) {
            return null;
        }

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
