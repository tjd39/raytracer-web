package org.raytracerweb.scene.objects.material;

import org.raytracerweb.preview.colour.Colour;

public class SimpleMaterial extends AbstractMaterial {
    public SimpleMaterial(final Colour absorption, final Colour emission, final float roughness, final float transparency, final float indexOfRefraction) {
        super(absorption, emission, roughness, transparency, indexOfRefraction);
    }
}
