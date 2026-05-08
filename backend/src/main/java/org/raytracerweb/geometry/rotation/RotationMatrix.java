package org.raytracerweb.geometry.rotation;

import org.raytracerweb.geometry.vector.Vec4;


// TODO: local coordinate space rotations?
public class RotationMatrix {
    private final float[][] matrix;

    public RotationMatrix(Vec4 rotationVector, float angleDegrees) {
        float angleRadians = (float) Math.toRadians(angleDegrees);
        rotationVector = rotationVector.normalise();
        double cosTheta = Math.cos(angleRadians);
        double sinTheta = Math.sin(angleRadians);
        matrix = new float[4][4];
        double x = rotationVector.x();
        double y = rotationVector.y();
        double z = rotationVector.z();
        matrix[0][0] = (float) (cosTheta + x * x * (1 - cosTheta));
        matrix[0][1] = (float) (x * y * (1 - cosTheta) - z * sinTheta);
        matrix[0][2] = (float) (x * z * (1 - cosTheta) + y * sinTheta);
        matrix[0][3] = 0f;
        matrix[1][0] = (float) (y * x * (1 - cosTheta) + z * sinTheta);
        matrix[1][1] = (float) (cosTheta + y * y * (1 - cosTheta));
        matrix[1][2] = (float) (y * z * (1 - cosTheta) - x * sinTheta);
        matrix[1][3] = 0f;
        matrix[2][0] = (float) (z * x * (1 - cosTheta) - y * sinTheta);
        matrix[2][1] = (float) (z * y * (1 - cosTheta) + x * sinTheta);
        matrix[2][2] = (float) (cosTheta + z * z * (1 - cosTheta));
        matrix[2][3] = 0f;
        matrix[3][0] = 0f;
        matrix[3][1] = 0f;
        matrix[3][2] = 1f;
    }

    public RotationMatrix(float[][] matrix) {
        this.matrix = matrix;
    }

    public Vec4 apply(Vec4 source) {
        return new Vec4(
                (matrix[0][0] * source.x()) + (matrix[0][1] * source.y()) + (matrix[0][2] * source.z()) + (matrix[0][3] * source.a()),
                (matrix[1][0] * source.x()) + (matrix[1][1] * source.y()) + (matrix[1][2] * source.z()) + (matrix[1][3] * source.a()),
                (matrix[2][0] * source.x()) + (matrix[2][1] * source.y()) + (matrix[2][2] * source.z()) + (matrix[2][3] * source.a()),
                (matrix[3][0] * source.x()) + (matrix[3][1] * source.y()) + (matrix[3][2] * source.z()) + (matrix[3][3] * source.a())
        );
    }

    public Vec4 applyInverse(Vec4 source) {
        return new Vec4(
                (matrix[0][0] * source.x()) + (matrix[1][0] * source.y()) + (matrix[2][0] * source.z()) - ((matrix[0][0] * matrix[0][3] + matrix[1][0] * matrix[1][3] + matrix[2][0] * matrix[2][3]) * source.a()),
                (matrix[0][1] * source.x()) + (matrix[1][1] * source.y()) + (matrix[2][1] * source.z()) - ((matrix[0][1] * matrix[0][3] + matrix[1][1] * matrix[1][3] + matrix[2][1] * matrix[2][3]) * source.a()),
                (matrix[0][2] * source.x()) + (matrix[1][2] * source.y()) + (matrix[2][2] * source.z()) - ((matrix[0][2] * matrix[0][3] + matrix[1][2] * matrix[1][3] + matrix[2][2] * matrix[2][3]) * source.a()),
                (matrix[3][0] * source.x()) + (matrix[3][1] * source.y()) + (matrix[3][2] * source.z()) + (matrix[3][3] * source.a())
        );
    }

//    public Builder builder() {
//        return new Builder();
//    }
//
//    // convenience methods for taking Axis and angle (in degrees) and building a matrix from those
//    public static class Builder {
//        // rotation angles in degrees
//        private float angleX;
//        private float angleY;
//        private float angleZ;
//        private Vec4 translation = new Vec4();
//
//        public Builder() {
//
//        }
//
//        public Builder x(float x) {
//            angleX = x;
//            return this;
//        }
//
//        public Builder y(float y) {
//            angleY = y;
//            return this;
//        }
//
//        public Builder z(float z) {
//            angleZ = z;
//            return this;
//        }
//
//        public Builder translation(Vec4 translation) {
//            this.translation = translation;
//            return this;
//        }
//
//        public RotationMatrix build() {
//            // 1. Calculate trigonometric values for each axis
//            float cx = (float) cos(angleX);
//            float sx = (float) sin(angleX);
//            float cy = (float) cos(angleY);
//            float sy = (float) sin(angleY);
//            float cz = (float) cos(angleZ);
//            float sz = (float) sin(angleZ);
//
//            return new RotationMatrix(
//                    new float[][]{
//                            {cy * cz,
//                                    cz * sx * sy - cx * sz,
//                                    cy * sz,
//                                    translation.x()},
//                            {cy * sz,
//                                    cx * cz + sx * sy * sz,
//                                    -cz * sx + cx * sy * sz,
//                                    translation.y()},
//                            {-sy,
//                                    cy * sx,
//                                    cx * cy,
//                                    translation.z()},
//                            {0f, 0f, 0f, 1f}
//                    }
//            );
//        }
//    }
}
