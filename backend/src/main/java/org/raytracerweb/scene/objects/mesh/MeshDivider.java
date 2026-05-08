package org.raytracerweb.scene.objects.mesh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.raytracerweb.geometry.vector.Vec4;
import org.raytracerweb.math.Tuple;
import org.raytracerweb.scene.objects.AABB;
import org.raytracerweb.scene.objects.Plane;
import org.raytracerweb.scene.objects.Triangle;
import org.raytracerweb.scene.objects.polygon.MeshObject;

public class MeshDivider {
    private final List<Vec4> divisorNormals = List.of(
            new Vec4(1.0f, 0.0f, 0.0f),
            new Vec4(0.0f, 1.0f, 0.0f),
            new Vec4(0.0f, 0.0f, 1.0f)
    );

    public List<MeshObject> splitMeshObject(final MeshObject meshObject, final Integer level) {
        return splitMeshObjects(level, Collections.singletonList(meshObject));
    }

    private List<MeshObject> splitMeshObjects(Integer level, List<MeshObject> meshObjects) {
        if (level <= 0) {
            // Return the original mesh object list, i.e. no further division
            return meshObjects;
        } else {
            // For each meshObject, split and add to the sub mesh object list
            final List<MeshObject> subMeshObjects = new ArrayList<>();
            for (final MeshObject meshObject : meshObjects) {
                subMeshObjects.addAll(performMeshObjectSplit(meshObject, divisorNormals.get(Math.floorMod(level, divisorNormals.size()))));
            }

            // Recursively call to split the sub objects further
            return splitMeshObjects(level - 1, subMeshObjects);
        }
    }

    private List<MeshObject> performMeshObjectSplit(final MeshObject meshObject, final Vec4 divisorNormal) {
        // Get bounding box for mesh object and find the centre
        final List<MeshObject> out = new ArrayList<>();
        final AABB aabb = meshObject.getAABB();
        final Plane divisor = new Plane(meshObject.getMaterial(), new Vec4(aabb.longestAxis()), aabb.center().mask(aabb.longestAxis()).length(), false);
        Tuple<List<Triangle>, List<Triangle>> splitTriangles = getSplitTrianglesList(meshObject, divisor);

        out.add(new MeshObject(meshObject.getMaterial(), splitTriangles.getA(), meshObject.isRender()));
        out.add(new MeshObject(meshObject.getMaterial(), splitTriangles.getB(), meshObject.isRender()));

        return out;
    }

    private Tuple<List<Triangle>, List<Triangle>> getSplitTrianglesList(final MeshObject meshObject, final Plane divisor) {
        // Build sub-objects
        final List<Triangle> left = new ArrayList<>();
        final List<Triangle> right = new ArrayList<>();
        for (final Triangle triangle : meshObject.getTriangleList()) {
            switch (divisor.checkObject(triangle)) {
            case LEFT:
                left.add(triangle);
                break;

            case RIGHT:
            case BOTH:
                right.add(triangle);
                break;
            }
        }
        return new Tuple<>(left, right);
    }
}
