package org.raytracerweb.scene.objects.mesh;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.raytracerweb.geometry.rotation.RotationMatrix;
import org.raytracerweb.geometry.vector.Vec4;
import org.raytracerweb.scene.objects.SurfaceNormals;
import org.raytracerweb.scene.objects.Triangle;
import org.raytracerweb.scene.objects.material.Material;

public abstract class AbstractMeshLoader implements MeshLoader {
    protected final Mesh mesh;
    protected final Material material;
    protected final float scaleFactor;
    protected final Vec4 offset;
    protected final RotationMatrix rotation;
    protected final List<Triangle> triangles = new ArrayList<>();
    protected final SurfaceNormals normals;

    public AbstractMeshLoader(final Mesh mesh, final Material material, final float scaleFactor, final Vec4 offset, final RotationMatrix rotation, final SurfaceNormals normals) {
        this.mesh = mesh;
        this.material = material;
        this.scaleFactor = scaleFactor;
        this.offset = offset;
        this.rotation = rotation;
        this.normals = normals;
    }

    @Override
    public Material material() {
        return material;
    }

    @Override
    public List<Triangle> loadMesh() {
        return loadMeshInternal();
    }

    protected List<Triangle> loadMeshInternal() {
        triangles.clear();
        final String resourceName = mesh.name().toLowerCase() + "." + getType().toString();
        try (InputStream is = Mesh.class.getResourceAsStream(resourceName)) {
            if (is == null) {
                System.err.println("Mesh resource not found: " + resourceName);
                return triangles;
            }
            System.out.println("Reading mesh resource: \"" + resourceName + "\"");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = br.readLine();
            while (line != null) {
                processLine(line);
                line = br.readLine();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return triangles;
    }

    abstract void processLine(final String line);

    abstract MeshType getType();
}
