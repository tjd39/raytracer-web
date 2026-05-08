package org.raytracerweb.scene.camera;

import static org.raytracerweb.geometry.Axis.Y;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.raytracerweb.geometry.rotation.RotationMatrix;
import org.raytracerweb.geometry.vector.Vec4;
import org.raytracerweb.math.Tuple;
import org.raytracerweb.preview.canvas.Pixel;

public class Camera {
    private static final float DEFAULT_FOCAL_LENGTH = 1f;
    private static final Vec4 DEFAULT_PAN = new Vec4(0f, 0f, -1f);
    private static final Vec4 DEFAULT_UP = new Vec4(-1f, 0f, 0f);
    private static final float DEFAULT_ROTATION_ANGLE = 5.0f; // 5 degrees

    private Vec4 position;
    private Vec4 rasterPosition;
    private Vec4 up;
    private Vec4 look;
    private Vec4 pan;
    private float focalLength;
    private final ConcurrentMap<Pixel, Tuple<Vec4, Vec4>> offsetVectors = new ConcurrentHashMap<>(); // pixel here used as a convenient storage of two int values
    private RotationMatrix panRotation;
    private RotationMatrix upRotation;
    private RotationMatrix lookRotation;

    public Camera(final Vec4 position) {
        this(position, position.reverse());
    }

    public Camera(final Vec4 position, final Vec4 look) {
        this(position, look, DEFAULT_FOCAL_LENGTH);
    }

    public Camera(final Vec4 position, final Vec4 look, final float focalLength) {
        this.position = position;
        this.focalLength = focalLength;
        setLook(look);
    }

    public Vec4 getPosition() {
        return position;
    }

    public Vec4 getRasterPosition() {
        return rasterPosition;
    }

    public Vec4 getUp() {
        return up;
    }

    public Vec4 getLook() {
        return look;
    }

    public Vec4 getPan() {
        return pan;
    }

    public float getFocalLength() {
        return focalLength;
    }

    public void setPosition(Vec4 position) {
        this.position = position;
        rasterPosition = position.plus(look.scale(focalLength));
    }

    public void setUp(Vec4 up) {
        this.up = up;
        upRotation = new RotationMatrix(up, DEFAULT_ROTATION_ANGLE);
        lookRotation = new RotationMatrix(look, DEFAULT_ROTATION_ANGLE);
    }

    public void setLook(Vec4 look) {
        this.look = look.normalise();
        rasterPosition = position.plus(look.scale(focalLength));

        pan = look.cross(new Vec4(Y));
        if (pan.length() <= 1e-5f) {
            // If camera is looking exactly vertically, no calculation can be done, use defaults.
            pan = DEFAULT_PAN;
            up = DEFAULT_UP;
        } else {
            // Calculate pan and up given the position, look and "world up"
            up = pan.cross(look);
        }

        setPan(pan.normalise());
        setUp(up.normalise());
    }

    public void setPan(Vec4 pan) {
        this.pan = pan;
        this.panRotation = new RotationMatrix(pan, DEFAULT_ROTATION_ANGLE);
    }

    public void setFocalLength(float focalLength) {
        this.focalLength = focalLength;
        this.rasterPosition = position.plus(look.scale(focalLength));
    }

    // Method to turn left (yaw left)
    public void turnLeft() {
        rotate(upRotation, false);
    }

    // Method to turn right (yaw right)
    public void turnRight() {
        rotate(upRotation, true);
    }

    // Method to turn up (pitch up)
    public void turnUp() {
        rotate(panRotation, false);
    }

    // Method to turn down (pitch down)
    public void turnDown() {
        rotate(panRotation, true);
    }

    public void barrelLeft() {
        rotate(lookRotation, false);
    }

    public void barrelRight() {
        rotate(lookRotation, true);
    }

    public void moveForward() {
        move(look, false);
    }

    public void moveBackward() {
        move(look, true);
    }

    public void moveRight() {
        move(pan, false);
    }

    public void moveLeft() {
        move(pan, true);
    }

    public void moveUp() {
        move(up, false);
    }

    public void moveDown() {
        move(up, true);
    }

    public Vec4 getPixelPosition(final Pixel pixel, final int imageWidth, final int imageHeight) {
        Tuple<Vec4, Vec4> offsets = offsetVectors.computeIfAbsent(new Pixel(imageWidth, imageHeight), this::calculateOffsetVectors);
        return getRasterPosition()
                .plus(offsets.getB().scale(((float) pixel.x() / imageWidth) - 0.5f))
                .plus(offsets.getA().scale(((float) pixel.y() / imageHeight) - 0.5f));
    }

    private Tuple<Vec4, Vec4> calculateOffsetVectors(final Pixel pixel) {
        int w = pixel.x();
        int h = pixel.y();
        if (h > w) {
            return new Tuple<>(up.reverse().scale((float) h / w), pan);
        } else if (w > h) {
            return new Tuple<>(up.reverse(), pan.scale((float) w / h));
        } else {
            return new Tuple<>(up.reverse(), pan);
        }
    }

    public Vec4 getSubPixelPosition(final Pixel pixel, final int imageWidth, final int imageHeight, final int px, final int py, final int antiAliasing) {
        if (px == 0 && py == 0) {
            return getPixelPosition(pixel, imageWidth, imageHeight);
        }

        final float xSubOffset = (((float) px / antiAliasing) - 0.5f) / imageWidth;
        final float ySubOffset = ((((float) py / antiAliasing) - 0.5f) / imageHeight * ((float) imageHeight / imageWidth));

        return getPixelPosition(pixel, imageWidth, imageHeight)
                .plus(pan.scale(xSubOffset))
                .plus(up.scale(-ySubOffset));
    }

    @Override
    public String toString() {
        return String.format("Camera(new Vec4(%sf, %sf, %sf), new Vec4(%sf, %sf, %sf));", position.x(), position.y(), position.z(), look.x(), look.y(), look.z());
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, look, focalLength);
    }

    private void move(final Vec4 offset, final boolean reverse) {
        position = position.plus(offset.scale(reverse ? -0.05f : 0.05f));
        rasterPosition = position.plus(look.scale(focalLength));
    }

    private void rotate(final RotationMatrix rotation, final boolean inverse) {
        if (inverse) {
            look = rotation.applyInverse(look).normalise();
            up = rotation.applyInverse(up).normalise();
            pan = rotation.applyInverse(pan).normalise();
        } else {
            look = rotation.apply(look).normalise();
            up = rotation.apply(up).normalise();
            pan = rotation.apply(pan).normalise();
        }
        rasterPosition = position.plus(look.scale(focalLength));

        upRotation = new RotationMatrix(up, DEFAULT_ROTATION_ANGLE);
        panRotation = new RotationMatrix(pan, DEFAULT_ROTATION_ANGLE);
        lookRotation = new RotationMatrix(look, DEFAULT_ROTATION_ANGLE);

        offsetVectors.clear();
    }
}
