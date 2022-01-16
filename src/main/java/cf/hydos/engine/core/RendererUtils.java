package cf.hydos.engine.core;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

public class RendererUtils {

    public static Matrix4f toRotationMatrix(Quaternion q) {
        Vector3f forward = new Vector3f(2.0f * (q.x() * q.z() - q.w() * q.y()), 2.0f * (q.y() * q.z() + q.w() * q.x()), 1.0f - 2.0f * (q.x() * q.x() + q.y() * q.y()));
        Vector3f up = new Vector3f(2.0f * (q.x() * q.y() + q.w() * q.z()), 1.0f - 2.0f * (q.x() * q.x() + q.z() * q.z()), 2.0f * (q.y() * q.z() - q.w() * q.x()));
        Vector3f right = new Vector3f(1.0f - 2.0f * (q.y() * q.y() + q.z() * q.z()), 2.0f * (q.x() * q.y() - q.w() * q.z()), 2.0f * (q.x() * q.z() + q.w() * q.y()));

        return new Matrix4f().identity().rotate(forward, up, right);
    }

    public static Matrix4f WeirdMul(Matrix4f left, Matrix4f right) {
        Matrix4f res = new Matrix4f();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                res.set(i, j,
                        left.m[i][0] * right.get(0, j) +
                                left.m[i][1] * right.get(1, j) +
                                left.m[i][2] * right.get(2, j) +
                                left.m[i][3] * right.get(3, j));
            }
        }

        return res;
    }

    public static FloatBuffer CreateFlippedBuffer(Matrix4f value) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(4 * 4);

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                buffer.put(value.get(i, j));
            }
        }

        return buffer.flip();
    }
}
