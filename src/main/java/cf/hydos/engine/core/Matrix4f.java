package cf.hydos.engine.core;

import org.joml.Vector3f;

public class Matrix4f {
    public final float[][] m;

    public Matrix4f() {
        m = new float[4][4];
    }

    public Matrix4f identity() {
        m[0][0] = 1;
        m[0][1] = 0;
        m[0][2] = 0;
        m[0][3] = 0;
        m[1][0] = 0;
        m[1][1] = 1;
        m[1][2] = 0;
        m[1][3] = 0;
        m[2][0] = 0;
        m[2][1] = 0;
        m[2][2] = 1;
        m[2][3] = 0;
        m[3][0] = 0;
        m[3][1] = 0;
        m[3][2] = 0;
        m[3][3] = 1;

        return this;
    }

    public Matrix4f translate(float x, float y, float z) {
        m[0][3] = x;
        m[1][3] = y;
        m[2][3] = z;
        return this;
    }

    public Matrix4f scale(float x, float y, float z) {
        m[0][0] = x;
        m[1][1] = y;
        m[2][2] = z;
        return this;
    }

    public Matrix4f rotate(Vector3f forward, Vector3f up, Vector3f right) {
        m[0][0] = right.x();
        m[0][1] = right.y();
        m[0][2] = right.z();
        m[1][0] = up.x();
        m[1][1] = up.y();
        m[1][2] = up.z();
        m[2][0] = forward.x();
        m[2][1] = forward.y();
        m[2][2] = forward.z();
        return this;
    }

    public Matrix4f WeirdMul(Matrix4f r) {
        return RendererUtils.WeirdMul(this, r);
    }

    public float get(int x, int y) {
        return m[x][y];
    }

    public void set(int x, int y, float value) {
        m[x][y] = value;
    }
}
