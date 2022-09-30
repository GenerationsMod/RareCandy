package com.pixelmongenerations.rarecandy.rendering;

import org.lwjgl.opengl.*;

public class VertexLayout {

    /**
     * VertexArrayObject (Vertex Layout)
     */
    private final int vao;
    private final int attribCount;
    public final AttribLayout[] attribLayouts;

    public VertexLayout(int vao, AttribLayout... attribLayouts) {
        this.attribCount = attribLayouts.length;
        this.vao = vao;
        this.attribLayouts = attribLayouts;

        int stride = 0;
        for (AttribLayout attrib : attribLayouts) {
            stride += switch (attrib.glType) {
                case GL11C.GL_FLOAT, GL11C.GL_INT -> Float.BYTES * attrib.size;
                default -> throw new IllegalStateException("Unexpected value: " + attrib.glType);
            };
        }
        int stride1 = stride;

        enable();
        int offset = 0;
        for (int i = 0; i < attribLayouts.length; i++) {
            AttribLayout attrib = attribLayouts[i];

            GL20C.glVertexAttribPointer(i, attrib.size, attrib.glType, false, stride1, offset);

            offset += switch (attrib.glType) {
                case GL11C.GL_FLOAT, GL11C.GL_INT -> Float.BYTES * attrib.size;
                default -> throw new IllegalStateException("Unexpected value: " + attrib.glType);
            };
        }
    }

    public void bind() {
        GL30C.glBindVertexArray(this.vao);
    }

    private void enable() {
        for (int i = 0; i < attribCount; i++) {
            GL20C.glEnableVertexAttribArray(i);
        }
    }

    public record AttribLayout(int size, int glType, String name) {
    }
}
