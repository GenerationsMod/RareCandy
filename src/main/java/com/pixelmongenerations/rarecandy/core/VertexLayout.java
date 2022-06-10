package com.pixelmongenerations.rarecandy.core;

import org.lwjgl.opengl.*;

public class VertexLayout {

    /**
     * VertexArrayObject (Vertex Layout)
     */
    private final int vao;
    private final int attribCount;
    private final int stride;

    public VertexLayout(int vao, AttribLayout... attribLayouts) {
        this.attribCount = attribLayouts.length;
        this.vao = vao;

        int stride = 0;
        for (AttribLayout attrib : attribLayouts) {
            stride += switch (attrib.glType) {
                case GL11C.GL_FLOAT, GL11C.GL_INT -> Float.BYTES * attrib.size;
                default -> throw new IllegalStateException("Unexpected value: " + attrib.glType);
            };
        }
        this.stride = stride;

        enable();
        int offset = 0;
        for (int i = 0; i < attribLayouts.length; i++) {
            AttribLayout attrib = attribLayouts[i];

            GL20C.glVertexAttribPointer(i, attrib.size, attrib.glType, false, this.stride, offset);

            offset += switch (attrib.glType) {
                case GL11C.GL_FLOAT, GL11C.GL_INT -> Float.BYTES * attrib.size;
                default -> throw new IllegalStateException("Unexpected value: " + attrib.glType);
            };
        }
    }

    public void bind() {
        GL30C.glBindVertexArray(this.vao);
    }

    public void applyTo(int ebo, int vbo) {
        // No-Op on legacy. Binding is done at the top
        //GL45C.glVertexArrayVertexBuffer(vao, 0, vbo, 0, this.stride);
        //GL45C.glVertexArrayElementBuffer(vao, ebo);
    }

    private void enable() {
        for (int i = 0; i < attribCount; i++) {
            GL20C.glEnableVertexAttribArray(i);
        }
    }

    public record AttribLayout(int size, int glType) {
    }
}
