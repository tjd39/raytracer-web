package org.raytracerweb.scene.objects;


import static java.lang.Math.abs;

import org.raytracerweb.geometry.vector.Vec4;
import org.raytracerweb.ray.HitInfo;
import org.raytracerweb.ray.Ray;
import org.raytracerweb.scene.objects.material.Material;
import org.raytracerweb.scene.objects.mesh.DivisorDirection;

public class Plane extends SceneObject {
    private Vec4 normal;
    private float distance;
    private Vec4 e1;
    private Vec4 e2;

    public Plane(final Material material, final Vec4 normal, final float distance, final boolean render) {
        super(material, render);
        this.normal = normal.normalise();
        this.distance = distance;
        recalculateE1E2();
    }

    public Vec4 getNormal() {
        return normal;
    }

    public float getDistance() {
        return distance;
    }

    public void setNormal(Vec4 normal) {
        this.normal = normal;
        recalculateE1E2();
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    private void recalculateE1E2() {
        this.e1 = ((abs(normal.x()) > abs(normal.z()))
                ? new Vec4(-normal.y(), normal.x(), 0f)
                : new Vec4(0f, -normal.z(), normal.y())).normalise();
        this.e2 = normal.cross(e1).normalise();
    }

    public DivisorDirection checkObject(SceneObject sceneObject) {
        // LEFT, RIGHT or BOTH

        if (sceneObject instanceof Sphere) {
            // Plane-Sphere test:
            return checkSphere((Sphere) sceneObject);
        }

        if (sceneObject instanceof Triangle) {
            // Plane-Triangle test;
            return checkTriangle((Triangle) sceneObject);
        }

        return null;
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

        // t = (a - o) . N / (D . N)
        float intersectionDistance = ((ray.origin().to(normal.scale(distance)).dot(normal)) / iDotN);

        // Plane is behind ray starting point
        if (intersectionDistance <= 0.0f) {
            return null;
        }

        return new HitInfo(intersectionDistance, this, normalAt(ray.getPositionAt(intersectionDistance)), frontFaceIntersection);
    }

    @Override
    public Vec4 getUVCoords(Vec4 position) {
        float u = this.e1.dot(position);
        float v = this.e2.dot(position);
        u = u - (int) u;
        v = v - (int) v;
        if (u < 0.0f) u = u + 1.0f;
        if (v < 0.0f) v = v + 1.0f;
        return new Vec4(u, v);
    }

    @Override
    public Vec4 normalAt(final Vec4 position) {
        return normal;
    }

    private DivisorDirection checkTriangle(final Triangle triangle) {
        final Vec4 lNorm = normal.scale(distance);
        final double d1 = normal.dot(lNorm.to(triangle.getP1()));
        final double d2 = normal.dot(lNorm.to(triangle.getP2()));
        final double d3 = normal.dot(lNorm.to(triangle.getP3()));
        if ((Math.signum(d1) == Math.signum(d2)) && (Math.signum(d1) == Math.signum(d3))) {
            // If all distances have the same sign, then the triangle does not collide with the plane
            if (Math.signum(d1) == 1.0) {
                return DivisorDirection.RIGHT;
            } else {
                return DivisorDirection.LEFT;
            }
        } else {
            return DivisorDirection.BOTH;
        }
    }

    private DivisorDirection checkSphere(final Sphere sphere) {
        Vec4 pointOnPlane = this.normal.scale(this.distance);

        final float distance = (pointOnPlane.to(sphere.getOrigin()).dot(this.normal) / sphere.getOrigin().length());
        if (abs(distance) <= sphere.getRadius()) {
            return DivisorDirection.BOTH;
        } else {
            if (distance > 0) {
                return DivisorDirection.LEFT;
            } else {
                return DivisorDirection.RIGHT;
            }
        }
    }
}
