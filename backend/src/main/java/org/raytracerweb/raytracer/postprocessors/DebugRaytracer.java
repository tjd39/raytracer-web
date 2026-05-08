package org.raytracerweb.raytracer.postprocessors;

import org.raytracerweb.preview.canvas.Canvas;
import org.raytracerweb.preview.colour.Colour;
import org.raytracerweb.preview.graphics.GraphicsSettings;
import org.raytracerweb.ray.Ray;
import org.raytracerweb.raytracer.AbstractRayTracer;
import org.raytracerweb.scene.Scene;
import org.raytracerweb.scene.camera.Camera;

public class DebugRaytracer extends AbstractRayTracer {
    public DebugRaytracer(Scene scene, Camera camera, Canvas canvas, GraphicsSettings graphicsSettings) {
        super(scene, camera, canvas, graphicsSettings);
    }

    @Override
    protected void rayCast(Ray ray, Colour pixelColour) {
        // No op: this class is used for alternative means
    }
}
