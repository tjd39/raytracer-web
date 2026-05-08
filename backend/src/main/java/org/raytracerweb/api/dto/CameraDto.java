package org.raytracerweb.api.dto;

import org.raytracerweb.geometry.vector.Vec4;
import org.raytracerweb.scene.camera.Camera;

public record CameraDto(float[] position, float[] look) {

    public static CameraDto from(Camera camera) {
        Vec4 pos = camera.getPosition();
        Vec4 look = camera.getLook();
        return new CameraDto(
                new float[]{pos.x(), pos.y(), pos.z()},
                new float[]{look.x(), look.y(), look.z()}
        );
    }

    public Camera toCamera() {
        return new Camera(
                new Vec4(position[0], position[1], position[2]),
                new Vec4(look[0], look[1], look[2])
        );
    }
}
