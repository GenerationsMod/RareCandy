package gg.generations.rarecandy.renderer.ubo;

import org.lwjgl.opengl.GL33C;

import java.io.Closeable;

public abstract class UniformBlockUploader implements Closeable {
    public static final int MAT4F_SIZE = Float.BYTES * 4 * 4;
    public static final int VEC2_SIZE = Float.BYTES * 4;
    public static final int VEC3_SIZE = Float.BYTES * 4;
    public static final int BOOL_SIZE = Float.BYTES * 4;
    public static final int INT_SIZE = Float.BYTES * 4;
    public static final int FLOAT_SIZE = Float.BYTES * 4;

    protected int id;
    private final int size;
    private final int index;

    private boolean initalized = false;

    public UniformBlockUploader(int size, int index) {
        this.size = size;
        this.index = index;
    }

    public void bind() {
        GL33C.glBindBuffer(GL33C.GL_UNIFORM_BUFFER, id);
    }

    protected void upload(int offset, int size, long pointer) {
        bind();
        GL33C.nglBufferSubData(GL33C.GL_UNIFORM_BUFFER, offset, size, pointer);
    }

    public void initalize() {
        if(!initalized) {
            this.id = GL33C.glGenBuffers();
            bind();
            GL33C.glBufferData(GL33C.GL_UNIFORM_BUFFER, size, GL33C.GL_STATIC_DRAW);
            GL33C.glBindBufferRange(GL33C.GL_UNIFORM_BUFFER, index, id, 0, size);
        }
    }

    public void close() {
        GL33C.glDeleteBuffers(id);
    }
}
