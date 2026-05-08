package org.raytracerweb.scene.objects.polygon.cube;

import static org.raytracerweb.scene.objects.SurfaceNormals.NORMAL;

import org.raytracerweb.geometry.vector.Vec4;
import org.raytracerweb.scene.objects.SurfaceNormals;
import org.raytracerweb.scene.objects.Triangle;
import org.raytracerweb.scene.objects.material.Material;
import org.raytracerweb.scene.objects.polygon.Polygon;

// TODO:
//    - This might be obsolete now we have AABB?
//    - Maybe not, with non-axis-aligned cubes..
public abstract class AbstractCube extends Polygon {
    protected Vec4 center;

    /**
     * Specify between 1 and 6 materials to use
     */
    public AbstractCube(final Vec4 centre, final Vec4 v1, final Vec4 v2, final Vec4 v3, boolean render, final Material... material) {
        this(centre, v1, v2, v3, render, NORMAL, material);
    }

    public AbstractCube(final Vec4 centre, final Vec4 v1, final Vec4 v2, final Vec4 v3, boolean render, SurfaceNormals normals, final Material... material) {
        super(material[0], render);

        this.center = centre;
        final Vec4 p1 = centre.plus(v1).plus(v2).plus(v3);
        final Vec4 p2 = centre.plus(v1).plus(v2).plus(v3.reverse());
        final Vec4 p3 = centre.plus(v1).plus(v2.reverse()).plus(v3);
        final Vec4 p4 = centre.plus(v1).plus(v2.reverse()).plus(v3.reverse());
        final Vec4 p5 = centre.plus(v1.reverse()).plus(v2).plus(v3);
        final Vec4 p6 = centre.plus(v1.reverse()).plus(v2).plus(v3.reverse());
        final Vec4 p7 = centre.plus(v1.reverse()).plus(v2.reverse()).plus(v3);
        final Vec4 p8 = centre.plus(v1.reverse()).plus(v2.reverse()).plus(v3.reverse());
        int i = 0;
        // front
        triangleList.add(new Triangle(material[i % material.length], p6, p5, p8, true, normals));
        triangleList.add(new Triangle(material[i++ % material.length], p7, p8, p5, true, normals));
        // back
        triangleList.add(new Triangle(material[i % material.length], p1, p2, p3, true, normals));
        triangleList.add(new Triangle(material[i++ % material.length], p4, p3, p2, true, normals));
        // left
        triangleList.add(new Triangle(material[i % material.length], p4, p8, p3, true, normals));
        triangleList.add(new Triangle(material[i++ % material.length], p7, p3, p8, true, normals));
        // right
        triangleList.add(new Triangle(material[i % material.length], p1, p5, p2, true, normals));
        triangleList.add(new Triangle(material[i++ % material.length], p6, p2, p5, true, normals));
        // top
        triangleList.add(new Triangle(material[i % material.length], p1, p3, p5, true, normals));
        triangleList.add(new Triangle(material[i++ % material.length], p7, p5, p3, true, normals));
        // bottom
        triangleList.add(new Triangle(material[i % material.length], p6, p8, p2, true, normals));
        triangleList.add(new Triangle(material[i % material.length], p4, p2, p8, true, normals));
    }

    public Vec4 getCenter() {
        return center;
    }
}
