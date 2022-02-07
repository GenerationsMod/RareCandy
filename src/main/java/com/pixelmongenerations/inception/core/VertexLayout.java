package com.pixelmongenerations.inception.core;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL45C;

public class VertexLayout {

    /**
     * VertexArrayObject (Vertex Layout)
     */
    private final int vao;
    private final int attribCount;
    private final int stride;

    public VertexLayout(AttribLayout... attribLayouts) {
        this.vao = GL45C.glCreateVertexArrays();
        this.attribCount = attribLayouts.length;

        enable();
        int offset = 0;
        for (int i = 0; i < attribLayouts.length; i++) {
            AttribLayout attrib = attribLayouts[i];

            GL45C.glVertexArrayAttribBinding(vao, i, 0);
            GL45C.glVertexArrayAttribFormat(vao, i, attrib.size, attrib.glType, false, offset);

            offset = offset + switch (attrib.glType) {
                case GL11C.GL_FLOAT, GL11C.GL_INT -> Float.BYTES * attrib.size;
                default -> throw new IllegalStateException("Unexpected value: " + attrib.glType);
            };
        }

        this.stride = offset;
    }

    public void bind() {
        GL30C.glBindVertexArray(this.vao);
    }

    public void applyTo(int ebo, int vbo) {
        GL45C.glVertexArrayVertexBuffer(vao, 0, vbo, 0, this.stride);
        GL45C.glVertexArrayElementBuffer(vao, ebo);
    }

    private void enable() {
        for (int i = 0; i < attribCount; i++) {
            GL45C.glEnableVertexArrayAttrib(vao, i);
        }
    }

    public record AttribLayout(int size, int glType) {
    }
}
