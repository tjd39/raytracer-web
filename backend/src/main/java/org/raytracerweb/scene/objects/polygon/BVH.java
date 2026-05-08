package org.raytracerweb.scene.objects.polygon;

import static org.raytracerweb.scene.objects.mesh.DivisorDirection.BOTH;
import static org.raytracerweb.scene.objects.mesh.DivisorDirection.LEFT;
import static org.raytracerweb.scene.objects.mesh.DivisorDirection.RIGHT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.raytracerweb.geometry.vector.Vec4;
import org.raytracerweb.preview.colour.ColourPreset;
import org.raytracerweb.ray.HitInfo;
import org.raytracerweb.ray.Ray;
import org.raytracerweb.scene.objects.AABB;
import org.raytracerweb.scene.objects.Plane;
import org.raytracerweb.scene.objects.Triangle;
import org.raytracerweb.scene.objects.material.Material;
import org.raytracerweb.scene.objects.mesh.DivisorDirection;

public class BVH extends Polygon {
    public BVH left;
    public BVH right;

    public BVH(final Material material, final List<Triangle> triangleList, final boolean render, final int depth) {
        super(material, render);

        // calculate AABB, then split it along its longest edge
        calculateAABB(triangleList);

        if (depth == 0) {
            this.triangleList = triangleList;
        } else {
            final Plane divisor = new Plane(null, new Vec4(aabb.longestAxis()), aabb.center().mask(aabb.longestAxis()).length(), false);
            final Map<DivisorDirection, List<Triangle>> tMap = triangleList.stream().collect(
                    HashMap::new,
                    (m, t) -> m.computeIfAbsent(divisor.checkObject(t), (d) -> new ArrayList<>()).add(t),
                    (m1, m2) -> Arrays.stream(DivisorDirection.values()).forEach(d -> m1.get(d).addAll(m2.get(d)))
            );
            tMap.computeIfAbsent(LEFT, (d) -> new ArrayList<>()).addAll(tMap.computeIfAbsent(BOTH, (d) -> new ArrayList<>()));
            tMap.computeIfAbsent(RIGHT, (d) -> new ArrayList<>()).addAll(tMap.get(BOTH));
            if (!tMap.get(LEFT).isEmpty() && !tMap.get(RIGHT).isEmpty()) {
                // Triangles for both sub-BVH objects
                this.left = new BVH(Material.nonEmissive(ColourPreset.debugNext()), tMap.get(LEFT), isRender(), depth - 1);
                this.right = new BVH(Material.nonEmissive(ColourPreset.debugNext()), tMap.get(RIGHT), isRender(), depth - 1);
            } else {
                // All triangles fit within only 1 sub-BVH.
                this.triangleList = triangleList;
            }
        }
    }

    public HitInfo aabbHit(final Ray ray) {
        return getAABB().checkIntersection(ray);
    }

    @Override
    public HitInfo checkIntersection(final Ray ray) {
        // First check own AABB
        if (aabbHit(ray) == null) {
            return null;
        }

        if (triangleList.isEmpty()) {
            final HitInfo lHit = left.aabbHit(ray);
            final HitInfo rHit = right.aabbHit(ray);

            // Doesn't hit left, return result of right check.
            if (lHit == null) return right.checkIntersection(ray);

            // Doesn't hit right, return result of left check.
            if (rHit == null) return left.checkIntersection(ray);

            // Hits both: check closest first then fallback to furthest.
            if (lHit.distance() < rHit.distance()) {
                HitInfo leftHit = left.checkIntersection(ray);
                return (leftHit != null)
                        ? leftHit
                        : right.checkIntersection(ray);
            } else {
                HitInfo rightHit = right.checkIntersection(ray);
                return (rightHit != null)
                        ? rightHit
                        : left.checkIntersection(ray);
            }
        } else {
            // delegate to Polygon method (check all triangles)
            return super.checkIntersection(ray);
        }
    }

    public List<AABB> getAABBs() {
        return Stream.concat(
                Stream.of(this.aabb),
                Stream.concat(
                        left != null ? left.getAABBs().stream() : Stream.empty(),
                        right != null ? right.getAABBs().stream() : Stream.empty()
                )
        ).toList();
    }

//    public List<Triangle> getTriangles() {
//        return Stream.concat(
//                this.triangleList.stream(),
//                Stream.concat(
//                        left != null ? left.getTriangles().stream() : Stream.empty(),
//                        right != null ? right.getTriangles().stream() : Stream.empty()
//                )
//        ).toList();
//    }
}
