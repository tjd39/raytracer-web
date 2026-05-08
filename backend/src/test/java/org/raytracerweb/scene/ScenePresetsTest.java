package org.raytracerweb.scene;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.raytracerweb.api.dto.SceneDescriptorDto;

class ScenePresetsTest {

    @ParameterizedTest
    @ValueSource(strings = {"simple_ball", "pool_balls", "sunset_balls", "cowboy_pingy"})
    void forName_buildsSceneWithObjects(String name) {
        SceneResult result = ScenePresets.forName(name);
        assertThat(result).isNotNull();
        assertThat(result.scene()).isNotNull();
        assertThat(result.scene().sceneObjects()).isNotEmpty();
        assertThat(result.camera()).isNotNull();
    }

    @Test
    void forName_unknownName_fallsBackToSimpleBall() {
        SceneResult fallback = ScenePresets.forName("does_not_exist");
        SceneResult simpleBall = ScenePresets.simpleBall();
        // Both should build without error and have a camera
        assertThat(fallback.camera()).isNotNull();
        assertThat(fallback.scene().sceneObjects().length)
                .isEqualTo(simpleBall.scene().sceneObjects().length);
    }

    @Test
    void listAll_returnsAllFourPresets() {
        List<SceneDescriptorDto> list = ScenePresets.listAll();
        assertThat(list).hasSize(4);
        assertThat(list).extracting(SceneDescriptorDto::name)
                .containsExactlyInAnyOrder("simple_ball", "pool_balls", "sunset_balls", "cowboy_pingy");
    }

    @Test
    void listAll_descriptorsHaveLabelsAndCameras() {
        for (SceneDescriptorDto d : ScenePresets.listAll()) {
            assertThat(d.label()).isNotBlank();
            assertThat(d.defaultCamera()).isNotNull();
            assertThat(d.defaultCamera().position()).hasSize(3);
            assertThat(d.defaultCamera().look()).hasSize(3);
        }
    }

    @Test
    void eachPreset_hasCameraWithNormalisedLook() {
        for (SceneDescriptorDto d : ScenePresets.listAll()) {
            float[] look = d.defaultCamera().look();
            float len = (float) Math.sqrt(look[0]*look[0] + look[1]*look[1] + look[2]*look[2]);
            assertThat(len).isCloseTo(1f, org.assertj.core.api.Assertions.offset(1e-4f));
        }
    }
}
