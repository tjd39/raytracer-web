package org.raytracerweb.scene.objects.mesh;

import java.net.URL;

public enum Mesh {
    DIAMOND,
    ICOSOHEDRON,
    HEART,
    PAWN,
    TREE,
    TREE2,
    TREE3,
    TREE4,
    PIG,
    BUNNY,
    PINGY,
    PINGY_WAVE,
    PINGY_SITTING,
    HORSE,
    COWBOY_HAT;

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    public String getFilePath(final MeshType type) {
        URL url = Mesh.class.getResource(this + "." + type.toString());
        return url == null ? "" : url.getPath();
    }
}
