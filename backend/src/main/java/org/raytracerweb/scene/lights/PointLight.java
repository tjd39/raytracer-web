package org.raytracerweb.scene.lights;

import org.raytracerweb.geometry.vector.Vec4;
import org.raytracerweb.preview.colour.Colour;

/**
 * Simple representation of a size-less point light source.
 */
public class PointLight extends SceneLight {
    private final Vec4 lightPosition;

    public PointLight(final Vec4 lightPosition, final Vec4 lightDirection, final Colour lightColour) {
        super(lightDirection, lightColour);
        this.lightPosition = lightPosition;
    }

    public Vec4 getLightPosition() {
        return lightPosition;
    }

//    @Override
//    public void lightInteraction(final Material objectMaterial, final Vec4 surfaceNormal) {
//
//    }

    @Override
    public LightProperties getPropertiesAt(final Vec4 position) {
        final Vec4 ltpVector;
        final Colour intensity = getLightColour();
        ltpVector = getLightPosition().to(position);

        if (getLightDirection().length() > 0f) {
            // Ip = Il * (L . D)^n
            intensity.scale((float) Math.pow(ltpVector.dot(getLightDirection()), SceneLight.DEFAULT_LIGHT_PHONG_POWER));
        }

        return new LightProperties(ltpVector.normalise(), intensity);
    }
}
