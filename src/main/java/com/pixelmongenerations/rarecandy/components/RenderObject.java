package com.pixelmongenerations.rarecandy.components;

import com.pixelmongenerations.pixelmonassetutils.scene.material.Material;
import com.pixelmongenerations.pixelmonassetutils.scene.material.Texture;
import com.pixelmongenerations.rarecandy.core.VertexLayout;
import com.pixelmongenerations.rarecandy.rendering.InstanceState;
import com.pixelmongenerations.rarecandy.rendering.shader.ShaderProgram;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL30C;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;

public class RenderObject {
    protected ShaderProgram shaderProgram;
    protected Material material;
    protected int indexCount;
    protected VertexLayout layout;
    protected int ebo;
    protected int vbo;
    protected int vao;

    public void addVertices(ShaderProgram program, FloatBuffer vertices, IntBuffer indices, Texture diffuseTexture) {
        this.shaderProgram = program;

        material = new Material(diffuseTexture);

        this.vbo = GL15C.glGenBuffers(); // VertexBufferObject (Vertices)
        this.ebo = GL15C.glGenBuffers(); // ElementBufferObject (Indices)
        indexCount = indices.capacity();

        this.vao = GL30C.glGenVertexArrays();
        GL30C.glBindVertexArray(vao);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
    }

    public void render(Matrix4f projectionMatrix, List<InstanceState> instances) {
    }

    public void update() {
    }
}

