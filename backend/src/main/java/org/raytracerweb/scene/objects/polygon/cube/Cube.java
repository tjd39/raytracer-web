package org.raytracerweb.scene.objects.polygon.cube;

import org.raytracerweb.geometry.vector.Vec4;
import org.raytracerweb.scene.objects.SurfaceNormals;
import org.raytracerweb.scene.objects.material.Material;

public class Cube extends AbstractCube {
    public Cube(Vec4 centre, Vec4 v1, Vec4 v2, Vec4 v3, boolean render, Material... material) {
        super(centre, v1, v2, v3, render, material);
    }

    public Cube(Vec4 centre, Vec4 v1, Vec4 v2, Vec4 v3, boolean render, SurfaceNormals normals, Material... material) {
        super(centre, v1, v2, v3, render, normals, material);
    }
}
