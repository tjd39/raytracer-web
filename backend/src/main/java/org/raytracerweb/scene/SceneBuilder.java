package org.raytracerweb.scene;

import static org.raytracerweb.geometry.Axis.Y;
import static org.raytracerweb.preview.colour.ColourPreset.GREEN;
import static org.raytracerweb.preview.colour.ColourPreset.ORANGE;
import static org.raytracerweb.preview.colour.ColourPreset.RED;
import static org.raytracerweb.preview.colour.ColourPreset.SUNSET;
import static org.raytracerweb.preview.colour.ColourPreset.WHITE;
import static org.raytracerweb.preview.colour.ColourPreset.YELLOW;
import static org.raytracerweb.scene.objects.SurfaceNormals.NORMAL;

import java.util.ArrayList;
import java.util.List;

import org.raytracerweb.geometry.rotation.NoOpRotation;
import org.raytracerweb.geometry.rotation.RotationMatrix;
import org.raytracerweb.geometry.vector.Vec4;
import org.raytracerweb.preview.colour.Colour;
import org.raytracerweb.preview.colour.ColourPreset;
import org.raytracerweb.scene.camera.Camera;
import org.raytracerweb.scene.lights.SceneLight;
import org.raytracerweb.scene.objects.Plane;
import org.raytracerweb.scene.objects.SceneObject;
import org.raytracerweb.scene.objects.Sphere;
import org.raytracerweb.scene.objects.SurfaceNormals;
import org.raytracerweb.scene.objects.Triangle;
import org.raytracerweb.scene.objects.material.Material;
import org.raytracerweb.scene.objects.material.texture.ImageTextureMaterial;
import org.raytracerweb.scene.objects.material.texture.Texture;
import org.raytracerweb.scene.objects.mesh.Mesh;
import org.raytracerweb.scene.objects.mesh.MeshLoader;
import org.raytracerweb.scene.objects.mesh.OBJMeshLoader;
import org.raytracerweb.scene.objects.mesh.TXTMeshLoader;
import org.raytracerweb.scene.objects.polygon.cube.Cube;

public class SceneBuilder {
    private Colour sky = ColourPreset.SKY.get();
    private final List<SceneObject> sceneObjects = new ArrayList<>();
    private final List<SceneLight> sceneLights = new ArrayList<>();

    public SceneBuilder add(final SceneObject object) {
        sceneObjects.add(object);
        return this;
    }

    public SceneBuilder add(final SceneLight light) {
        sceneLights.add(light);
        return this;
    }

    public SceneBuilder sky(final Colour sky) {
        this.sky = sky;
        return this;
    }

    public SceneBuilder sphere(final Material material, final Vec4 position, final float radius) {
        return add(new Sphere(material, position, radius, true));
    }

    public SceneBuilder triangle(final Material material, final Vec4 p1, final Vec4 p2, final Vec4 p3) {
        return add(new Triangle(material, p1, p2, p3, true));
    }

    public SceneBuilder plane(final Material material, final Vec4 normal, final float distance) {
        return add(new Plane(material, normal, distance, true));
    }

    public SceneBuilder cameraCube(final Camera camera, final float scale) {
        return add(new Cube(camera.getPosition().plus(camera.getLook().scale(2.45f * scale)), camera.getLook().scale(2.5f * scale), camera.getPan(), camera.getUp(), true, NORMAL,
                Material.emissive(WHITE),
                Material.nonEmissive(RED),
                Material.nonEmissive(WHITE),
                Material.nonEmissive(WHITE),
                Material.emissive(WHITE),
                Material.nonEmissive(GREEN)));
    }

    public SceneBuilder flashlight(final Camera camera, final Material material, final float radius) {
        return sphere(material, camera.getPosition().plus(camera.getLook().reverse().scale(radius + 0.5f)), radius);
    }

    public SceneBuilder cube(final Material material, final Vec4 position, final Vec4 v1, final Vec4 v2, final Vec4 v3) {
        return add(new Cube(position, v1, v2, v3, true, material));
    }

    public SceneBuilder floor() {
        return floor(new ImageTextureMaterial(Texture.CHEQUERBOARD, 1f));
    }

    public SceneBuilder floor(final Material material) {
        return plane(material, new Vec4(Y), 0f);
    }

    public SceneBuilder sun() {
        return sphere(Material.emissive(WHITE), new Vec4(0f, 70f, 0f), 50f);
    }

    public SceneBuilder sunset() {
        return sphere(Material.emissive(SUNSET), new Vec4(70f, 0f, 70f), 50f);
    }

    public SceneBuilder mesh(final Mesh mesh, final Material material) {
        return mesh(mesh, material, 1f, new Vec4(), new NoOpRotation(), NORMAL);
    }

    public SceneBuilder mesh(final Mesh mesh, final Material material, final float scaleFactor, final Vec4 offset, final RotationMatrix rotation, final SurfaceNormals normals) {
        MeshLoader meshLoader = new TXTMeshLoader(mesh, material, scaleFactor, offset, rotation, normals);
        meshLoader.loadMesh().forEach(this::add);
        return this;
    }

    public SceneBuilder mesh_obj(final Mesh mesh, final Material material) {
        return mesh_obj(mesh, material, 1f, new Vec4(), new NoOpRotation(), NORMAL);
    }

    public SceneBuilder mesh_obj(final Mesh mesh, final Material material, final float scaleFactor, final Vec4 offset, final RotationMatrix rotation, final SurfaceNormals normals) {
        MeshLoader meshLoader = new OBJMeshLoader(mesh, material, scaleFactor, offset, rotation, normals);
        meshLoader.loadMesh().forEach(this::add);
        return this;
    }

    public SceneBuilder cameraBox(final Camera camera) {
        final float boxSize = 2f;
        plane(new ImageTextureMaterial(Texture.CHEQUERBOARD, 1f), camera.getUp(), -boxSize); // box bottom
        plane(Material.nonEmissive(RED), camera.getLook().reverse(), -boxSize * 5f); // box back
        plane(Material.nonEmissive(YELLOW), camera.getPan().reverse(), -boxSize); // box left
        plane(Material.nonEmissive(GREEN), camera.getPan(), -boxSize); // box right
        plane(Material.emissive(WHITE), camera.getUp().reverse(), -boxSize); // box top
        return this;
    }

    public Scene build() {
        return new Scene(sky, sceneObjects.toArray(new SceneObject[0]), sceneLights.toArray(new SceneLight[0]));
    }
}
