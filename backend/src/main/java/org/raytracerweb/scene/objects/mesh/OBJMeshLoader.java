package org.raytracerweb.scene.objects.mesh;

import static org.raytracerweb.scene.objects.SurfaceNormals.NORMAL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.raytracerweb.geometry.rotation.NoOpRotation;
import org.raytracerweb.geometry.rotation.RotationMatrix;
import org.raytracerweb.geometry.vector.Vec4;
import org.raytracerweb.preview.colour.ColourPreset;
import org.raytracerweb.scene.objects.SurfaceNormals;
import org.raytracerweb.scene.objects.Triangle;
import org.raytracerweb.scene.objects.material.Material;

public class OBJMeshLoader extends AbstractMeshLoader {
    private final List<Vec4> points = new ArrayList<>();

    public OBJMeshLoader(final Mesh mesh, final Material material) {
        this(mesh,material,1f, new Vec4(), new NoOpRotation(), NORMAL);
    }
    public OBJMeshLoader(final Mesh mesh, final Material material, final float scaleFactor, final Vec4 offset, final RotationMatrix rotation, final SurfaceNormals normals) {
        super(mesh, material, scaleFactor, offset, rotation, normals);
    }

    @Override
    public List<Triangle> loadMesh() {
        points.clear();
        return super.loadMeshInternal();
    }

    @Override
    void processLine(String line) {
        if (line.startsWith("v ")) {
            // Should read vertices first
            List<Float> coords = Arrays.stream(line.replace("v ", "").trim().split(" ")).map(Float::valueOf).toList();
            points.add(new Vec4(coords.get(0), coords.get(1), coords.get(2)).scale(scaleFactor).rotate(rotation).plus(offset));

        } else if (line.startsWith("f ")) {
            // Then faces (triangles)
            List<Integer> pointIndices = Arrays.stream(line.replace("f ", "").trim().split(" ")).map(s -> (Integer.parseInt(s) - 1)).toList();
            Vec4 p = points.get(pointIndices.get(0));
            Vec4 p1 = points.get(pointIndices.get(1));
            Vec4 p2 = points.get(pointIndices.get(2));

            Triangle t = new Triangle(material == null ? Material.nonEmissive(ColourPreset.randomNotDark()) : material, p, p1, p2, true, normals);
            triangles.add(t);

            if (pointIndices.size() == 4) {
                Vec4 p3 = points.get(pointIndices.get(3));
                Triangle t2 = new Triangle(material == null ? Material.nonEmissive(ColourPreset.randomNotDark()) : material, p, p2, p3, true, normals);
                triangles.add(t2);
            }
        }
    }

    @Override
    MeshType getType() {
        return MeshType.OBJ;
    }
}
