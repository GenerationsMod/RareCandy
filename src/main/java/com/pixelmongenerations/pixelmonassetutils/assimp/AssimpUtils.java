package com.pixelmongenerations.pixelmonassetutils.assimp;

import com.pixelmongenerations.legacy.inception.rendering.Bone;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.assimp.*;

import java.util.Objects;

/**
 * Utilities for converting from Assimp objects.
 */
public class AssimpUtils {

    public static Vector3f from(AIVector3D aiVec3) {
        return new Vector3f(aiVec3.x(), aiVec3.y(), aiVec3.z());
    }

    public static Vector2f from3to2(AIVector3D aiVec3) {
        return new Vector2f(aiVec3.x(), aiVec3.y());
    }

    public static Matrix4f from(AIMatrix4x4 aiMat4) {
        Matrix4f dest = new Matrix4f();
        dest.m00(aiMat4.a1());
        dest.m10(aiMat4.a2());
        dest.m20(aiMat4.a3());
        dest.m30(aiMat4.a4());
        dest.m01(aiMat4.b1());
        dest.m11(aiMat4.b2());
        dest.m21(aiMat4.b3());
        dest.m31(aiMat4.b4());
        dest.m02(aiMat4.c1());
        dest.m12(aiMat4.c2());
        dest.m22(aiMat4.c3());
        dest.m32(aiMat4.c4());
        dest.m03(aiMat4.d1());
        dest.m13(aiMat4.d2());
        dest.m23(aiMat4.d3());
        dest.m33(aiMat4.d4());
        return dest;
    }

    public static Quaternionf from(AIQuaternion aiQuat) {
        return new Quaternionf(aiQuat.x(), aiQuat.y(), aiQuat.z(), aiQuat.w());
    }

    public static Bone from(AIBone bone) {
        Bone b = new Bone();
        b.offsetMatrix = from(bone.mOffsetMatrix());
        b.name = bone.mName().dataString();

        AIVertexWeight.Buffer aiWeights = Objects.requireNonNull(bone.mWeights());
        Bone.VertexWeight[] vertexWeights = new Bone.VertexWeight[aiWeights.capacity()];
        for (int i = 0; i < aiWeights.capacity(); i++) {
            AIVertexWeight aiWeight = aiWeights.get(i);
            vertexWeights[i] = new Bone.VertexWeight(aiWeight.mVertexId(), aiWeight.mWeight());
        }

        b.weights = vertexWeights;
        return b;
    }
}
