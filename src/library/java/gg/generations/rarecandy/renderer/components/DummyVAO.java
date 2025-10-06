package gg.generations.rarecandy.renderer.components;

import static org.lwjgl.opengl.GL30C.*;

public final class DummyVAO {
    private final int vaoId;

    public DummyVAO() {
        // Generate VAO
        vaoId = glGenVertexArrays();
        // Bind it once (required by core profile before issuing draw calls)
        glBindVertexArray(vaoId);
        // Leave bound or unbind as needed
        glBindVertexArray(0);
    }

    public void bind() {
        glBindVertexArray(vaoId);
    }

    public void unbind() {
        glBindVertexArray(0);
    }

    public void delete() {
        glDeleteVertexArrays(vaoId);
    }

    public int id() {
        return vaoId;
    }
}