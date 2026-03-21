package gg.generations.framebuffertest.second;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL30.*;

public final class FullscreenTriangle implements AutoCloseable {
    private final int vao;

    public FullscreenTriangle() {
        vao = glGenVertexArrays();
    }

    public void draw() {
        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, 3);
        glBindVertexArray(0);
    }

    @Override
    public void close() {
        glDeleteVertexArrays(vao);
    }
}