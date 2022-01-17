package cf.hydos.engine.core;

public class Matrix4f {
    public final org.joml.Matrix4f wrapped;

    public Matrix4f(org.joml.Matrix4f from) {
        this.wrapped = new org.joml.Matrix4f(from);
    }

    public Matrix4f() {
        this.wrapped = new org.joml.Matrix4f();
    }

    public Matrix4f identity() {
        wrapped.identity();
        return this;
    }

    public Matrix4f translate(float x, float y, float z) {
        wrapped.m30(x);
        wrapped.m31(y);
        wrapped.m32(z);
        return this;
    }

    public Matrix4f scale(float x, float y, float z) {
        wrapped.m00(x);
        wrapped.m11(y);
        wrapped.m22(z);
        return this;
    }

    public Matrix4f mul(Matrix4f r) {
        return new Matrix4f(wrapped.mul(r.wrapped, new org.joml.Matrix4f()));
    }
}
