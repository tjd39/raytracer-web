package org.raytracerweb.api.dto;

import java.util.List;

/**
 * Wire format for a user-authored scene from the scene builder UI.
 *
 * floor.type  = "texture" | "solid"
 * floor.texture = texture key (chequerboard, mc_grass, …) — used when type="texture"
 * floor.color   = [r,g,b] 0–1 floats — used when type="solid"
 * floor.roughness = 0–1
 * floor.height  = Y offset of the floor plane (distance from origin, default 0)
 *
 * sky.type  = "preset" | "solid"
 * sky.preset = "default" | "black" | "sunset"
 * sky.color = [r,g,b]
 *
 * light.type = "sun" | "sunset" | "none"
 *
 * spheres[] = list of sphere descriptors
 */
public record CustomSceneDto(
        FloorDto floor,
        SkyDto sky,
        LightDto light,
        List<SphereDto> spheres
) {
    public record FloorDto(String type, String texture, float[] color, float roughness, float height) {}

    public record SkyDto(String type, String preset, float[] color) {}

    public record LightDto(String type) {}

    public record SphereDto(
            float x, float y, float z,
            float radius,
            String materialType,   // "simple" | "iridescent" | "kaleidoscopic"
            float[] color,         // absorption colour [r,g,b]
            float[] emission,      // emission colour [r,g,b] (null = black)
            float roughness,
            float transparency,
            float indexOfRefraction
    ) {}
}
