package cf.hydos.engine.core;

import org.joml.Vector3f;
import org.lwjgl.assimp.AIMatrix4x4;

public class Matrix4f {
    private final float[][] m;

    public Matrix4f() {
        m = new float[4][4];
    }

    public Matrix4f InitIdentity() {
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

    public Matrix4f InitTranslation(float x, float y, float z) {
        m[0][0] = 1;
        m[0][1] = 0;
        m[0][2] = 0;
        m[0][3] = x;
        m[1][0] = 0;
        m[1][1] = 1;
        m[1][2] = 0;
        m[1][3] = y;
        m[2][0] = 0;
        m[2][1] = 0;
        m[2][2] = 1;
        m[2][3] = z;
        m[3][0] = 0;
        m[3][1] = 0;
        m[3][2] = 0;
        m[3][3] = 1;

        return this;
    }

    public Matrix4f InitScale(float x, float y, float z) {
        m[0][0] = x;
        m[0][1] = 0;
        m[0][2] = 0;
        m[0][3] = 0;
        m[1][0] = 0;
        m[1][1] = y;
        m[1][2] = 0;
        m[1][3] = 0;
        m[2][0] = 0;
        m[2][1] = 0;
        m[2][2] = z;
        m[2][3] = 0;
        m[3][0] = 0;
        m[3][1] = 0;
        m[3][2] = 0;
        m[3][3] = 1;

        return this;
    }

    public Matrix4f InitRotation(Vector3f forward, Vector3f up, Vector3f right) {
        m[0][0] = right.x();
        m[0][1] = right.y();
        m[0][2] = right.z();
        m[0][3] = 0;
        m[1][0] = up.x();
        m[1][1] = up.y();
        m[1][2] = up.z();
        m[1][3] = 0;
        m[2][0] = forward.x();
        m[2][1] = forward.y();
        m[2][2] = forward.z();
        m[2][3] = 0;
        m[3][0] = 0;
        m[3][1] = 0;
        m[3][2] = 0;
        m[3][3] = 1;

        return this;
    }

    public Matrix4f Mul(Matrix4f r) {
        Matrix4f res = new Matrix4f();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                res.Set(i, j, m[i][0] * r.Get(0, j) +
                        m[i][1] * r.Get(1, j) +
                        m[i][2] * r.Get(2, j) +
                        m[i][3] * r.Get(3, j));
            }
        }

        return res;
    }

    public float Get(int x, int y) {
        return m[x][y];
    }

    public void Set(int x, int y, float value) {
        m[x][y] = value;
    }

    public Matrix4f fromAssimp(AIMatrix4x4 m) {
        this.m[0][0] = m.a1();
        this.m[0][1] = m.a2();
        this.m[0][2] = m.a3();
        this.m[0][3] = m.a4();

        this.m[1][0] = m.b1();
        this.m[1][1] = m.b2();
        this.m[1][2] = m.b3();
        this.m[1][3] = m.b4();

        this.m[2][0] = m.c1();
        this.m[2][1] = m.c2();
        this.m[2][2] = m.c3();
        this.m[2][3] = m.c4();

        this.m[3][0] = m.d1();
        this.m[3][1] = m.d2();
        this.m[3][2] = m.d3();
        this.m[3][3] = m.d4();

        return this;
    }
}
