package org.raytracerweb.preview.colour;

public class ColourFinal extends Colour {
    public ColourFinal(final Colour other) {
        super(other);
    }

    public ColourFinal(float r, float g, float b) {
        super(r, g, b);
    }

    @Override
    public Colour scale(final float s) {
        return new ColourFinal(
                r() * s,
                g() * s,
                b() * s
        );
    }

    @Override
    public Colour plus(final Colour other) {
        return new ColourFinal(
                r() + other.r(),
                g() + other.g(),
                b() + other.b()
        );
    }

    @Override
    public Colour multiply(final Colour other) {
        return new ColourFinal(
                r() * other.r(),
                g() * other.g(),
                b() * other.b()
        );
    }

    @Override
    public Colour toLinear() {
        return new ColourFinal(
                srgbToLinear(r()),
                srgbToLinear(g()),
                srgbToLinear(b())
        );
    }

    @Override
    public Colour toSRGB() {
        return new ColourFinal(
                linearToSrgb(r()),
                linearToSrgb(g()),
                linearToSrgb(b())
        );
    }

    @Override
    public ColourFinal immutable() {
        return this; // already immutable
    }

    @Override
    public Colour mutableCopy() {
        return new Colour(this);
    }

    public static Colour average(final Colour... colours) {
        return new ColourFinal(Colour.average(colours));
    }
}
