package org.raytracerweb.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.raytracerweb.api.dto.CameraDto;
import org.raytracerweb.api.dto.CustomSceneDto;
import org.raytracerweb.api.dto.SceneDescriptorDto;
import org.raytracerweb.geometry.Axis;
import org.raytracerweb.geometry.vector.Vec4;
import org.raytracerweb.preview.canvas.Canvas;
import org.raytracerweb.preview.colour.Colour;
import org.raytracerweb.preview.colour.ColourPreset;
import org.raytracerweb.preview.graphics.GraphicsSettings;
import org.raytracerweb.raytracer.IRayTracer;
import org.raytracerweb.raytracer.LEMRayTracer;
import org.raytracerweb.raytracer.engine.RenderEngine;
import org.raytracerweb.raytracer.engine.core.CommandHandle;
import org.raytracerweb.scene.SceneBuilder;
import org.raytracerweb.scene.ScenePresets;
import org.raytracerweb.scene.SceneResult;
import org.raytracerweb.scene.camera.Camera;
import org.raytracerweb.scene.objects.material.IridescentMaterial;
import org.raytracerweb.scene.objects.material.KaleidoscopicMaterial;
import org.raytracerweb.scene.objects.material.Material;
import org.raytracerweb.scene.objects.material.SimpleMaterial;
import org.raytracerweb.scene.objects.material.texture.ImageTextureMaterial;
import org.raytracerweb.scene.objects.material.texture.Texture;
import org.springframework.stereotype.Service;

@Service
public class RenderService {

    private final RenderEngine renderEngine = new RenderEngine();
    private final Map<String, RenderJob> jobs = new ConcurrentHashMap<>();

    public RenderJob submit(SceneDescriptorDto sceneDescriptor, int width, int height,
            int renderLevels, int antiAlias, int sampleCount) {
        SceneResult sceneResult = ScenePresets.forName(sceneDescriptor.name());
        Camera camera = sceneDescriptor.defaultCamera() != null
                ? sceneDescriptor.defaultCamera().toCamera()
                : sceneResult.camera();

        final boolean accumulate = sampleCount > 1;
        GraphicsSettings settings = new GraphicsSettings(width, height, renderLevels, antiAlias, accumulate, false);
        Canvas canvas = new Canvas(width, height);

        IRayTracer rayTracer = new LEMRayTracer(sceneResult.scene(), camera, canvas, settings);
        RenderJob job = new RenderJob(rayTracer, sampleCount);
        jobs.put(job.getId(), job);

        job.setStatus(RenderJob.Status.RUNNING);
        CommandHandle handle;
        if (accumulate) {
            handle = renderEngine.executeAccumulatedTrace(
                    rayTracer, sampleCount,
                    () -> job.setStatus(RenderJob.Status.COMPLETE),
                    () -> job.setStatus(RenderJob.Status.ERROR),
                    e -> {
                        job.setErrorMessage(e.getMessage());
                        job.setStatus(RenderJob.Status.ERROR);
                    },
                    job::incrementPixel,
                    job::incrementSample);
        } else {
            handle = renderEngine.executeSingleTrace(
                    rayTracer,
                    () -> job.setStatus(RenderJob.Status.COMPLETE),
                    () -> job.setStatus(RenderJob.Status.ERROR),
                    e -> {
                        job.setErrorMessage(e.getMessage());
                        job.setStatus(RenderJob.Status.ERROR);
                    },
                    job::incrementPixel);
        }
        job.setCommandHandle(handle);

        return job;
    }

    public SceneResult buildCustomScene(CustomSceneDto dto, CameraDto cameraDto) {
        Camera camera = cameraDto != null
                ? cameraDto.toCamera()
                : new Camera(new Vec4(3f, 3f, 3f));

        SceneBuilder builder = new SceneBuilder();

        // Sky
        if (dto.sky() != null) {
            String skyType = dto.sky().type();
            if ("solid".equals(skyType) && dto.sky().color() != null) {
                builder.sky(toColour(dto.sky().color()));
            } else if ("black".equals(dto.sky().preset())) {
                builder.sky(ColourPreset.BLACK.get());
            }
            // "default" (sky blue) is the SceneBuilder default — no call needed
        }

        // Light
        if (dto.light() != null) {
            switch (dto.light().type() == null ? "" : dto.light().type()) {
                case "sun"    -> builder.sun();
                case "sunset" -> builder.sunset();
                // "none" → no light added
            }
        }

        // Floor
        if (dto.floor() != null) {
            CustomSceneDto.FloorDto f = dto.floor();
            float roughness = f.roughness();
            float height = f.height();
            Material floorMaterial;
            if ("solid".equals(f.type()) && f.color() != null) {
                floorMaterial = new SimpleMaterial(toColour(f.color()), ColourPreset.BLACK.get(), roughness, 0f, 1f);
            } else {
                Texture tex = textureForKey(f.texture());
                floorMaterial = tex != null
                        ? new ImageTextureMaterial(tex, 1f, roughness, 0f, 1f)
                        : new ImageTextureMaterial(Texture.CHEQUERBOARD, 1f, roughness, 0f, 1f);
            }
            builder.plane(floorMaterial, new Vec4(Axis.Y), height);
        }

        // Spheres
        if (dto.spheres() != null) {
            for (CustomSceneDto.SphereDto s : dto.spheres()) {
                Material mat = buildSphereMaterial(s);
                builder.sphere(mat, new Vec4(s.x(), s.y(), s.z()), s.radius());
            }
        }

        return new SceneResult(builder.build(), camera);
    }

    private Material buildSphereMaterial(CustomSceneDto.SphereDto s) {
        String type = s.materialType() == null ? "simple" : s.materialType();
        return switch (type) {
            case "iridescent"   -> new IridescentMaterial(s.roughness(), s.transparency(), s.indexOfRefraction() > 0 ? s.indexOfRefraction() : 1f);
            case "kaleidoscopic" -> new KaleidoscopicMaterial();
            default -> {
                Colour absorption = s.color() != null ? toColour(s.color()) : ColourPreset.WHITE.get();
                Colour emission   = s.emission() != null ? toColour(s.emission()) : ColourPreset.BLACK.get();
                yield new SimpleMaterial(absorption, emission, s.roughness(), s.transparency(), s.indexOfRefraction() > 0 ? s.indexOfRefraction() : 1f);
            }
        };
    }

    private static Colour toColour(float[] rgb) {
        return new Colour(rgb[0], rgb[1], rgb[2]);
    }

    private static Texture textureForKey(String key) {
        if (key == null) return null;
        return switch (key.toLowerCase()) {
            case "chequerboard"          -> Texture.CHEQUERBOARD;
            case "mc_grass"              -> Texture.MC_GRASS;
            case "mc_cobble"             -> Texture.MC_COBBLE;
            case "mc_dirt"               -> Texture.MC_DIRT;
            case "greyscale_noise_random" -> Texture.GREYSCALE_NOISE_RANDOM;
            case "rainbow_noise_random"  -> Texture.RAINBOW_NOISE_RANDOM;
            case "debug"                 -> Texture.DEBUG;
            default                      -> null;
        };
    }

    public RenderJob submitCustom(CustomSceneDto sceneDto, CameraDto cameraDto,
            int width, int height, int renderLevels, int antiAlias, int sampleCount) {
        SceneResult sceneResult = buildCustomScene(sceneDto, cameraDto);
        Camera camera = sceneResult.camera();

        final boolean accumulate = sampleCount > 1;
        GraphicsSettings settings = new GraphicsSettings(width, height, renderLevels, antiAlias, accumulate, false);
        Canvas canvas = new Canvas(width, height);

        IRayTracer rayTracer = new LEMRayTracer(sceneResult.scene(), camera, canvas, settings);
        RenderJob job = new RenderJob(rayTracer, sampleCount);
        jobs.put(job.getId(), job);

        job.setStatus(RenderJob.Status.RUNNING);
        CommandHandle handle;
        if (accumulate) {
            handle = renderEngine.executeAccumulatedTrace(
                    rayTracer, sampleCount,
                    () -> job.setStatus(RenderJob.Status.COMPLETE),
                    () -> job.setStatus(RenderJob.Status.ERROR),
                    e -> { job.setErrorMessage(e.getMessage()); job.setStatus(RenderJob.Status.ERROR); },
                    job::incrementPixel,
                    job::incrementSample);
        } else {
            handle = renderEngine.executeSingleTrace(
                    rayTracer,
                    () -> job.setStatus(RenderJob.Status.COMPLETE),
                    () -> job.setStatus(RenderJob.Status.ERROR),
                    e -> { job.setErrorMessage(e.getMessage()); job.setStatus(RenderJob.Status.ERROR); },
                    job::incrementPixel);
        }
        job.setCommandHandle(handle);
        return job;
    }

    public RenderJob get(String id) {
        return jobs.get(id);
    }

    public boolean cancel(String id) {
        RenderJob job = jobs.get(id);
        if (job == null) return false;
        CommandHandle handle = job.getCommandHandle();
        if (handle != null) handle.cancel();
        return true;
    }
}
