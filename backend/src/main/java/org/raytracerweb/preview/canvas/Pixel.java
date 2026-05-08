package org.raytracerweb.preview.canvas;

public record Pixel(int x, int y) {
    @Override
    public String toString() {
        return "Pixel [" + x + ", " + y + "]";
    }
}
