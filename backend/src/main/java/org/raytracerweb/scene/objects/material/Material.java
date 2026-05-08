package org.raytracerweb.scene.objects.material;

import static org.raytracerweb.preview.colour.ColourPreset.BLACK;
import static org.raytracerweb.preview.colour.ColourPreset.WHITE;

import org.raytracerweb.preview.colour.Colour;
import org.raytracerweb.preview.colour.ColourPreset;
import org.raytracerweb.ray.HitInfo;
import org.raytracerweb.ray.Ray;

public interface Material {
    void absorption(final Colour input, final Ray ray, final HitInfo hitInfo);

    void emission(final Colour input, final Ray ray, final HitInfo hitInfo);

    float getRoughness();

    float getTransparency();

    float getIndexOfRefraction();

    static Material nonEmissive(final ColourPreset colour) {
        // Black doesn't emit
        return new SimpleMaterial(colour.get(), BLACK.get(), 1.0f, 0f, 1f);
    }

    static Material emissive(final ColourPreset colour) {
        // White doesn't absorb
        return new SimpleMaterial(WHITE.get(), colour.get(), 1f, 0f, 1f);
    }

    static Material shiny(final ColourPreset colour, final float roughness) {
        return new SimpleMaterial(colour.get(), BLACK.get(), roughness, 0f, 1f);
    }

    static Material transparent() {
        return Material.transparent(WHITE, 1f, 1.025f);
    }

    static Material transparent(final ColourPreset colour, final float transparency, final float indexOfRefraction) {
        return new SimpleMaterial(colour.get(), BLACK.get(), 1f, transparency, indexOfRefraction);
    }
}
