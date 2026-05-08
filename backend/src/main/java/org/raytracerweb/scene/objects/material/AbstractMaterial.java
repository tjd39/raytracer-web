package org.raytracerweb.scene.objects.material;

import static org.raytracerweb.preview.colour.ColourPreset.BLACK;
import static org.raytracerweb.preview.colour.ColourPreset.WHITE;

import org.raytracerweb.preview.colour.Colour;
import org.raytracerweb.ray.HitInfo;
import org.raytracerweb.ray.Ray;

public abstract class AbstractMaterial implements Material {
    private final Colour absorption;
    private final Colour emission;
    private final float roughness;
    private final float transparency;
    private final float indexOfRefraction;

    public AbstractMaterial(final Colour absorption, final Colour emission, final float roughness, final float transparency, final float indexOfRefraction) {
        // Automatically taper off the effects of absorption and emission for transparent objects.
        this.absorption = Colour.lerp(absorption, WHITE.get(), transparency).immutable();
        this.emission = Colour.lerp(emission, BLACK.get(), transparency).immutable();

        this.roughness = roughness;
        this.transparency = transparency;
        this.indexOfRefraction = indexOfRefraction;
    }

    @Override
    public void absorption(final Colour input, final Ray ray, final HitInfo hitInfo) {
        input.multiply(absorption);
    }

    @Override
    public void emission(final Colour input, final Ray ray, final HitInfo hitInfo) {
        input.plus(emission);
    }

    @Override
    public float getRoughness() {
        return roughness;
    }

    @Override
    public float getTransparency() {
        return transparency;
    }

    @Override
    public float getIndexOfRefraction() {
        return indexOfRefraction;
    }
}
