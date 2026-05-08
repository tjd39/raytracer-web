package org.raytracerweb.scene.objects.mesh;

import java.util.List;

import org.raytracerweb.scene.objects.Triangle;
import org.raytracerweb.scene.objects.material.Material;

public interface MeshLoader {
    List<Triangle> loadMesh();

    Material material();
}
