package org.raytracerweb.scene.objects.material.texture;

import java.net.URL;

public enum Texture {
    DEBUG("debug.png"),
    GREYSCALE_NOISE_RANDOM("greyscale-random-noise.png"),
    RAINBOW_NOISE_RANDOM("rainbow-random-noise.png"),
    MC_GRASS("mc-grass.png"),
    MC_COBBLE("mc-cobble.png"),
    MC_DIRT("mc-dirt.png"),
    CHEQUERBOARD("chequerboard.png");

    private final String fileName;

    Texture(final String fileName) {
        this.fileName = fileName;
    }

    public URL getUrl() {
        URL resource = Texture.class.getResource(fileName);
        if (resource == null) {
            throw new IllegalStateException("Image texture not found on classpath: " + fileName);
        }
        return resource;
    }
}
