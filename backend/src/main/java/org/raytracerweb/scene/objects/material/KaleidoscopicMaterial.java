package org.raytracerweb.scene.objects.material;

import static org.raytracerweb.preview.colour.ColourPreset.BLACK;
import static org.raytracerweb.preview.colour.ColourPreset.GREY;
import static org.raytracerweb.preview.colour.ColourPreset.WHITE;

import org.raytracerweb.preview.colour.Colour;
import org.raytracerweb.preview.colour.ColourPreset;
import org.raytracerweb.ray.HitInfo;
import org.raytracerweb.ray.Ray;

/**
 * Experimental Material that changes colour every time it's used.
 * <p/>
 * N.B: This is very silly, results in per-sub-pixel rgb noise.. does not play nicely with anti-aliasing.
 */
public class KaleidoscopicMaterial extends AbstractMaterial {
    public KaleidoscopicMaterial() {
        super(WHITE.get(), BLACK.get(), 1f, 0f, 1f);
    }

    @Override
    public void absorption(final Colour input, final Ray ray, final HitInfo hitInfo) {
        final Colour randomColour = ColourPreset.randomNotDark().get();
        input.multiply(randomColour);
    }

    @Override
    public void emission(final Colour input, final Ray ray, final HitInfo hitInfo) {
        // do nothing.
    }
}
