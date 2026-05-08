package org.raytracerweb.scene;

import java.util.Arrays;
import java.util.Objects;

import org.raytracerweb.preview.colour.Colour;
import org.raytracerweb.scene.lights.SceneLight;
import org.raytracerweb.scene.objects.SceneObject;

public record Scene(Colour sky, SceneObject[] sceneObjects, SceneLight[] sceneLights) {
    public static SceneBuilder builder() {
        return new SceneBuilder();
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.stream(sceneObjects).toArray());
    }
}
