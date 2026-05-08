package org.raytracerweb.scene.objects.polygon;

import java.util.List;

import org.raytracerweb.ray.HitInfo;
import org.raytracerweb.ray.Ray;
import org.raytracerweb.scene.objects.Triangle;
import org.raytracerweb.scene.objects.material.Material;

public class MeshObject extends Polygon {

    public MeshObject(Material material, boolean render) {
        super(material, render);
    }

    public MeshObject(Material material, List<Triangle> triangleList, boolean render) {
        super(material, triangleList, render);
    }

    @Override
    public HitInfo checkIntersection(final Ray ray) {
        if (getAABB().checkIntersection(ray) != null) {
            return super.checkIntersection(ray);
        }
        return null;
    }

}
