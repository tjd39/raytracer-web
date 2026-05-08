package org.raytracerweb.scene.objects.material;

import static org.raytracerweb.math.MathHelper.clamp;
import static org.raytracerweb.math.MathHelper.lerp;
import static org.raytracerweb.preview.colour.ColourPreset.BLACK;
import static org.raytracerweb.preview.colour.ColourPreset.WHITE;

import org.raytracerweb.geometry.vector.Vec4;
import org.raytracerweb.preview.colour.Colour;
import org.raytracerweb.ray.HitInfo;
import org.raytracerweb.ray.Ray;

public class IridescentMaterial extends SimpleMaterial {
    public IridescentMaterial(final float roughness, final float transparency, final float indexOfRefraction) {
        super(WHITE.get(), BLACK.get(), roughness, transparency, indexOfRefraction);
    }

    @Override
    public void absorption(final Colour input, final Ray ray, final HitInfo hitInfo) {
        input.multiply(iridescent(ray, hitInfo));
    }

    @Override
    public void emission(final Colour input, final Ray ray, final HitInfo hitInfo) {
//        input.plus(iridescent(ray, hitInfo)); // no-op
    }

    private Colour iridescent(final Ray ray, final HitInfo hitInfo) {
        float cosTheta = clamp(Math.abs(hitInfo.normal().dot(ray.direction())), 0.0f, 1.0f);
        Vec4 p = ray.getPositionAt(hitInfo.distance());
        // grazing-angle emphasis, softened with smooth step
        float angleFactor = (float) java.lang.Math.pow(1.0f - cosTheta, 1.0f);
        float f = smoothStep(angleFactor);

        // hue shift
        float hue = fract(fract(0.6f * f) + (0.1f * fract((float) Math.sin(p.x() * 12.9898f + p.y() * 78.233f + p.z() * 37.719f) * 43758.5453f)));

        // optional saturation boost toward rim
        float S = lerp(0.8f, 1.0f, f);
        float V = lerp(1.0f, 1.2f, f);

        return hsvToRgb(hue, S, V);
    }

    private Colour hsvToRgb(float h, float s, float v) {
        float r, g, b;

        float i = (float) java.lang.Math.floor(h * 6.0f);
        float f = h * 6.0f - i;
        float p = v * (1.0f - s);
        float q = v * (1.0f - f * s);
        float t = v * (1.0f - (1.0f - f) * s);

        int mod = (int) (i % 6);
        switch (mod) {
        case 0:
            r = v;
            g = t;
            b = p;
            break;
        case 1:
            r = q;
            g = v;
            b = p;
            break;
        case 2:
            r = p;
            g = v;
            b = t;
            break;
        case 3:
            r = p;
            g = q;
            b = v;
            break;
        case 4:
            r = t;
            g = p;
            b = v;
            break;
        case 5:
        default:
            r = v;
            g = p;
            b = q;
            break;
        }

        return new Colour(r, g, b);
    }

    private float fract(float x) {
        return x - (float) java.lang.Math.floor(x);
    }

    private float smoothStep(float x) {
        float t = clamp(x, 0.0f, 1.0f);
        return t * t * (3.0f - 2.0f * t);
    }
}
