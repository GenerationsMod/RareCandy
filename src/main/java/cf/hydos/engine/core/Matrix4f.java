package cf.hydos.engine.core;

public class Matrix4f {
    public final org.joml.Matrix4f wrapped;

    public Matrix4f(org.joml.Matrix4f from) {
        this.wrapped = new org.joml.Matrix4f(from);
    }

    public Matrix4f() {
        this.wrapped = new org.joml.Matrix4f();
    }

    public Matrix4f(Matrix4f old) {
        this.wrapped = new org.joml.Matrix4f(old.wrapped);
    }

    public Matrix4f identity() {
        wrapped.identity();
        return this;
    }

    public Matrix4f translate(float x, float y, float z) {
        wrapped.translate(x, y, z);
        return this;
    }

    public Matrix4f scale(float x, float y, float z) {
        wrapped.scale(x, y, z);
        return this;
    }

    public Matrix4f mul(Matrix4f r) {
        wrapped.mul(r.wrapped);
        return this;
    }
}
