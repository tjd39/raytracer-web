package org.raytracerweb.scene.objects.mesh;

public enum MeshType {
    OBJ,
    TXT;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
