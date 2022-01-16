package cf.hydos.pixelmonassetutils;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.assimp.AIMatrix4x4;
import org.lwjgl.assimp.AIQuaternion;
import org.lwjgl.assimp.AIVector3D;

/**
 * Utilities for converting from Assimp objects.
 */
public class AssimpUtils {

    public static Vector3f from(AIVector3D aiVec3) {
        return new Vector3f(aiVec3.x(), aiVec3.y(), aiVec3.z());
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

    @Deprecated
    public static cf.hydos.engine.core.Matrix4f fromOld(AIMatrix4x4 m) {
        cf.hydos.engine.core.Matrix4f matrix4f = new cf.hydos.engine.core.Matrix4f();
        matrix4f.m[0][0] = m.a1();
        matrix4f.m[0][1] = m.a2();
        matrix4f.m[0][2] = m.a3();
        matrix4f.m[0][3] = m.a4();

        matrix4f.m[1][0] = m.b1();
        matrix4f.m[1][1] = m.b2();
        matrix4f.m[1][2] = m.b3();
        matrix4f.m[1][3] = m.b4();

        matrix4f.m[2][0] = m.c1();
        matrix4f.m[2][1] = m.c2();
        matrix4f.m[2][2] = m.c3();
        matrix4f.m[2][3] = m.c4();

        matrix4f.m[3][0] = m.d1();
        matrix4f.m[3][1] = m.d2();
        matrix4f.m[3][2] = m.d3();
        matrix4f.m[3][3] = m.d4();

        return matrix4f;
    }

    public static Quaternionf from(AIQuaternion aiQuat) {
        return new Quaternionf(aiQuat.x(), aiQuat.y(), aiQuat.z(), aiQuat.w());
    }
}
