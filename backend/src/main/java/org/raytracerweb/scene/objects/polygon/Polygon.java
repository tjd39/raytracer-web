package org.raytracerweb.scene.objects.polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.raytracerweb.geometry.vector.Vec4;
import org.raytracerweb.ray.HitInfo;
import org.raytracerweb.ray.Ray;
import org.raytracerweb.scene.objects.AABB;
import org.raytracerweb.scene.objects.SceneObject;
import org.raytracerweb.scene.objects.Triangle;
import org.raytracerweb.scene.objects.material.Material;

public class Polygon extends SceneObject {
    protected AABB aabb;
    protected List<Triangle> triangleList;

    public Polygon(final Material material, final boolean render) {
        this(material, new ArrayList<>(), render);
    }

    public Polygon(final Material material, final List<Triangle> triangleList, final boolean render) {
        super(material, render);
        this.triangleList = triangleList;
    }

    public AABB getAABB() {
        if (aabb == null) {
            calculateAABB(triangleList);
        }
        return aabb;
    }

    public List<Triangle> getTriangleList() {
        return triangleList;
    }

    @Override
    public HitInfo checkIntersection(Ray ray) {
        HitInfo closestIntersection = null;
        float distance = Float.MAX_VALUE;
        for (final Triangle triangle : triangleList) {
            HitInfo triangleHitInfo = triangle.checkIntersection(ray);
            if (triangleHitInfo != null) {
                if (triangleHitInfo.distance() < distance) {
                    distance = triangleHitInfo.distance();
                    closestIntersection = triangleHitInfo;
                }
            }
        }
        return closestIntersection;
    }

    @Override
    public Vec4 normalAt(final Vec4 position) {
        throw new UnsupportedOperationException("Polygon does not support normalAt operation.");
    }

    protected void calculateAABB(final List<Triangle> triangleList) {
        if (triangleList.isEmpty()) {
            return;
        }

        this.aabb = triangleList.stream()
                .flatMap(t -> Stream.of(t.getP1(), t.getP2(), t.getP3()))
                .collect(
                        AABB::new,
                        (aabb, point) -> aabb.update(point.x(), point.y(), point.z()),
                        AABB::combine
                );
    }
}
