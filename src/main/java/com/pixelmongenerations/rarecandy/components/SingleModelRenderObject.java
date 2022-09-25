package com.pixelmongenerations.rarecandy.components;

import com.pixelmongenerations.pixelmonassetutils.scene.material.Material;
import com.pixelmongenerations.pixelmonassetutils.scene.material.Texture;
import com.pixelmongenerations.rarecandy.rendering.shader.ShaderProgram;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL30C;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;

public abstract class SingleModelRenderObject extends RenderObject {

    protected int ebo;
    protected int vbo;
    protected int vao;

    public void upload(ShaderProgram program, FloatBuffer vertices, IntBuffer indices, List<Texture> diffuseTextures) {
        this.shaderProgram = program;

        materials = diffuseTextures.stream().map(Material::new).collect(Collectors.toList());
        this.variants = materials.stream().collect(Collectors.toMap(mat -> mat.diffuseTexture.name, mat -> mat));

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
}
