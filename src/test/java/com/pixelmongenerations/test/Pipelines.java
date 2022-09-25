package com.pixelmongenerations.test;

import com.pixelmongenerations.rarecandy.core.VertexLayout;
import com.pixelmongenerations.rarecandy.pipeline.Pipeline;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11C;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.function.Supplier;

public class Pipelines {
    private static final Pipeline.Builder STATIC = new Pipeline.Builder()
            .shader(
                    builtin("static/static.vs.glsl"),
                    builtin("static/static.fs.glsl"),
                    Collections.emptyMap(),
                    new VertexLayout.AttribLayout(3, GL11C.GL_FLOAT, "inPosition"),
                    new VertexLayout.AttribLayout(2, GL11C.GL_FLOAT, "inTexCoords"),
                    new VertexLayout.AttribLayout(3, GL11C.GL_FLOAT, "inNormal")
            )
            .meshBuilder(mesh -> {
                var sizeOfVertex = Float.BYTES * 3 + Float.BYTES * 2 + Float.BYTES * 3;
                var rawMeshData = new float[mesh.getVertices().length * sizeOfVertex];
                var index = 0;

                for (var i = 0; i < mesh.getVertices().length; i++) {
                    Vector3f position = mesh.getVertices()[i];
                    Vector3f normal = mesh.getNormals()[i];
                    Vector2f texCoord = mesh.getTexCoords()[i];

                    rawMeshData[index++] = position.x();
                    rawMeshData[index++] = position.y();
                    rawMeshData[index++] = position.z();

                    rawMeshData[index++] = texCoord.x();
                    rawMeshData[index++] = texCoord.y();

                    rawMeshData[index++] = normal.x();
                    rawMeshData[index++] = normal.y();
                    rawMeshData[index++] = normal.z();
                }

                return createVertexBuffer(rawMeshData);
            })
            .indexBuilder(mesh -> createIndexBuffer(mesh.getIndices()))
            .supplyUniform("modelMatrix", (ctx) -> ctx.uniform().uploadMat4f(ctx.instance().modelMatrix));

    public static Pipeline staticPipeline(Supplier<Matrix4f> viewMatrix, Supplier<Matrix4f> projectionMatrix) {
        return new Pipeline.Builder(STATIC)
                .supplyUniform("viewMatrix", (ctx) -> ctx.uniform().uploadMat4f(viewMatrix.get()))
                .supplyUniform("projectionMatrix", (ctx) -> ctx.uniform().uploadMat4f(projectionMatrix.get()))
                .build();
    }

    private static String builtin(String name) {
        try (var is = Pipelines.class.getResourceAsStream("/shaders/" + name)) {
            assert is != null;
            return new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read built in shader", e);
        }
    }

    private static FloatBuffer createVertexBuffer(float[] rawMeshData) {
        FloatBuffer vertBuffer = BufferUtils.createFloatBuffer(rawMeshData.length);
        for (float v : rawMeshData) vertBuffer.put(v);
        return vertBuffer.flip();
    }

    private static IntBuffer createIndexBuffer(int[] indices) {
        IntBuffer pIndices = BufferUtils.createIntBuffer(indices.length);
        for (int i : indices) pIndices.put(i);
        return pIndices.flip();
    }
}
