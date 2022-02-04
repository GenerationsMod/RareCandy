package cf.hydos.engine.components;

import cf.hydos.engine.rendering.shader.ShaderProgram;
import cf.hydos.pixelmonassetutils.scene.material.Material;
import cf.hydos.pixelmonassetutils.scene.material.Texture;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL45C;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class StaticRenderObject extends GameComponent {
    public ShaderProgram shaderProgram;
    public Material material;

    private int vao;
    private int indexCount;

    public void addVertices(ShaderProgram program, FloatBuffer vertices, IntBuffer indices, Texture diffuseTexture) {
        this.shaderProgram = program;
        material = new Material(diffuseTexture);

        vao = GL45C.glCreateVertexArrays(); // VertexArrayObject (Vertex Layout)
        int vbo = GL45C.glCreateBuffers(); // VertexBufferObject (Vertices)
        int ebo = GL45C.glCreateBuffers(); // ElementBufferObject (Indices)
        indexCount = indices.capacity();

        GL45C.glNamedBufferData(vbo, vertices, GL45C.GL_STATIC_DRAW);
        GL45C.glNamedBufferData(ebo, indices, GL45C.GL_STATIC_DRAW);

        GL45C.glEnableVertexArrayAttrib(vao, 0);
        GL45C.glEnableVertexArrayAttrib(vao, 1);
        GL45C.glEnableVertexArrayAttrib(vao, 2);

        GL45C.glVertexArrayAttribBinding(vao, 0, 0);
        GL45C.glVertexArrayAttribFormat(vao, 0, 3, GL11C.GL_FLOAT, false, 0);

        GL45C.glVertexArrayAttribBinding(vao, 1, 0);
        GL45C.glVertexArrayAttribFormat(vao, 1, 2, GL11C.GL_FLOAT, false, 12);

        GL45C.glVertexArrayAttribBinding(vao, 2, 0);
        GL45C.glVertexArrayAttribFormat(vao, 2, 3, GL11C.GL_FLOAT, false, 20);

        GL45C.glVertexArrayVertexBuffer(vao, 0, vbo, 0, 8 * 4);
        GL45C.glVertexArrayElementBuffer(vao, ebo);
    }

    @Override
    public void render(Matrix4f projViewMatrix) {
        shaderProgram.bind();
        shaderProgram.updateUniforms(GetTransform(), material, projViewMatrix);
        GL30C.glBindVertexArray(this.vao);
        GL11C.glDisable(GL11C.GL_CULL_FACE);
        GL11C.glDrawElements(GL11C.GL_TRIANGLES, this.indexCount, GL11C.GL_UNSIGNED_INT, 0);
        GL11C.glEnable(GL11C.GL_CULL_FACE);
    }

    @Override
    public void update() {
    }
}