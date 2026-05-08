package org.raytracerweb.scene;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.raytracerweb.geometry.vector.Vec4;
import org.raytracerweb.scene.camera.Camera;

class CameraTest {

    private static final float DELTA = 1e-4f;

    @Test
    void moveForward_changesPositionAlongLook() {
        Camera cam = new Camera(new Vec4(0f, 0f, 0f), new Vec4(0f, 0f, -1f));
        Vec4 before = cam.getPosition();
        cam.moveForward();
        Vec4 after = cam.getPosition();

        // position should move in the look direction
        assertThat(after.z()).isLessThan(before.z());
        assertThat(after.x()).isCloseTo(before.x(), offset(DELTA));
        assertThat(after.y()).isCloseTo(before.y(), offset(DELTA));
    }

    @Test
    void moveBackward_reversesForward() {
        Camera cam = new Camera(new Vec4(0f, 0f, 0f), new Vec4(0f, 0f, -1f));
        cam.moveForward();
        float zAfterForward = cam.getPosition().z();
        cam.moveBackward();
        cam.moveBackward();
        assertThat(cam.getPosition().z()).isGreaterThan(zAfterForward);
    }

    @ParameterizedTest
    @ValueSource(strings = {"turnLeft", "turnRight", "turnUp", "turnDown", "barrelLeft", "barrelRight"})
    void rotations_keepLookNormalised(String action) throws Exception {
        Camera cam = new Camera(new Vec4(3f, 3f, 3f));
        cam.getClass().getMethod(action).invoke(cam);
        float len = cam.getLook().length();
        assertThat(len).isCloseTo(1f, offset(DELTA));
    }

    @Test
    void turnLeft_thenRight_returnsToOriginalLook() {
        Camera cam = new Camera(new Vec4(0f, 0f, 5f), new Vec4(0f, 0f, -1f));
        Vec4 originalLook = cam.getLook();
        // Each turn is 5 degrees; 72 left + 72 right should cancel
        for (int i = 0; i < 72; i++) cam.turnLeft();
        for (int i = 0; i < 72; i++) cam.turnRight();

        assertThat(cam.getLook().x()).isCloseTo(originalLook.x(), offset(0.01f));
        assertThat(cam.getLook().y()).isCloseTo(originalLook.y(), offset(0.01f));
        assertThat(cam.getLook().z()).isCloseTo(originalLook.z(), offset(0.01f));
    }

    @Test
    void moveUp_thenDown_returnsToOrigin() {
        Camera cam = new Camera(new Vec4(0f, 0f, 5f), new Vec4(0f, 0f, -1f));
        cam.moveUp();
        cam.moveDown();
        assertThat(cam.getPosition().x()).isCloseTo(0f, offset(DELTA));
        assertThat(cam.getPosition().y()).isCloseTo(0f, offset(DELTA));
        assertThat(cam.getPosition().z()).isCloseTo(5f, offset(DELTA));
    }

    @Test
    void lookIsNormalisedOnConstruction() {
        Camera cam = new Camera(new Vec4(1f, 2f, 3f), new Vec4(3f, 4f, 0f));
        assertThat(cam.getLook().length()).isCloseTo(1f, offset(DELTA));
    }
}
