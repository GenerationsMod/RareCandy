package com.pixelmongenerations.rarecandy.components;

import com.pixelmongenerations.pkl.reader.TextureReference;
import com.pixelmongenerations.pkl.scene.material.Material;
import com.pixelmongenerations.pkl.scene.objects.Mesh;
import com.pixelmongenerations.rarecandy.pipeline.Pipeline;
import com.pixelmongenerations.rarecandy.rendering.InstanceState;
import com.pixelmongenerations.rarecandy.rendering.RareCandy;
import com.pixelmongenerations.rarecandy.rendering.VertexLayout;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL30C;

import java.util.List;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL15.*;

public abstract class MeshRenderObject extends RenderObject {

    protected int ebo; // ElementBufferObject (Indices)
    protected int vbo; // VertexBufferObject (Vertices)
    protected int vao; // VertexArrayObject (Layout)
    protected VertexLayout layout;
    private Runnable uploadTask;

    public void upload(Mesh mesh, Pipeline pipeline, List<TextureReference> diffuseTextures) {}

    public void createUploadTask(Mesh mesh, Pipeline pipeline, List<TextureReference> diffuseTextures, VertexLayout.AttribLayout... attribs) {
        this.uploadTask = () -> {
            try {
                this.pipeline = pipeline;
                this.materials = diffuseTextures.stream().map(Material::new).collect(Collectors.toList());
                this.variants = materials.stream().collect(Collectors.toMap(mat -> mat.getDiffuseTexture().name, mat -> mat));
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

                this.layout = new VertexLayout(vao, attribs);
                this.ready = true;
            } catch (Exception e) {
                RareCandy.fatal(e.getMessage());
            }
        };
    }

    @Override
    public boolean isReady() {
        if (uploadTask != null) {
            uploadTask.run();
        }

        uploadTask = null;

        return super.isReady();
    }

    public void render(List<InstanceState> instances) {
    }
}
