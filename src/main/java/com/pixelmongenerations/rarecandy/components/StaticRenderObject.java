package com.pixelmongenerations.rarecandy.components;

import com.pixelmongenerations.pixelmonassetutils.PixelAsset;
import com.pixelmongenerations.pixelmonassetutils.scene.Scene;
import com.pixelmongenerations.pixelmonassetutils.scene.objects.Mesh;
import com.pixelmongenerations.rarecandy.core.VertexLayout;
import com.pixelmongenerations.rarecandy.rendering.shader.ShaderProgram;
import com.pixelmongenerations.pixelmonassetutils.scene.material.Material;
import com.pixelmongenerations.pixelmonassetutils.scene.material.Texture;
import com.sun.tools.javac.Main;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AITexture;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL30C;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;

public class StaticRenderObject extends RenderObject {
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
                new VertexLayout.AttribLayout(3, GL11C.GL_FLOAT) // Normal
        );

        layout.applyTo(ebo, vbo);
    }

    @Override
    public void render(Matrix4f projectionMatrix, Matrix4f viewMatrix) {
        shaderProgram.bind();
        shaderProgram.updateUniforms(getTransformationMatrix(), material, projectionMatrix, viewMatrix);
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

    public static StaticRenderObject loadStaticFile(Scene scene, AIScene aiScene, int textureIndex) {
        int sizeOfVertex = Float.BYTES * 3 + Float.BYTES * 2 + Float.BYTES * 3;

        for (Mesh mesh : scene.meshes) {
            float[] rawMeshData = new float[mesh.getVertices().length * sizeOfVertex];
            int index = 0;

            for (int v = 0; v < mesh.getVertices().length; v++) {
                Vector3f position = mesh.getVertices()[v];
                Vector3f normal = mesh.getNormals()[v];
                Vector2f texCoord = mesh.getTexCoords()[v];

                rawMeshData[index++] = position.x();
                rawMeshData[index++] = position.y();
                rawMeshData[index++] = position.z();

                rawMeshData[index++] = texCoord.x();
                rawMeshData[index++] = texCoord.y();

                rawMeshData[index++] = normal.x();
                rawMeshData[index++] = normal.y();
                rawMeshData[index++] = normal.z();
            }

            StaticRenderObject component = new StaticRenderObject();
            FloatBuffer vertBuffer = BufferUtils.createFloatBuffer(rawMeshData.length);

            IntBuffer indices = BufferUtils.createIntBuffer(mesh.getIndices().length);
            for (int i : mesh.getIndices()) {
                indices.put(i);
            }
            indices.flip();

            for (float v : rawMeshData) vertBuffer.put(v);
            vertBuffer.flip();

            List<AITexture> rawTextures = new ArrayList<>();

            // Retrieve Textures
            PointerBuffer pTextures = aiScene.mTextures();
            if (pTextures != null) {
                for (int i = 0; i < aiScene.mNumTextures(); i++) {
                    rawTextures.add(AITexture.create(pTextures.get(i)));
                }
            } else {
                throw new RuntimeException("How do you expect us to render without textures? Use colours? we don't support that yet!");
            }

            // Try to load the textures into rosella
            List<Texture> textures = new ArrayList<>();
            for (AITexture rawTexture : rawTextures) {
                if (rawTexture.mHeight() > 0) {
                    throw new RuntimeException(".glb file had texture with height of 0");
                } else {
                    textures.add(new Texture(rawTexture.pcDataCompressed(), rawTexture.mFilename().dataString()));
                }
            }

            component.addVertices(ShaderProgram.STATIC_SHADER, vertBuffer, indices, textures.get(textureIndex));
            return component;
        }
        throw new RuntimeException("Failed to create static object.");
    }
}
