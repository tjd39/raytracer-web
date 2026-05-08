package org.raytracerweb.scene.objects.mesh;

import static org.raytracerweb.scene.objects.SurfaceNormals.NORMAL;

import java.util.Arrays;
import java.util.List;

import org.raytracerweb.geometry.rotation.NoOpRotation;
import org.raytracerweb.geometry.rotation.RotationMatrix;
import org.raytracerweb.geometry.vector.Vec4;
import org.raytracerweb.preview.colour.ColourPreset;
import org.raytracerweb.scene.objects.SurfaceNormals;
import org.raytracerweb.scene.objects.Triangle;
import org.raytracerweb.scene.objects.material.Material;

public class TXTMeshLoader extends AbstractMeshLoader {

    public TXTMeshLoader(Mesh mesh, Material material) {
        this(mesh, material, 1f, new Vec4(), new NoOpRotation(), NORMAL);
    }

    public TXTMeshLoader(final Mesh mesh, final Material material, final float scale,final Vec4 offset,final RotationMatrix rotation, final SurfaceNormals normals) {
        super(mesh, material, scale, offset, rotation, normals);
    }

    @Override
    void processLine(String line) {
        List<Float> points = Arrays.stream(line.split(",")).map(Float::valueOf).toList();
        if (points.size() != 9) {
            throw new IllegalArgumentException("Invalid txt mesh");
        }

        final Vec4 p1 = new Vec4(points.get(0), points.get(1), points.get(2)).scale(scaleFactor).rotate(rotation).plus(offset);
        final Vec4 p2 = new Vec4(points.get(3), points.get(4), points.get(5)).scale(scaleFactor).rotate(rotation).plus(offset);
        final Vec4 p3 = new Vec4(points.get(6), points.get(7), points.get(8)).scale(scaleFactor).rotate(rotation).plus(offset);

        final Triangle triangle = new Triangle(material == null ? Material.nonEmissive(ColourPreset.randomNotDark()) : material, p1, p2, p3, true, normals);
        triangles.add(triangle);
    }

    @Override
    MeshType getType() {
        return MeshType.TXT;
    }
}
