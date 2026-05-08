package org.raytracerweb.scene.objects;

import org.raytracerweb.geometry.vector.Vec4;
import org.raytracerweb.ray.HitInfo;
import org.raytracerweb.ray.Ray;
import org.raytracerweb.scene.objects.material.Material;

public abstract class SceneObject {
    protected static final float BIAS = 10E-4f;
    private final Material material;
    private final boolean render;

    public SceneObject(final Material material, final boolean render) {
        this.material = material;
        this.render = render;
    }

    public Material getMaterial() {
        return material;
    }

    public boolean isRender() {
        return render;
    }

    public Vec4 getUVCoords(final Vec4 position) {
        return new Vec4(0.0f, 0.0f);
    }

    public abstract HitInfo checkIntersection(final Ray ray);

    public abstract Vec4 normalAt(final Vec4 position);
}
