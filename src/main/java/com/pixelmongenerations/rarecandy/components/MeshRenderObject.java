package com.pixelmongenerations.rarecandy.components;

import com.pixelmongenerations.pkl.scene.material.Material;
import com.pixelmongenerations.pkl.scene.material.Texture;
import com.pixelmongenerations.pkl.scene.objects.Mesh;
import com.pixelmongenerations.rarecandy.pipeline.Pipeline;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL30C;

import java.util.List;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;

public abstract class MeshRenderObject extends RenderObject {

    protected int ebo; // ElementBufferObject (Indices)
    protected int vbo; // VertexBufferObject (Vertices)
    protected int vao; // VertexArrayObject (Layout)

    public void upload(Mesh mesh, Pipeline pipeline, List<Texture> diffuseTextures) {
        this.pipeline = pipeline;
        this.materials = diffuseTextures.stream().map(Material::new).collect(Collectors.toList());
        this.variants = materials.stream().collect(Collectors.toMap(mat -> mat.diffuseTexture.name, mat -> mat));
        this.vbo = GL15C.glGenBuffers();
        this.ebo = GL15C.glGenBuffers();
        this.vao = GL30C.glGenVertexArrays();

        var indexBuffer = pipeline.indexBuilder().apply(mesh);
        var vertexBuffer = pipeline.vertexBufferBuilder().apply(mesh);

        this.indexCount = indexBuffer.capacity();
        GL30C.glBindVertexArray(vao);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
    }
}
