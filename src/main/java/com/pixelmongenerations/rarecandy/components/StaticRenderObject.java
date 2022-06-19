package com.pixelmongenerations.rarecandy.components;

import com.pixelmongenerations.pixelmonassetutils.scene.material.Material;
import com.pixelmongenerations.pixelmonassetutils.scene.material.Texture;
import com.pixelmongenerations.rarecandy.core.VertexLayout;
import com.pixelmongenerations.rarecandy.rendering.InstanceState;
import com.pixelmongenerations.rarecandy.rendering.shader.ShaderProgram;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL30C;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;

public class StaticRenderObject extends SingleModelRenderObject {

    @Override
    public void upload(ShaderProgram program, FloatBuffer vertices, IntBuffer indices, Texture diffuseTexture) {
        this.shaderProgram = program;
        material = new Material(diffuseTexture);

        int vbo = GL15C.glGenBuffers(); // VertexBufferObject (Vertices)
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
                new VertexLayout.AttribLayout(3, GL11C.GL_FLOAT) // Normal
        );

        layout.applyTo(ebo, vbo);
    }

    @Override
    public void render(Matrix4f projectionMatrix, List<InstanceState> instances) {
        shaderProgram.bind();
        this.layout.bind();
        GL15C.glBindBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, this.ebo);

        for (InstanceState instance : instances) {
            shaderProgram.updateUniforms(instance.transformationMatrix, material, projectionMatrix, instance.modelViewMatrix);
            GL11C.glDrawElements(GL11C.GL_TRIANGLES, this.indexCount, GL11C.GL_UNSIGNED_INT, 0);
        }
    }
}
