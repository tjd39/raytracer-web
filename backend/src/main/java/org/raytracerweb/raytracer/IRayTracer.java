package org.raytracerweb.raytracer;

import org.raytracerweb.preview.canvas.Canvas;
import org.raytracerweb.preview.canvas.Pixel;
import org.raytracerweb.preview.colour.Colour;
import org.raytracerweb.preview.graphics.GraphicsSettings;
import org.raytracerweb.ray.HitInfo;
import org.raytracerweb.ray.Ray;
import org.raytracerweb.scene.Scene;
import org.raytracerweb.scene.camera.Camera;

public interface IRayTracer {
    Scene scene();

    Camera camera();

    Canvas canvas();

    GraphicsSettings graphicsSettings();

    void setScene(Scene scene);

    void setCamera(Camera camera);

    void setCanvas(Canvas canvas);

    void pixelTrace(final Pixel pixel);

    Colour pixelTraceColour(final Pixel pixel);

    Ray getPixelRay(final Pixel pixel);

    HitInfo checkIntersections(final Ray ray, final int level);

    void validateCache();
}


