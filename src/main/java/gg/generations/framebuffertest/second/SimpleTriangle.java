package gg.generations.framebuffertest.second;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public final class SimpleTriangle implements AutoCloseable {
    private final int vao;
    private final int vbo;

    public SimpleTriangle() {
        vao = glGenVertexArrays();
        vbo = glGenBuffers();

        float[] vertices = {
                -0.6f, -0.5f, 0.0f,
                 0.6f, -0.5f, 0.0f,
                 0.0f,  0.6f, 0.0f
        };

        FloatBuffer buffer = MemoryUtil.memAllocFloat(vertices.length);
        buffer.put(vertices).flip();

        glBindVertexArray(vao);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0L);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        MemoryUtil.memFree(buffer);
    }

    public void draw() {
        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, 3);
        glBindVertexArray(0);
    }

    @Override
    public void close() {
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
    }
}