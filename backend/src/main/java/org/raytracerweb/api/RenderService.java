package org.raytracerweb.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.raytracerweb.api.dto.CameraDto;
import org.raytracerweb.api.dto.SceneDescriptorDto;
import org.raytracerweb.preview.canvas.Canvas;
import org.raytracerweb.preview.graphics.GraphicsSettings;
import org.raytracerweb.raytracer.IRayTracer;
import org.raytracerweb.raytracer.LEMRayTracer;
import org.raytracerweb.raytracer.engine.RenderEngine;
import org.raytracerweb.raytracer.engine.core.CommandHandle;
import org.raytracerweb.scene.ScenePresets;
import org.raytracerweb.scene.SceneResult;
import org.raytracerweb.scene.camera.Camera;
import org.springframework.stereotype.Service;

@Service
public class RenderService {

    private final RenderEngine renderEngine = new RenderEngine();
    private final Map<String, RenderJob> jobs = new ConcurrentHashMap<>();

    public RenderJob submit(SceneDescriptorDto sceneDescriptor, int width, int height, int renderLevels, int antiAlias) {
        SceneResult sceneResult = ScenePresets.forName(sceneDescriptor.name());
        Camera camera = sceneDescriptor.defaultCamera() != null
                ? sceneDescriptor.defaultCamera().toCamera()
                : sceneResult.camera();

        GraphicsSettings settings = new GraphicsSettings(width, height, renderLevels, antiAlias, false, false);
        Canvas canvas = new Canvas(width, height);

        IRayTracer rayTracer = new LEMRayTracer(sceneResult.scene(), camera, canvas, settings);
        RenderJob job = new RenderJob(rayTracer);
        jobs.put(job.getId(), job);

        job.setStatus(RenderJob.Status.RUNNING);
        CommandHandle handle = renderEngine.executeSingleTrace(
                rayTracer,
                () -> job.setStatus(RenderJob.Status.COMPLETE),
                () -> job.setStatus(RenderJob.Status.ERROR),
                e -> {
                    job.setErrorMessage(e.getMessage());
                    job.setStatus(RenderJob.Status.ERROR);
                },
                job::incrementPixel);
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
