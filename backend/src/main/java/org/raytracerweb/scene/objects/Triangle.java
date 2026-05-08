package org.raytracerweb.scene.objects;

import static org.raytracerweb.scene.objects.SurfaceNormals.IGNORE;
import static org.raytracerweb.scene.objects.SurfaceNormals.NORMAL;
import static org.raytracerweb.scene.objects.SurfaceNormals.REVERSED;

import org.raytracerweb.geometry.vector.Vec4;
import org.raytracerweb.ray.HitInfo;
import org.raytracerweb.ray.Ray;
import org.raytracerweb.scene.objects.material.Material;

public class Triangle extends SceneObject {
    // Points (in global space)
    private final Vec4 p1;
    private final Vec4 p2;
    private final Vec4 p3;

    // Local offset vectors (p1 -> p2) and (p1 -> p3)
    private final Vec4 u;
    private final Vec4 v;

    // Surface Normal
    private final Vec4 normal;

    // Helper floats
    private final float dotUU;
    private final float dotVV;
    private final float dotUV;

    private final SurfaceNormals normals;

    public Triangle(final Material material, final Vec4 p1, final Vec4 p2, final Vec4 p3, final boolean render) {
        this(material, p1, p2, p3, render, NORMAL);
    }

    public Triangle(final Material material, final Vec4 p1, final Vec4 p2, final Vec4 p3, final boolean render, final SurfaceNormals normals) {
        super(material, render);
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
        this.u = (normals.equals(REVERSED) ? p1.to(p3) : p1.to(p2));
        this.v = (normals.equals(REVERSED) ? p1.to(p2) : p1.to(p3));
        this.normal = u.cross(v).normalise();
        this.dotUU = u.dot(u);
        this.dotVV = v.dot(v);
        this.dotUV = u.dot(v);
        this.normals = normals;
    }

    public Vec4 getP1() {
        return p1;
    }

    public Vec4 getP2() {
        return p2;
    }

    public Vec4 getP3() {
        return p3;
    }

    // p1 -> p2 local vector
    public Vec4 getU() {
        return u;
    }

    // p1 -> p3 local vector
    public Vec4 getV() {
        return v;
    }

    public Vec4 getNormal() {
        return normal;
    }

    @Override
    public HitInfo checkIntersection(Ray ray) {
        if (!isRender()) {
            return null;
        }

        boolean frontFaceIntersection = false;
        float iDotN = ray.direction().dot(normal);

        if (iDotN == 0.0) {
            // Grazing Incidence
            return null;
        }

        if (iDotN < 0.0f) {
            frontFaceIntersection = true;
        }

        // Point is behind viewing plane
        float intersectionDistance = ((ray.origin().to(p1)).dot(normal)) / iDotN;
        if (intersectionDistance > 0.0f
                && pointInTriangle(ray.getPositionAt(intersectionDistance))) {
            final Vec4 normalAtHit = normalAt(ray.getPositionAt(intersectionDistance));
            return new HitInfo(intersectionDistance,
                    this,
                    (normals.equals(IGNORE) && !frontFaceIntersection) ? normalAtHit.reverse() : normalAtHit,
                    normals.equals(IGNORE) || frontFaceIntersection
            );
        }

        return null;
    }

    @Override
    public Vec4 getUVCoords(Vec4 position) {
        final Vec4 vI = p1.to(position);
        final float dotUI = u.dot(vI);
        final float dotVI = v.dot(vI);
        final float divVal = ((dotUU * dotVV) - (dotUV * dotUV));
        return new Vec4((dotVV * dotUI - dotUV * dotVI) / divVal, (dotUU * dotVI - dotUV * dotUI) / divVal);
    }

    @Override
    public Vec4 normalAt(final Vec4 position) {
        return normal;
    }

    // Barycentric coordinates... P = p1 + uP3 + vP2
    private Boolean pointInTriangle(final Vec4 point) {
        Vec4 barycentricCoords = getUVCoords(point);
        double u = barycentricCoords.x();
        double v = barycentricCoords.y();
        // 0 <= U, V <= 1 // 0 <= (U + V) <= 1 // These are the limits.
        if ((0.0 <= u) && (u <= 1.0)) {
            if ((0.0 <= v) && (v <= 1.0)) {
                return (u + v) <= 1.0;
            }
        }
        return false;
    }
}
