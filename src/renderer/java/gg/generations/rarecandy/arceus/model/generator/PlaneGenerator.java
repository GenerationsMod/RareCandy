package gg.generations.rarecandy.arceus.model.generator;

import gg.generations.pokeutils.reader.TextureReference;
import gg.generations.rarecandy.arceus.model.Model;
import gg.generations.rarecandy.arceus.model.lowlevel.*;
import gg.generations.rarecandy.legacy.model.misc.ITexture;
import gg.generations.rarecandy.legacy.model.misc.Texture;
import gg.generations.rarecandy.legacy.pipeline.ShaderProgram;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Arrays;

import static de.javagl.jgltf.model.GltfConstants.*;
import static org.lwjgl.opengl.GL14.glBlendFuncSeparate;

public class PlaneGenerator {
    public static ShaderProgram simple(Matrix4f projectionMatrix, Matrix4f viewMatrix) throws IOException {
        return new ShaderProgram.Builder()
                .supplyUniform(ShaderProgram.Builder.UniformType.SHARED, "color1", ctx -> ctx.uniform().upload4f(1f, 0f, 0f, 1))
                .supplyUniform(ShaderProgram.Builder.UniformType.SHARED, "color2", ctx -> ctx.uniform().upload4f(1f, 1f, 0f, 1))
                .supplyUniform(ShaderProgram.Builder.UniformType.SHARED, "color3", ctx -> ctx.uniform().upload4f(0f, 0f, 1f, 1))
                .supplyUniform(ShaderProgram.Builder.UniformType.SHARED, "color4", ctx -> ctx.uniform().upload4f(1f, 1f, 1f, 1))
                .supplyUniform(ShaderProgram.Builder.UniformType.SHARED, "viewMatrix", ctx -> ctx.uniform().uploadMat4f(viewMatrix))
                .supplyUniform(ShaderProgram.Builder.UniformType.INSTANCE, "modelMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().getTransform()))
                .supplyUniform(ShaderProgram.Builder.UniformType.SHARED, "projectionMatrix", (ctx) -> ctx.uniform().uploadMat4f(projectionMatrix))
                .supplyUniform(ShaderProgram.Builder.UniformType.INSTANCE, "textureSampler", ctx -> {
                    ctx.bindAndUploadTex(ctx.instance().getMaterial().getDiffuseTexture(), 0);
                })
                .shader(builtin("simple/simple.vs.glsl"), builtin("simple/simple.fs.glsl"))
                            .prePostDraw(() -> {
//                                GL11.glDisable(GL11.GL_CULL_FACE);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
//                glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            }, () -> {
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glDisable(GL11.GL_BLEND);
//                                GL11.glEnable(GL11.GL_CULL_FACE);
            })
                .build();
    }

    public static String builtin(String name) {
        try (var is = ShaderProgram.class.getResourceAsStream("/shaders/" + name)) {
            assert is != null;
            return new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read built in shader", e);
        }
    }

    public static Model generatePlane(Matrix4f projection, Matrix4f view, float width, float length) throws IOException {
        return new Model(PlaneGenerator.generatePlaneRenderData(width, length), simple(projection, view));
    }

    public static RenderData generatePlaneRenderData(float width, float length) {
        // Generate vertex data and indices
        var vertices = generatePlaneVertices(width, length);
        var indices = generatePlaneIndices();

        // Convert float[] to ByteBuffer
        var vertexBuffer = MemoryUtil.memAlloc(vertices.length * 4);
        vertexBuffer.asFloatBuffer().put(vertices).flip();

        // Convert int[] to ByteBuffer
        var indexBuffer = MemoryUtil.memAlloc(indices.length * 2);
        indexBuffer.asShortBuffer().put(indices).flip();

        // Create and return RenderData using the generated data
        return new RenderData(DrawMode.TRIANGLES, createVertexLayout(vertexBuffer), indexBuffer, IndexType.UNSIGNED_SHORT, indices.length);
    }

    private static VertexData createVertexLayout(ByteBuffer vertexBuffer) {
        return new VertexData(vertexBuffer, Arrays.asList(
                Attribute.POSITION,
                Attribute.TEXCOORD
        ));
    }

    private static float[] generatePlaneVertices(float width, float length) {
        return new float[]{
                // Position (x, y, z), Normal (nx, ny, nz), UV (u, v)
                -width / 2, 0.0f, -length / 2, 0.0f, 0.0f, // Bottom left
                width / 2, 0.0f, -length / 2, 1.0f, 0.0f,   // Bottom right
                -width / 2, 0.0f, length / 2, 0.0f, 1.0f,   // Top left
                width / 2, 0.0f, length / 2, 1.0f, 1.0f     // Top right
        };
    }

    private static short[] generatePlaneIndices() {
        return new short[]{
            0, 1, 2, // First triangle
            1, 3, 2  // Second triangle
        };
    }
}