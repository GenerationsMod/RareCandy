package cf.hydos.engine.core;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class RendererUtils {

    public static Matrix4f toRotationMatrix(Quaternionf q) {
        Vector3f forward = new Vector3f(2.0f * (q.x() * q.z() - q.w() * q.y()), 2.0f * (q.y() * q.z() + q.w() * q.x()), 1.0f - 2.0f * (q.x() * q.x() + q.y() * q.y()));
        Vector3f up = new Vector3f(2.0f * (q.x() * q.y() + q.w() * q.z()), 1.0f - 2.0f * (q.x() * q.x() + q.z() * q.z()), 2.0f * (q.y() * q.z() - q.w() * q.x()));
        Vector3f right = new Vector3f(1.0f - 2.0f * (q.y() * q.y() + q.z() * q.z()), 2.0f * (q.x() * q.y() - q.w() * q.z()), 2.0f * (q.x() * q.z() + q.w() * q.y()));

        return rotate(new Matrix4f().identity(), forward, up, right);
    }

    public static Matrix4f rotate(Matrix4f original, Vector3f forward, Vector3f up, Vector3f right) {
        original.wrapped.m00(right.x());
        original.wrapped.m10(right.y());
        original.wrapped.m20(right.z());
        original.wrapped.m01(up.x());
        original.wrapped.m11(up.y());
        original.wrapped.m21(up.z());
        original.wrapped.m02(forward.x());
        original.wrapped.m12(forward.y());
        original.wrapped.m22(forward.z());
        return original;
    }
}
