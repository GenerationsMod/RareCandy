package com.pokemod.rarecandy.pipeline;

import org.lwjgl.opengl.GL33C;

public abstract class UniformBlockUploader implements AutoCloseable {
    public static final int MAT4F_SIZE = Float.BYTES * 4 * 4;
    protected final int id;

    public UniformBlockUploader(int size, int index) {
        this.id = GL33C.glGenBuffers();
        bind();
        GL33C.glBufferData(GL33C.GL_UNIFORM_BUFFER, size, GL33C.GL_STATIC_DRAW);
        GL33C.glBindBufferRange(GL33C.GL_UNIFORM_BUFFER, index, id, 0, size);
    }

    public void bind() {
        GL33C.glBindBuffer(GL33C.GL_UNIFORM_BUFFER, id);
    }

    protected void upload(int offset, int size, long pointer) {
        bind();
        GL33C.nglBufferSubData(GL33C.GL_UNIFORM_BUFFER, offset, size, pointer);
    }

    public void close() {
        GL33C.glDeleteBuffers(id);
    }
}
