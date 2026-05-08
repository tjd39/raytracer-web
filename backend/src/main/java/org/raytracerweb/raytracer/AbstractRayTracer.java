package org.raytracerweb.raytracer;

import static org.raytracerweb.math.MathHelper.clamp;
import static org.raytracerweb.math.MathHelper.lerp;

import java.util.Objects;

import org.raytracerweb.geometry.vector.Vec4;
import org.raytracerweb.preview.canvas.Canvas;
import org.raytracerweb.preview.canvas.Pixel;
import org.raytracerweb.preview.canvas.SubPixel;
import org.raytracerweb.preview.colour.Colour;
import org.raytracerweb.preview.graphics.GraphicsSettings;
import org.raytracerweb.ray.HitInfo;
import org.raytracerweb.ray.Ray;
import org.raytracerweb.scene.Scene;
import org.raytracerweb.scene.camera.Camera;
import org.raytracerweb.scene.objects.material.Material;

public abstract class AbstractRayTracer implements IRayTracer {
    protected static float BIAS = 10E-4f;
    protected Scene scene;
    protected Camera camera;
    protected Canvas canvas;
    protected GraphicsSettings graphicsSettings;

    private Ray[] primaryRayCache;

    public AbstractRayTracer(Scene scene, Camera camera, Canvas canvas, GraphicsSettings graphicsSettings) {
        setScene(scene);
        setCamera(camera);
        setCanvas(canvas);
        setGraphicsSettings(graphicsSettings);
    }

    @Override
    public Scene scene() {
        return scene;
    }

    @Override
    public Camera camera() {
        return camera;
    }

    @Override
    public Canvas canvas() {
        return canvas;
    }

    @Override
    public GraphicsSettings graphicsSettings() {
        return graphicsSettings;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
        if (this.graphicsSettings != null) this.primaryRayCache = buildPrimaryRayCache();
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    public void setGraphicsSettings(GraphicsSettings graphicsSettings) {
        this.graphicsSettings = graphicsSettings;
        if (this.camera != null) this.primaryRayCache = buildPrimaryRayCache();
    }

    private Ray[] buildPrimaryRayCache() {
        int w = graphicsSettings.imageWidth();
        int h = graphicsSettings.imageHeight();
        int aa = graphicsSettings.antiAlias();
        Ray[] cache = new Ray[w * h * aa * aa];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Pixel pixel = new Pixel(x, y);
                for (int py = 0; py < aa; py++) {
                    for (int px = 0; px < aa; px++) {
                        cache[(y * w + x) * aa * aa + py * aa + px] =
                                createPrimaryRay(new SubPixel(pixel, px, py));
                    }
                }
            }
        }
        return cache;
    }

    @Override
    public void pixelTrace(Pixel pixel) {
        Colour pixelColour = pixelTraceColour(pixel);
        if (graphicsSettings().accumulateImage()) {
            canvas.addPixelValue(pixel.x(), pixel.y(), pixelColour);
        } else {
            canvas.setPixelValue(pixel.x(), pixel.y(), pixelColour);
        }
    }

    @Override
    public Colour pixelTraceColour(Pixel pixel) {
        int antiAlias = graphicsSettings().antiAlias();
        Colour pixelColour = new Colour(0f, 0f, 0f);

        for (int px = 0; px < antiAlias; px++) {
            for (int py = 0; py < antiAlias; py++) {
                // Call to implementation-specific logic for ray-cast
                rayCast(getPixelRay(new SubPixel(pixel, px, py)), pixelColour);
            }
        }
        pixelColour.scale(1.0f / (antiAlias * antiAlias));
        return pixelColour;
    }

    @Override
    public HitInfo checkIntersections(Ray ray, int level) {
        return calculateIntersection(ray);
    }

    protected HitInfo calculateIntersection(Ray ray) {
        // Perform ray intersection tests
        HitInfo closestIntersection = null;
        float distance = Float.MAX_VALUE;
        for (int objectIndex = 0; objectIndex < scene.sceneObjects().length; objectIndex++) {
            HitInfo hitInfo = scene.sceneObjects()[objectIndex].checkIntersection(ray);
            if (hitInfo != null) {
                if (hitInfo.distance() < distance) {
                    distance = hitInfo.distance();
                    closestIntersection = hitInfo;
                }
            }
        }
        return closestIntersection;
    }

    @Override
    public Ray getPixelRay(Pixel pixel) {
        return getPixelRay(new SubPixel(pixel, 0, 0));
    }

    private Ray getPixelRay(SubPixel subPixel) {
        int w = graphicsSettings.imageWidth();
        int aa = graphicsSettings.antiAlias();
        int x = subPixel.pixel().x(), y = subPixel.pixel().y();
        return primaryRayCache[(y * w + x) * aa * aa + subPixel.py() * aa + subPixel.px()];
    }

    private Ray createPrimaryRay(SubPixel subPixel) {
        Vec4 pixelPosition = camera.getSubPixelPosition(subPixel.pixel(), graphicsSettings().imageWidth(), graphicsSettings().imageHeight(), subPixel.px(), subPixel.py(), graphicsSettings().antiAlias());
        return new Ray(camera.getPosition(), camera.getPosition().to(pixelPosition));
    }

    protected abstract void rayCast(Ray ray, Colour pixelColour);

    protected Ray reflectionRay(Ray ray, HitInfo hitInfo, Material objectMaterial) {
        return new Ray(
                ray.getPositionAt(hitInfo.distance()).plus(hitInfo.normal().scale(BIAS)),
                reflectionDirection(hitInfo.normal(), ray.direction(), objectMaterial.getRoughness())
        );
    }

    protected Vec4 reflectionDirection(Vec4 normal, Vec4 incident, float roughness) {
        // Bucketed logic in an attempt to prevent wasted computation
        if (roughness == 0f) {
            return mirrorReflection(normal, incident);
        }

        if (roughness == 1f) {
            return Vec4.randomHemisphere();
        }

        if (roughness < 0.5f) {
            return Vec4.randomHemisphere().lerp(normal.cosinePowerSample(lerp(0f, 1000f, roughness * 2f)), roughness * 2f);
        } else {
            return normal.cosinePowerSample(lerp(0f, 1000f, roughness * 2f)).lerp(mirrorReflection(normal, incident), roughness * 2f);
        }
    }

    private Vec4 mirrorReflection(Vec4 normal, Vec4 incident) {
        return normal.scale(2.0f * normal.dot(incident)).to(incident).normalise();
    }

    protected Ray refractionRay(Ray ray, HitInfo hitInfo, Material objectMaterial) {
        return new Ray(
                ray.getPositionAt(hitInfo.distance()).plus(hitInfo.normal().scale(hitInfo.frontFaceIntersection() ? -BIAS : BIAS)),
                refractedDirection(ray.direction(),
                        hitInfo.normal(),
                        hitInfo.object().getMaterial().getIndexOfRefraction(),
                        objectMaterial.getRoughness(),
                        hitInfo.frontFaceIntersection())
        );
    }

    private Vec4 refractedDirection(Vec4 incidence, Vec4 hitNormal, float indexOfRefraction, float roughness, boolean frontFaceIntersection) {
        float eta = frontFaceIntersection ? 1f / indexOfRefraction : indexOfRefraction;
        Vec4 normal = frontFaceIntersection ? hitNormal.reverse() : hitNormal;
        float dotNI = normal.dot(incidence);
        final float k = 1f - eta * eta * (1f - dotNI * dotNI);
        if (k < 0) {
            return reflectionDirection(hitNormal, incidence, roughness);
        }
        return normal.scale(eta * dotNI - (float) Math.sqrt(k))
                .plus(incidence.scale(eta)).normalise();
    }

    public void validateCache() {
        // primary rays are pre-computed at construction time; nothing to do
    }
}
