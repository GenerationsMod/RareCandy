package com.pixelmongenerations.rarecandy.components;

import com.pixelmongenerations.rarecandy.core.VertexLayout;
import com.pixelmongenerations.rarecandy.rendering.shader.ShaderProgram;
import com.pixelmongenerations.pixelmonassetutils.scene.material.Material;
import com.pixelmongenerations.pixelmonassetutils.scene.material.Texture;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL30C;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;

public class StaticRenderObject extends GameComponent {
    public ShaderProgram shaderProgram;
    public Material material;

    private int indexCount;
    private VertexLayout layout;
    private int vbo;
    private int ebo;

    public void addVertices(ShaderProgram program, FloatBuffer vertices, IntBuffer indices, Texture diffuseTexture) {
        this.shaderProgram = program;
        material = new Material(diffuseTexture);

        this.vbo = GL15C.glGenBuffers(); // VertexBufferObject (Vertices)
        this.ebo = GL15C.glGenBuffers(); // ElementBufferObject (Indices)
        indexCount = indices.capacity();

        int vao = GL30C.glGenVertexArrays();
        GL30C.glBindVertexArray(vao);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        this.layout = new VertexLayout(vao,
                new VertexLayout.AttribLayout(3, GL11C.GL_FLOAT), // Position
                new VertexLayout.AttribLayout(2, GL11C.GL_FLOAT), // TexCoords
                new VertexLayout.AttribLayout(3, GL11C.GL_FLOAT), // Normal
                new VertexLayout.AttribLayout(3, GL11C.GL_FLOAT), // ???
                new VertexLayout.AttribLayout(4, GL11C.GL_FLOAT), // BoneData
                new VertexLayout.AttribLayout(4, GL11C.GL_FLOAT) // BoneData
        );

        layout.applyTo(ebo, vbo);
    }

    @Override
    public void render(Matrix4f projectionMatrix, Matrix4f viewMatrix) {
        shaderProgram.bind();
        shaderProgram.updateUniforms(GetTransform(), material, projectionMatrix, viewMatrix);
        this.layout.bind();
        GL15C.glBindBuffer(GL15C.GL_STATIC_DRAW, this.vbo);
        GL15C.glBindBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, this.ebo);
        GL11C.glDisable(GL11C.GL_CULL_FACE);
        GL11C.glDrawElements(GL11C.GL_TRIANGLES, this.indexCount, GL11C.GL_UNSIGNED_INT, 0);
        GL11C.glEnable(GL11C.GL_CULL_FACE);
    }

    @Override
    public void update() {
    }
}
