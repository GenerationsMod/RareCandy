package com.pixelmongenerations.rarecandy.pipeline;

import com.pixelmongenerations.pkl.scene.objects.Mesh;
import com.pixelmongenerations.rarecandy.components.RenderObject;
import com.pixelmongenerations.rarecandy.rendering.InstanceState;
import com.pixelmongenerations.rarecandy.rendering.RareCandy;
import com.pixelmongenerations.rarecandy.rendering.VertexLayout;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public record Pipeline(Map<String, Consumer<UniformUploadContext>> uniformSuppliers, Map<String, Uniform> uniforms, Function<Mesh, FloatBuffer> vertexBufferBuilder, Function<Mesh, IntBuffer> indexBuilder, int program) {
    private static final Builder FALLBACK = new Builder()
            .shader(
                    builtin("fallback/fallback.vs.glsl"),
                    builtin("fallback/fallback.fs.glsl"),
                    Collections.emptyMap(),
                    new VertexLayout.AttribLayout(3, GL11C.GL_FLOAT, "inPosition")
            )
            .meshBuilder(mesh -> {
                var sizeOfVertex = Float.BYTES * 3;
                var rawMeshData = new float[mesh.getVertices().length * sizeOfVertex];
                var index = 0;

                for (var i = 0; i < mesh.getVertices().length; i++) {
                    var position = mesh.getVertices()[i];
                    rawMeshData[index++] = position.x();
                    rawMeshData[index++] = position.y();
                    rawMeshData[index++] = position.z();
                }

                return createVertexBuffer(rawMeshData);
            })
            .indexBuilder(mesh -> createIndexBuffer(mesh.getIndices()))
            .supplyUniform("viewMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().viewMatrix()))
            .supplyUniform("modelMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().transformationMatrix()));

    public void bind() {
        GL20C.glUseProgram(program);
    }

    public void updateUniforms(InstanceState instance, RenderObject renderObject) {
        for (var name : uniforms.keySet()) {
            var uniform = uniforms.get(name);
            if(!uniformSuppliers.containsKey(name)) RareCandy.fatal("No handler for uniform with name \"" + name + "\"");
            uniformSuppliers.get(name).accept(new UniformUploadContext(renderObject, instance, uniform));
        }
    }

    public static Pipeline fallback(Supplier<Matrix4f> projectionMatrix) {
        return new Pipeline.Builder(FALLBACK)
                .supplyUniform("projectionMatrix", ctx -> ctx.uniform().uploadMat4f(projectionMatrix.get()))
                .build();
    }

    public static String builtin(String name) {
        try (var is = Pipeline.class.getResourceAsStream("/" + name)) {
            assert is != null;
            return new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read built in shader", e);
        }
    }

    public static FloatBuffer createVertexBuffer(float[] rawMeshData) {
        var vertBuffer = BufferUtils.createFloatBuffer(rawMeshData.length);
        for (var v : rawMeshData) vertBuffer.put(v);
        return vertBuffer.flip();
    }

    public static IntBuffer createIndexBuffer(int[] indices) {
        var pIndices = BufferUtils.createIntBuffer(indices.length);
        for (var i : indices) pIndices.put(i);
        return pIndices.flip();
    }

    public static class Builder {
        private Map<String, Consumer<UniformUploadContext>> uniformSuppliers = new HashMap<>();
        private Function<Mesh, FloatBuffer> meshBuilder;
        private Function<Mesh, IntBuffer> indexBuilder;
        private int program;
        public Map<String, Uniform> uniforms = new HashMap<>();

        public Builder() {
        }

        public Builder(Builder oldBuilder) {
            this.uniformSuppliers = oldBuilder.uniformSuppliers;
            this.meshBuilder = oldBuilder.meshBuilder;
            this.indexBuilder = oldBuilder.indexBuilder;
            this.program = oldBuilder.program;
            this.uniforms = oldBuilder.uniforms;
        }

        private void addShader(String text, int type, int programId) {
            var shader = GL20C.glCreateShader(type);
            if (shader == 0) RareCandy.fatal("an error occurred creating the shader object. We don't know what it is.");
            GL20C.glShaderSource(shader, text);
            GL20C.glCompileShader(shader);
            if (GL20C.glGetShaderi(shader, GL20C.GL_COMPILE_STATUS) == 0)
                RareCandy.fatal(GL20C.glGetShaderInfoLog(shader, 1024));
            GL20C.glAttachShader(programId, shader);
        }

        private void compileShader(int programId) {
            GL20C.glLinkProgram(programId);
            if (GL20C.glGetProgrami(programId, GL20C.GL_LINK_STATUS) == 0)
                RareCandy.fatal(GL20C.glGetProgramInfoLog(programId, 1024));
            GL20C.glValidateProgram(programId);
            if (GL20C.glGetProgrami(programId, GL20C.GL_VALIDATE_STATUS) == 0)
                RareCandy.fatal(GL20C.glGetProgramInfoLog(programId, 1024));
        }

        public Builder meshBuilder(Function<Mesh, FloatBuffer> meshBuilder) {
            this.meshBuilder = meshBuilder;
            return this;
        }

        public Builder indexBuilder(Function<Mesh, IntBuffer> indexBuilder) {
            this.indexBuilder = indexBuilder;
            return this;
        }

        public Builder supplyUniform(String name, Consumer<UniformUploadContext> provider) {
            uniformSuppliers.put(name, provider);
            return this;
        }

        public Builder shader(@NotNull String vs, @NotNull String fs, Map<String, String> shaderPath, VertexLayout.AttribLayout... attribs) {
            // Generate in lines for VertexShader
            var appending = new StringBuilder("#version 450\n");
            for (int i = 0; i < attribs.length; i++) {
                var attrib = attribs[i];
                appending.append("layout(location = ").append(i).append(") in ").append(getType(attrib)).append(" ").append(attrib.name()).append(";\n");
            }

            vs = appending + vs;
            program = GL20C.glCreateProgram();
            addShader(vs, GL20C.GL_VERTEX_SHADER, program);
            addShader(fs, GL20C.GL_FRAGMENT_SHADER, program);
            compileShader(program);

            try (var stack = MemoryStack.stackPush()) {
                var pUniformCount = stack.ints(1);
                GL20C.glGetProgramiv(program, GL20C.GL_ACTIVE_UNIFORMS, pUniformCount);
                var uniformCount = pUniformCount.get(0);

                var pMaxNameLength = stack.ints(1);
                GL20C.glGetProgramiv(program, GL20.GL_ACTIVE_UNIFORM_MAX_LENGTH, pMaxNameLength);

                for (int i = 0; i < uniformCount; i++) {
                    var pSize = stack.ints(1);
                    var pType = stack.ints(1);
                    var name = GL20C.glGetActiveUniform(program, i, pMaxNameLength.get(0), pSize, pType);

                    if (name.contains("[")) {
                        name = name.substring(0, name.indexOf('['));
                    }

                    this.uniforms.put(name, new Uniform(program, name, pType.get(0), pSize.get(0)));
                }
            }
            return this;
        }

        private String getType(VertexLayout.AttribLayout attrib) {
            if (attrib.glType() == GL11C.GL_FLOAT) {
                return switch (attrib.size()) {
                    case 1 -> "float";
                    case 2 -> "vec2";
                    case 3 -> "vec3";
                    case 4 -> "vec4";
                    default -> throw new RuntimeException("Cant convert float with size " + attrib.size() + " to type");
                };
            } else {
                throw new RuntimeException("Unknown Type for In Attribute " + attrib);
            }
        }

        public Pipeline build() {
            if (this.meshBuilder == null) throw new RuntimeException("MeshBuilder is null");
            if (this.indexBuilder == null) throw new RuntimeException("IndexBuilder is null");
            if (this.program == 0) throw new RuntimeException("Shader not created");

            return new Pipeline(uniformSuppliers, uniforms, meshBuilder, indexBuilder, program);
        }
    }
}
