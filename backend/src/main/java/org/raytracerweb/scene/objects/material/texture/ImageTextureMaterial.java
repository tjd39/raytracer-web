package org.raytracerweb.scene.objects.material.texture;

import static org.raytracerweb.preview.colour.ColourPreset.BLACK;
import static org.raytracerweb.preview.colour.ColourPreset.WHITE;

import org.raytracerweb.geometry.vector.Vec4;
import org.raytracerweb.preview.canvas.Canvas;
import org.raytracerweb.preview.colour.Colour;
import org.raytracerweb.ray.HitInfo;
import org.raytracerweb.ray.Ray;
import org.raytracerweb.scene.objects.material.AbstractMaterial;

public class ImageTextureMaterial extends AbstractMaterial {
    private final float scaleFactor;
    private final Canvas imageCanvas;

    public ImageTextureMaterial(final Texture texture, final float scaleFactor) {
        this(texture, scaleFactor, 1f, 0f, 1f);
    }

    public ImageTextureMaterial(final Texture texture, final float scaleFactor, final float roughness, final float transparency, final float indexOfRefraction) {
        super(WHITE.get(), BLACK.get(), roughness, transparency, indexOfRefraction);
        this.scaleFactor = scaleFactor;
        try {
            this.imageCanvas = Canvas.fromImage(texture.getUrl());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void absorption(final Colour input, final Ray ray, final HitInfo hitInfo) {
        input.multiply(sampleImage(ray, hitInfo));
    }

    @Override
    public void emission(final Colour input, final Ray ray, final HitInfo hitInfo) {
        //input.plus(sampleImage(ray, hitInfo)); // no-op
    }

    private Colour sampleImage(final Ray ray, final HitInfo hitInfo) {
        Vec4 uvCoords = hitInfo.object().getUVCoords(ray.getPositionAt(hitInfo.distance()));
        float u = uvCoords.x() * scaleFactor;
        float v = uvCoords.y() * scaleFactor;

        // out of bounds
        if (u < 0.0f || v < 0.0f || u > 1.0f || v > 1.0f) {
            return new Colour(0.0f, 0.0f, 0.0f);
        }

        // wrap around to zero to avoid pixel buffer overflow
        if (u == 1.0f) u = 0.0f;
        if (v == 1.0f) v = 0.0f;

        return imageCanvas.getPixelValue((int) (imageCanvas.getWidth() * u), (int) (imageCanvas.getHeight() * v));
    }
}
