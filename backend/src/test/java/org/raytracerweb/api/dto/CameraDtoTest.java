package org.raytracerweb.api.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import org.junit.jupiter.api.Test;
import org.raytracerweb.scene.camera.Camera;
import org.raytracerweb.geometry.vector.Vec4;

class CameraDtoTest {

    @Test
    void fromCamera_capturesPositionAndLook() {
        Camera camera = new Camera(new Vec4(1f, 2f, 3f), new Vec4(-1f, 0f, 0f));
        CameraDto dto = CameraDto.from(camera);

        assertThat(dto.position()).containsExactly(1f, 2f, 3f);
        // look is normalised by Camera constructor, so just check direction
        assertThat(dto.look()[0]).isCloseTo(-1f, offset(1e-4f));
        assertThat(dto.look()[1]).isCloseTo(0f,  offset(1e-4f));
        assertThat(dto.look()[2]).isCloseTo(0f,  offset(1e-4f));
    }

    @Test
    void toCamera_reconstructsEquivalentCamera() {
        Camera original = new Camera(new Vec4(5f, 0f, -3f), new Vec4(0f, 0f, -1f));
        CameraDto dto = CameraDto.from(original);
        Camera reconstructed = dto.toCamera();

        assertThat(reconstructed.getPosition().x()).isCloseTo(5f,  offset(1e-4f));
        assertThat(reconstructed.getPosition().y()).isCloseTo(0f,  offset(1e-4f));
        assertThat(reconstructed.getPosition().z()).isCloseTo(-3f, offset(1e-4f));

        assertThat(reconstructed.getLook().x()).isCloseTo(0f,  offset(1e-4f));
        assertThat(reconstructed.getLook().y()).isCloseTo(0f,  offset(1e-4f));
        assertThat(reconstructed.getLook().z()).isCloseTo(-1f, offset(1e-4f));
    }

    @Test
    void roundTrip_preservesValues() {
        Camera camera = new Camera(new Vec4(3f, 3f, 3f));
        CameraDto dto = CameraDto.from(camera);
        Camera back = dto.toCamera();

        assertThat(CameraDto.from(back).position())
                .usingComparatorWithPrecision(1e-4f)
                .containsExactly(dto.position());
    }
}
