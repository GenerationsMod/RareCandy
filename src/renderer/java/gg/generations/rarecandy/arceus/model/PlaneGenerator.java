package gg.generations.rarecandy.arceus.model;

import gg.generations.pokeutils.reader.TextureReference;
import gg.generations.rarecandy.arceus.model.lowlevel.*;
import gg.generations.rarecandy.legacy.model.misc.Texture;
import gg.generations.rarecandy.legacy.pipeline.ShaderProgram;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Arrays;

public class PlaneGenerator {
    public static ShaderProgram simple(Matrix4f projectionMatrix, Matrix4f viewMatrix) throws IOException {
        var texture = new Texture(TextureReference.read(Path.of("cursed.png")));

        return new ShaderProgram.Builder()
                .supplyUniform(ShaderProgram.Builder.UniformType.SHARED, "viewMatrix", ctx -> ctx.uniform().uploadMat4f(viewMatrix))
                .supplyUniform(ShaderProgram.Builder.UniformType.INSTANCE, "modelMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().getTransform()))
                .supplyUniform(ShaderProgram.Builder.UniformType.SHARED, "projectionMatrix", (ctx) -> ctx.uniform().uploadMat4f(projectionMatrix))
                .supplyUniform(ShaderProgram.Builder.UniformType.SHARED, "textureSampler", ctx -> ctx.bindAndUploadTex(texture, 0))
                .shader(builtin("simple/simple.vs.glsl"), builtin("simple/simple.fs.glsl"))
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
        float[] vertices = generatePlaneVertices(width, length);
        short[] indices = generatePlaneIndices();

        // Convert float[] to ByteBuffer
        ByteBuffer vertexBuffer = MemoryUtil.memAlloc(vertices.length * 4);
        vertexBuffer.asFloatBuffer().put(vertices).flip();

        // Convert int[] to ByteBuffer
        ByteBuffer indexBuffer = MemoryUtil.memAlloc(indices.length * 2);
        indexBuffer.asShortBuffer().put(indices).flip();

        // Create and return RenderData using the generated data
        return new RenderData(DrawMode.TRIANGLES, createVertexLayout(), vertexBuffer, indexBuffer, IndexType.UNSIGNED_SHORT, indices.length);
    }

    private static VertexLayout createVertexLayout() {
        return new VertexLayout(Arrays.asList(
                Attribute.POSITION,
                Attribute.TEXCOORD
        ));
    }

    private static float[] generatePlaneVertices(float width, float length) {
        float[] vertices = {
            // Position (x, y, z), Normal (nx, ny, nz), UV (u, v)
            -width / 2, 0.0f, -length / 2, 0.0f, 0.0f, // Bottom left
            width / 2, 0.0f, -length / 2, 1.0f, 0.0f,   // Bottom right
            -width / 2, 0.0f, length / 2, 0.0f, 1.0f,   // Top left
            width / 2, 0.0f, length / 2, 1.0f, 1.0f     // Top right
        };
        return vertices;
    }

    private static short[] generatePlaneIndices() {
        short[] indices = {
            0, 1, 2, // First triangle
            1, 3, 2  // Second triangle
        };
        return indices;
    }
}