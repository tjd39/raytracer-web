package org.raytracerweb.ray;

import org.raytracerweb.geometry.vector.Vec4;
import org.raytracerweb.scene.objects.SceneObject;

public record HitInfo(float distance, SceneObject object, Vec4 normal, boolean frontFaceIntersection) {

}
