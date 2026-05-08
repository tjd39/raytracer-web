package org.raytracerweb.preview.colour;

import static org.raytracerweb.math.MathHelper.clamp;

import java.awt.*;
import java.util.Arrays;

import org.raytracerweb.math.MathHelper;


public class Colour {
    private float r;
    private float g;
    private float b;

    public Colour(final float r, final float g, final float b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public Colour(final Colour other) {
        set(other);
    }

    public float r() {
        return r;
    }

    public float g() {
        return g;
    }

    public float b() {
        return b;
    }

    public void setR(final float r) {
        this.r = r;
    }

    public void setG(final float g) {
        this.g = g;
    }

    public void setB(final float b) {
        this.b = b;
    }

    public void set(final Colour other) {
        setR(other.r());
        setG(other.g());
        setB(other.b());
    }

    public Colour scale(final float s) {
        setR(r * s);
        setG(g * s);
        setB(b * s);
        return this;
    }

    public Colour plus(final Colour other) {
        setR(r + other.r());
        setG(g + other.g());
        setB(b + other.b());
        return this;
    }

    public Colour multiply(final Colour other) {
        setR(r() * other.r());
        setG(g() * other.g());
        setB(b() * other.b());
        return this;
    }

    public int getRGBValue() {
        // Ripped out of the java.awt.Color class
        int r = (int) ((clamp(r(), 0f, 1f) * 255) + 0.5);
        int g = (int) ((clamp(g(), 0f, 1f) * 255) + 0.5);
        int b = (int) ((clamp(b(), 0f, 1f) * 255) + 0.5);
        return (0xFF << 24)
                | ((r & 0xFF) << 16)
                | ((g & 0xFF) << 8)
                | (b & 0xFF);
    }

    protected float srgbToLinear(float c) {
        return (c <= 0.04045f)
                ? c / 12.92f
                : (float) Math.pow((c + 0.055f) / 1.055f, 2.4f);
    }

    protected float linearToSrgb(float c) {
        return (c <= 0.0031308f)
                ? 12.92f * c
                : 1.055f * (float) Math.pow(c, 1.0 / 2.4) - 0.055f;
    }

    public Colour toLinear() {
        return new Colour(
                srgbToLinear(r()),
                srgbToLinear(g()),
                srgbToLinear(b())
        );
    }

    public Colour toSRGB() {
        return new Colour(
                linearToSrgb(r()),
                linearToSrgb(g()),
                linearToSrgb(b())
        );
    }

    public float getLuminance() {
        // Calculate luminance using the formula: 0.2126 * R + 0.7152 * G + 0.0722 * B
        return (0.2126f * srgbToLinear(r()))
                + (0.7152f * srgbToLinear(g()))
                + (0.0722f * srgbToLinear(b()));
    }

    public Color toAWTColor() {
        return new Color(
                Math.round(r() * 255),
                Math.round(g() * 255),
                Math.round(b() * 255)
        );
    }

    public Colour lerp(final Colour other, final float t) {
        setR(MathHelper.lerp(r, other.r(), t));
        setG(MathHelper.lerp(g, other.g(), t));
        setB(MathHelper.lerp(b, other.b(), t));
        return this;
    }

    public ColourFinal immutable() {
        return new ColourFinal(this);
    }

    public Colour mutableCopy() {
        return this; // already mutable
    }

    public static Colour average(final Colour... colours) {
        Colour average = new Colour(0f, 0f, 0f);
        Arrays.stream(colours).forEach(average::plus);
        return average.scale(1f / colours.length);
    }

    public static Colour fromRGBValue(final int rgbValue) {
        // Ripped out of the java.awt.Color class
        int r = (rgbValue >> 16) & 0xFF;
        int g = (rgbValue >> 8) & 0xFF;
        int b = rgbValue & 0xFF;
        return new Colour(r / 255f, g / 255f, b / 255f);
    }

    public static Colour lerp(final Colour first, final Colour second, final float t) {
        // Linear interpolation between two colours
        return new Colour(
                MathHelper.lerp(first.r(), second.r(), t),
                MathHelper.lerp(first.g(), second.g(), t),
                MathHelper.lerp(first.b(), second.b(), t)
        );
    }

    public static Colour fromAWTColor(final Color color) {
        return new Colour(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
    }
}
