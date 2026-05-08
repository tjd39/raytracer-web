package org.raytracerweb.scene.lights;

import org.raytracerweb.geometry.vector.Vec4;
import org.raytracerweb.preview.colour.Colour;

public record LightProperties(Vec4 lightToPointVector, Colour lightIntensity) {

}
