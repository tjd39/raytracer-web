package org.raytracerweb.preview.canvas;

import java.util.ArrayList;

import org.raytracerweb.preview.colour.Colour;

public class ColourStack {
    private final ArrayList<Colour> colours;

    public ColourStack(final Colour colour) {
        this();
        addColour(colour);
    }

    public ColourStack() {
        this.colours = new ArrayList<>();
    }

    public Colour getColour() {
        return colours.isEmpty() ? new Colour(0f, 0f, 0f) : colours.get(colours.size() - 1);
    }

    public Colour getColour(final int index) {
        return colours.get(index);
    }

    public Colour getAvgColour() {
        if (colours.isEmpty()) return new Colour(0f, 0f, 0f);

        float sumR = 0f, sumG = 0f, sumB = 0f;
        for (final Colour c : colours) {
            final Colour lin = c.toLinear();
            sumR += lin.r(); sumG += lin.g(); sumB += lin.b();
        }
        final int n = colours.size();
        float r = sumR / n, g = sumG / n, b = sumB / n;

        if (n > 1) {
            // Reinhard tone mapping in linear space: maps HDR [0,∞) → [0,1)
            r = r / (1f + r);
            g = g / (1f + g);
            b = b / (1f + b);
        }

        return new Colour(r, g, b).toSRGB();
    }

    public void addColour(Colour colour) {
        this.colours.add(colour);
    }
}
