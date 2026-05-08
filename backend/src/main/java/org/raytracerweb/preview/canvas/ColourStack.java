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
        return getColour(colours.size() - 1);
    }

    public Colour getColour(final int index) {
        return colours.get(index);
    }

    public Colour getAvgColour() {
        if (colours.isEmpty()) {
            return new Colour(0f, 0f, 0f); // Return black if no colours are present
        }
        Colour avg = new Colour(0f, 0f, 0f);
        for (final Colour c : colours) {
            avg = avg.plus(c.toLinear());
        }
        avg.scale(1f / colours.size());
        return avg.toSRGB();
    }

    public void addColour(Colour colour) {
        this.colours.add(colour);
    }
}
