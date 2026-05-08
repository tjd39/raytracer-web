package org.raytracerweb.scene;

import static org.raytracerweb.preview.colour.ColourPreset.BEIGE;
import static org.raytracerweb.preview.colour.ColourPreset.BLACK;
import static org.raytracerweb.preview.colour.ColourPreset.DARK_GREEN;
import static org.raytracerweb.preview.colour.ColourPreset.GREEN;
import static org.raytracerweb.preview.colour.ColourPreset.GREY;
import static org.raytracerweb.preview.colour.ColourPreset.PINK;
import static org.raytracerweb.preview.colour.ColourPreset.RED;
import static org.raytracerweb.preview.colour.ColourPreset.WHITE;
import static org.raytracerweb.preview.colour.ColourPreset.YELLOW;
import static org.raytracerweb.scene.objects.SurfaceNormals.IGNORE;
import static org.raytracerweb.scene.objects.SurfaceNormals.NORMAL;

import java.util.Iterator;
import java.util.List;

import org.raytracerweb.api.dto.CameraDto;
import org.raytracerweb.api.dto.SceneDescriptorDto;

import org.raytracerweb.geometry.Axis;
import org.raytracerweb.geometry.rotation.NoOpRotation;
import org.raytracerweb.geometry.rotation.RotationMatrix;
import org.raytracerweb.geometry.vector.Vec4;
import org.raytracerweb.preview.colour.ColourPreset;
import org.raytracerweb.scene.camera.Camera;
import org.raytracerweb.scene.objects.material.Material;
import org.raytracerweb.scene.objects.mesh.Mesh;

public class ScenePresets {

    private static final Camera DEFAULT_CAMERA = new Camera(new Vec4(3f, 3f, 3f));

    private static final List<String> PRESET_NAMES = List.of("simple_ball", "pool_balls", "sunset_balls", "cowboy_pingy");

    public static List<SceneDescriptorDto> listAll() {
        return PRESET_NAMES.stream().map(name -> {
            SceneResult r = forName(name);
            String spaced = name.replace("_", " ");
            String label = Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
            return new SceneDescriptorDto(name, label, CameraDto.from(r.camera()));
        }).toList();
    }

    public static SceneResult forName(String name) {
        return switch (name.toLowerCase()) {
            case "simple_ball"    -> simpleBall();
            case "pool_balls"     -> poolBalls();
            case "sunset_balls"   -> sunsetBalls();
            case "cowboy_pingy"   -> cowboyPingy();
            default               -> simpleBall();
        };
    }

    public static SceneResult simpleBall() {
        Scene scene = Scene.builder()
                .floor()
                .sun()
                .sphere(Material.nonEmissive(RED), new Vec4(0f, 0.5f, 0f), 0.5f)
                .build();
        return new SceneResult(scene, DEFAULT_CAMERA);
    }

    public static SceneResult poolBalls() {
        Camera camera = new Camera(new Vec4(1.2717688f, 2.9942496f, -2.157159f), new Vec4(-0.2117059f, -0.5769354f, 0.7888766f));
        SceneBuilder sceneBuilder = Scene.builder()
                .floor(Material.nonEmissive(GREEN))
                .sun();

        float r = 0.5f;
        Vec4 centre = new Vec4(0.0f, r, 0.0f);
        Vec4 v1 = new Vec4(-2.0f * r, 0.0f, 0.0f);
        Vec4 v2 = new Vec4(r, 0.0f, (float) (r / Math.tan(30 * (Math.PI / 180))));
        Iterator<ColourPreset> colours = List.of(RED, YELLOW, RED, RED, ColourPreset.BLACK, YELLOW, YELLOW, RED, YELLOW, RED, RED, YELLOW, YELLOW, RED, YELLOW).iterator();
        sceneBuilder.sphere(Material.nonEmissive(colours.next()), centre, r);
        for (int i = 1; i <= 4; i++) {
            Vec4 corner = centre.plus(v2.scale(i));
            sceneBuilder.sphere(Material.nonEmissive(colours.next()), corner, r);
            for (int j = 1; j <= i; j++) {
                sceneBuilder.sphere(Material.nonEmissive(colours.next()), corner.plus(v1.scale(j)), r);
            }
        }

        return new SceneResult(sceneBuilder.build(), camera);
    }

    public static SceneResult sunsetBalls() {
        Camera camera = new Camera(new Vec4(9.1991205f, 2.7947152f, 9.1991205f), new Vec4(-0.69746774f, -0.16455223f, -0.69746786f));
        float o = (float) Math.sqrt(2f) * 1.33f;
        Scene scene = Scene.builder()
                .floor(Material.nonEmissive(GREY))
                .sunset()
                .sky(BLACK.get())
                .sphere(Material.nonEmissive(ColourPreset.randomNotDark()), new Vec4(0f, 1f, 0f), 1f)
                .sphere(Material.nonEmissive(ColourPreset.randomNotDark()), new Vec4(-o, 1f, o), 1f)
                .sphere(Material.nonEmissive(ColourPreset.randomNotDark()), new Vec4(o, 1f, -o), 1f)
                .sphere(Material.nonEmissive(ColourPreset.randomNotDark()), new Vec4(-2f * o, 1f, 2f * o), 1f)
                .sphere(Material.nonEmissive(ColourPreset.randomNotDark()), new Vec4(2f * o, 1f, -2f * o), 1f)
                .build();
        return new SceneResult(scene, camera);
    }

    public static SceneResult cowboyPingy() {
        Camera camera = new Camera(new Vec4(3f, 3f, 3f));
        Scene scene = Scene.builder()
                .floor(Material.nonEmissive(GREEN))
                .sunset()
                .mesh_obj(Mesh.PINGY_WAVE, Material.nonEmissive(RED), 0.4f, new Vec4(0f, 0.095f, 0f), new NoOpRotation(), IGNORE)
                .mesh_obj(Mesh.COWBOY_HAT, Material.nonEmissive(BEIGE), 0.3f, new Vec4(0.7f, 1.65f, 0f), new RotationMatrix(new Vec4(Axis.Z), -90), IGNORE)
                .mesh(Mesh.PIG, Material.nonEmissive(PINK), 1.2f, new Vec4(0f, -1f, 0f), new RotationMatrix(new Vec4(Axis.Y), 197), NORMAL)
                .mesh(Mesh.TREE2, Material.nonEmissive(DARK_GREEN), 6.0f, new Vec4(-4f, -1f, -4f), new NoOpRotation(), IGNORE)
                .mesh(Mesh.TREE4, Material.nonEmissive(DARK_GREEN), 7.0f, new Vec4(0.5f, -1f, -5f), new NoOpRotation(), IGNORE)
                .flashlight(camera, Material.emissive(WHITE), 10f)
                .build();
        return new SceneResult(scene, camera);
    }
}
