package org.raytracerweb.api;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.raytracerweb.api.dto.CameraDto;
import org.raytracerweb.api.dto.CustomSceneDto;
import org.raytracerweb.api.dto.SceneDescriptorDto;
import org.raytracerweb.scene.ScenePresets;
import org.raytracerweb.scene.camera.Camera;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
public class RenderController {

    private final RenderService renderService;

    public RenderController(RenderService renderService) {
        this.renderService = renderService;
    }

    @GetMapping("/api/render/scenes")
    public List<SceneDescriptorDto> listScenes() {
        return ScenePresets.listAll();
    }

    @PostMapping("/api/render")
    public ResponseEntity<Map<String, String>> submitRender(@RequestBody RenderRequest request) {
        int width = request.width() > 0 ? request.width() : 500;
        int height = request.height() > 0 ? request.height() : 500;
        int renderLevels = request.renderLevels() > 0 ? request.renderLevels() : 4;
        int antiAlias = request.antiAlias() > 0 ? request.antiAlias() : 1;
        int sampleCount = request.sampleCount() > 1 ? request.sampleCount() : 1;

        RenderJob job = renderService.submit(request.scene(), width, height, renderLevels, antiAlias, sampleCount);
        return ResponseEntity.accepted().body(Map.of("jobId", job.getId()));
    }

    @GetMapping("/api/render/{id}/status")
    public ResponseEntity<Map<String, Object>> getStatus(@PathVariable String id) {
        RenderJob job = renderService.get(id);
        if (job == null) return ResponseEntity.notFound().build();

        Map<String, Object> body = new HashMap<>();
        body.put("status", job.getStatus().name());
        body.put("progress", job.getProgressPercent());
        body.put("completedSamples", job.getCompletedSamples());
        body.put("sampleCount", job.getSampleCount());
        if (job.getErrorMessage() != null) body.put("error", job.getErrorMessage());
        return ResponseEntity.ok(body);
    }

    @GetMapping(value = "/api/render/{id}/image", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getImage(@PathVariable String id) {
        RenderJob job = renderService.get(id);
        if (job == null) return ResponseEntity.notFound().build();
        if (job.getStatus() != RenderJob.Status.COMPLETE) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        }

        try {
            BufferedImage image = job.getRayTracer().canvas().getBufferedImage();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(baos.toByteArray());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/api/render/custom")
    public ResponseEntity<Map<String, String>> submitCustomRender(@RequestBody CustomRenderRequest request) {
        int width = request.width() > 0 ? request.width() : 500;
        int height = request.height() > 0 ? request.height() : 500;
        int renderLevels = request.renderLevels() > 0 ? request.renderLevels() : 4;
        int antiAlias = request.antiAlias() > 0 ? request.antiAlias() : 1;
        int sampleCount = request.sampleCount() > 1 ? request.sampleCount() : 1;

        RenderJob job = renderService.submitCustom(request.scene(), request.camera(),
                width, height, renderLevels, antiAlias, sampleCount);
        return ResponseEntity.accepted().body(Map.of("jobId", job.getId()));
    }

    @DeleteMapping("/api/render/{id}")
    public ResponseEntity<Void> cancelRender(@PathVariable String id) {
        return renderService.cancel(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/api/camera/move")
    public ResponseEntity<CameraDto> moveCamera(@RequestBody CameraMoveRequest request) {
        Camera camera = request.camera().toCamera();
        switch (request.action()) {
            case "moveForward"  -> camera.moveForward();
            case "moveBackward" -> camera.moveBackward();
            case "moveLeft"     -> camera.moveLeft();
            case "moveRight"    -> camera.moveRight();
            case "moveUp"       -> camera.moveUp();
            case "moveDown"     -> camera.moveDown();
            case "turnLeft"     -> camera.turnLeft();
            case "turnRight"    -> camera.turnRight();
            case "turnUp"       -> camera.turnUp();
            case "turnDown"     -> camera.turnDown();
            case "barrelLeft"   -> camera.barrelLeft();
            case "barrelRight"  -> camera.barrelRight();
            default -> { return ResponseEntity.badRequest().build(); }
        }
        return ResponseEntity.ok(CameraDto.from(camera));
    }

    public record RenderRequest(SceneDescriptorDto scene, int width, int height, int renderLevels, int antiAlias, int sampleCount) {}
    public record CustomRenderRequest(CustomSceneDto scene, CameraDto camera, int width, int height, int renderLevels, int antiAlias, int sampleCount) {}
    public record CameraMoveRequest(CameraDto camera, String action) {}
}
