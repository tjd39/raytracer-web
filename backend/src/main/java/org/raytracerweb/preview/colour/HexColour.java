package org.raytracerweb.preview.colour;

// Cool idea but not HDR compliant..
public class HexColour {
    private int r = 0x00;
    private int g = 0x00;
    private int b = 0x00;

    public HexColour(final int hexcodex) {
        r = (hexcodex >> 16) & 0xFF;
        g = (hexcodex >> 8) & 0xFF;
        b = hexcodex & 0xFF;
    }

    public int hex() {
        return (r << 16) | (g << 8) | b;
    }

    public int r() {
        return r;
    }

    public int g() {
        return g;
    }

    public int b() {
        return b;
    }

    public void plus(final HexColour other) {
        r = Math.min(255, r + other.r());
        g = Math.min(255, g + other.g());
        b = Math.min(255, b + other.b());
    }
}
