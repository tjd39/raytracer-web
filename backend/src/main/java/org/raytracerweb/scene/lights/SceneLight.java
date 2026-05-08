package org.raytracerweb.scene.lights;

import org.raytracerweb.geometry.vector.Vec4;
import org.raytracerweb.preview.colour.Colour;

public abstract class SceneLight {
    public static int DEFAULT_LIGHT_PHONG_POWER = 10;
    protected final Vec4 lightDirection;
    protected final Colour lightColour;

    public SceneLight(final Vec4 lightDirection, final Colour lightColour) {
        this.lightDirection = lightDirection.normalise();
        this.lightColour = lightColour;
    }

    public Vec4 getLightDirection() {
        return lightDirection;
    }

    public Colour getLightColour() {
        return lightColour;
    }

    public abstract LightProperties getPropertiesAt(final Vec4 position);
//    public abstract void lightInteraction(final Material objectMaterial, final Vec4 surfaceNormal);
}
