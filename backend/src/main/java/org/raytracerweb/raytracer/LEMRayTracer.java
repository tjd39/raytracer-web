package org.raytracerweb.raytracer;

import static org.raytracerweb.geometry.vector.Vec4.randomHemisphere;
import static org.raytracerweb.math.MathHelper.lerp;

import java.util.concurrent.ThreadLocalRandom;

import org.raytracerweb.geometry.vector.Vec4;
import org.raytracerweb.preview.canvas.Canvas;
import org.raytracerweb.preview.colour.Colour;
import org.raytracerweb.preview.graphics.GraphicsSettings;
import org.raytracerweb.ray.HitInfo;
import org.raytracerweb.ray.Ray;
import org.raytracerweb.scene.Scene;
import org.raytracerweb.scene.camera.Camera;
import org.raytracerweb.scene.objects.material.Material;

/**
 * Ray Tracer implementation that utilises Light Emissivity Model.
 * This means there is no hard shadow logic, and soft shadows emerge out
 * of interactions between light sources and objects.
 */
public class LEMRayTracer extends AbstractRayTracer {
    public LEMRayTracer(final Scene scene, final Camera camera, final Canvas canvas, final GraphicsSettings graphicsSettings) {
        super(scene, camera, canvas, graphicsSettings);
    }

    @Override
    public void rayCast(final Ray ray, final Colour pixelColour) {
        final Colour emission = new Colour(0f, 0f, 0f);
        final Colour absorption = new Colour(1f, 1f, 1f);
        recursiveRayCast(ray, emission, absorption, graphicsSettings().renderLevels());
        pixelColour.plus(emission.multiply(absorption));
    }

    // https://viclw17.github.io/2018/08/05/raytracing-dielectric-materials
    private void recursiveRayCast(final Ray ray, final Colour emission, final Colour absorption, final int level) {
        if (level == 0) return;

        HitInfo hitInfo = checkIntersections(ray, level);
        if (hitInfo == null) {
            emission.plus(scene.sky());
            return;
        }

        absorption.scale(lerp(0.8f, 1.0f, (float) level / graphicsSettings().renderLevels()));

        Material objectMaterial = hitInfo.object().getMaterial();
        float transparency = objectMaterial.getTransparency();

        if (transparency == 0 || ThreadLocalRandom.current().nextFloat() > transparency) {
            if (hitInfo.frontFaceIntersection()) {
                Vec4 hitPoint = ray.getPositionAt(hitInfo.distance());
                float roughness = objectMaterial.getRoughness();
                // Stochastic branch selection: rough=1 → always diffuse, rough=0 → always specular
                final Ray nextRay;
                if (ThreadLocalRandom.current().nextFloat() < roughness) {
                    nextRay = new Ray(
                            hitPoint.plus(hitInfo.normal().scale(BIAS)),
                            hitInfo.normal().plus(randomHemisphere()).normalise());
                } else {
                    nextRay = new Ray(
                            hitPoint.plus(hitInfo.normal().scale(BIAS)),
                            reflectionDirection(hitInfo.normal(), ray.direction(), roughness));
                }
                recursiveRayCast(nextRay, emission, absorption, level - 1);
            }
        } else {
            Ray refractionRay = refractionRay(ray, hitInfo, objectMaterial);
            recursiveRayCast(refractionRay, emission, absorption, level - 1);
        }

        if (hitInfo.frontFaceIntersection()) {
            objectMaterial.absorption(absorption, ray, hitInfo);
            objectMaterial.emission(emission, ray, hitInfo);
        }
    }
}
