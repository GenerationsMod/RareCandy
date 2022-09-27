package com.pixelmongenerations.test;

import com.pixelmongenerations.rarecandy.components.AnimatedSolid;
import com.pixelmongenerations.rarecandy.pipeline.Pipeline;
import com.pixelmongenerations.rarecandy.rendering.Bone;
import com.pixelmongenerations.rarecandy.rendering.VertexLayout;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11C;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
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

                return Pipeline.createVertexBuffer(rawMeshData);
            })
            .indexBuilder(mesh -> Pipeline.createIndexBuffer(mesh.getIndices()))
            .supplyUniform("viewMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().viewMatrix()))
            .supplyUniform("modelMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().transformationMatrix()))
            .supplyUniform("lightPosition", ctx -> ctx.uniform().uploadVec3f(new Vector3f(0, 2, 0)))
            .supplyUniform("LIGHT_reflectivity", ctx -> ctx.uniform().uploadFloat(0.3f))
            .supplyUniform("LIGHT_shineDamper", ctx -> ctx.uniform().uploadFloat(0.3f))
            .supplyUniform("LIGHT_color", ctx -> ctx.uniform().uploadVec3f(new Vector3f(1, 1, 1)))
            .supplyUniform("diffuse", ctx -> {
                ctx.object().getMaterial(ctx.instance().materialId()).getDiffuseTexture().bind(0);
                ctx.uniform().uploadInt(0);
            });

    private static final Pipeline.Builder ANIMATED = new Pipeline.Builder()
            .shader(
                    builtin("animated/animated.vs.glsl"),
                    builtin("animated/animated.fs.glsl"),
                    Collections.emptyMap(),
                    new VertexLayout.AttribLayout(3, GL11C.GL_FLOAT, "inPosition"),
                    new VertexLayout.AttribLayout(2, GL11C.GL_FLOAT, "inTexCoords"),
                    new VertexLayout.AttribLayout(3, GL11C.GL_FLOAT, "inNormal"),
                    new VertexLayout.AttribLayout(3, GL11C.GL_FLOAT, "inTangent"),
                    new VertexLayout.AttribLayout(4, GL11C.GL_FLOAT, "boneDataA"),
                    new VertexLayout.AttribLayout(4, GL11C.GL_FLOAT, "boneDataB")
            )
            .meshBuilder(mesh -> {
                int boneInfoOffset = 3 + 2 + 3 + 3;
                int sizeOfVertex = boneInfoOffset + 4 * 2;

                float[] rawMeshData = new float[mesh.getVertices().length * sizeOfVertex];
                int index = 0;

                for (int i = 0; i < mesh.getVertices().length; i++) {
                    Vector3f position = mesh.getVertices()[i];
                    Vector3f normal = mesh.getNormals()[i];
                    Vector3f tangent = mesh.getTangents()[i];
                    Vector2f texCoord = mesh.getTexCoords()[i];

                    rawMeshData[index++] = position.x();
                    rawMeshData[index++] = position.y();
                    rawMeshData[index++] = position.z();

                    rawMeshData[index++] = texCoord.x();
                    rawMeshData[index++] = texCoord.y();

                    rawMeshData[index++] = normal.x();
                    rawMeshData[index++] = normal.y();
                    rawMeshData[index++] = normal.z();

                    rawMeshData[index++] = tangent.x();
                    rawMeshData[index++] = tangent.y();
                    rawMeshData[index++] = tangent.z();

                    rawMeshData[index++] = 0; // Bone ID Bone 1
                    rawMeshData[index++] = 0; // Bone ID Bone 2
                    rawMeshData[index++] = 0; // Weight  Bone 1
                    rawMeshData[index++] = 0; // Weight  Bone 2

                    rawMeshData[index++] = 0; // Bone ID Bone 3
                    rawMeshData[index++] = 0; // Bone ID Bone 4
                    rawMeshData[index++] = 0; // Weight  Bone 3
                    rawMeshData[index++] = 0; // Weight  Bone 4
                }

                HashMap<Integer, Integer> bone_index_map0 = new HashMap<>();
                HashMap<Integer, Integer> bone_index_map1 = new HashMap<>();

                for (int boneId = 0; boneId < mesh.getBones().length; boneId++) {
                    Bone bone = Objects.requireNonNull(mesh.getBones()[boneId]);

                    for (int weightId = 0; weightId < bone.weights.length; weightId++) {
                        Bone.VertexWeight weight = bone.weights[weightId];
                        int vertId = weight.vertexId;
                        int pVertex = vertId * sizeOfVertex; // pointer to where a vertex starts in the array.

                        if (!bone_index_map0.containsKey(vertId)) {
                            rawMeshData[(pVertex + boneInfoOffset)] = boneId;
                            rawMeshData[(pVertex + boneInfoOffset) + 2] = weight.weight;
                            bone_index_map0.put(vertId, 0);
                        } else if (bone_index_map0.get(vertId) == 0) {
                            rawMeshData[(pVertex + boneInfoOffset) + 1] = boneId;
                            rawMeshData[(pVertex + boneInfoOffset) + 3] = weight.weight;
                            bone_index_map0.put(vertId, 1);
                        } else if (!bone_index_map1.containsKey(vertId)) {
                            rawMeshData[(pVertex + boneInfoOffset) + 4] = boneId;
                            rawMeshData[(pVertex + boneInfoOffset) + 6] = weight.weight;
                            bone_index_map1.put(vertId, 0);
                        } else if (bone_index_map1.get(vertId) == 0) {
                            rawMeshData[(pVertex + boneInfoOffset) + 5] = boneId;
                            rawMeshData[(pVertex + boneInfoOffset) + 7] = weight.weight;
                            bone_index_map1.put(vertId, 1);
                        } else {
                            System.err.println("max 4 bones per vertex.");
                            System.exit(0);
                        }
                    }
                }

                return Pipeline.createVertexBuffer(rawMeshData);
            })
            .indexBuilder(mesh -> Pipeline.createIndexBuffer(mesh.getIndices()))
            .supplyUniform("viewMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().viewMatrix()))
            .supplyUniform("modelMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().transformationMatrix()))
            .supplyUniform("lightPosition", ctx -> ctx.uniform().uploadVec3f(new Vector3f(0, 2, 0)))
            .supplyUniform("LIGHT_reflectivity", ctx -> ctx.uniform().uploadFloat(0.1f))
            .supplyUniform("LIGHT_shineDamper", ctx -> ctx.uniform().uploadFloat(0.5f))
            .supplyUniform("LIGHT_color", ctx -> ctx.uniform().uploadVec3f(new Vector3f(1, 1, 1)))
            .supplyUniform("diffuse", ctx -> {
                ctx.object().getMaterial(ctx.instance().materialId()).getDiffuseTexture().bind(0);
                ctx.uniform().uploadInt(0);
            })
            .supplyUniform("gBones", ctx -> ctx.uniform().uploadMat4fs(((AnimatedSolid) ctx.object()).boneTransforms));

    public static Pipeline staticPipeline(Supplier<Matrix4f> projectionMatrix) {
        return new Pipeline.Builder(STATIC)
                .supplyUniform("projectionMatrix", (ctx) -> ctx.uniform().uploadMat4f(projectionMatrix.get()))
                .build();
    }

    public static Pipeline animatedPipeline(Supplier<Matrix4f> projectionMatrix) {
        return new Pipeline.Builder(ANIMATED)
                .supplyUniform("projectionMatrix", (ctx) -> ctx.uniform().uploadMat4f(projectionMatrix.get()))
                .build();
    }

    private static String builtin(String name) {
        try (var is = Pipeline.class.getResourceAsStream("/shaders/" + name)) {
            assert is != null;
            return new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read built in shader", e);
        }
    }
}
