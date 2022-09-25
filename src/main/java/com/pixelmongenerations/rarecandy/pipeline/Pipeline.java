package com.pixelmongenerations.rarecandy.pipeline;

import com.pixelmongenerations.pkl.scene.objects.Mesh;
import com.pixelmongenerations.rarecandy.core.VertexLayout;
import com.pixelmongenerations.rarecandy.rendering.RareCandy;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public final class Pipeline {
    private final Map<String, Consumer<UniformUploadContext>> uniformSuppliers;
    private final Function<Mesh, FloatBuffer> meshBuilder;
    private final Function<Mesh, IntBuffer> indexBuilder;
    private final int program;

    public Pipeline(Map<String, Consumer<UniformUploadContext>> uniformSuppliers, Function<Mesh, FloatBuffer> meshBuilder, Function<Mesh, IntBuffer> indexBuilder, int program) {
        this.uniformSuppliers = uniformSuppliers;
        this.meshBuilder = meshBuilder;
        this.indexBuilder = indexBuilder;
        this.program = program;
    }

    public static class Builder {
        private final Map<String, Consumer<UniformUploadContext>> uniformSuppliers = new HashMap<>();
        private Function<Mesh, FloatBuffer> meshBuilder;
        private Function<Mesh, IntBuffer> indexBuilder;
        private int program;
        public final Map<String, Uniform> uniforms = new HashMap<>();

        public Builder() {
        }

        public Builder(Builder oldBuilder) {
            this.meshBuilder = oldBuilder.meshBuilder;
            this.indexBuilder = oldBuilder.indexBuilder;
            this.program = oldBuilder.program;
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
            var appending = new StringBuilder("#version 450");
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

            return new Pipeline(uniformSuppliers, meshBuilder, indexBuilder, program);
        }
    }
}
