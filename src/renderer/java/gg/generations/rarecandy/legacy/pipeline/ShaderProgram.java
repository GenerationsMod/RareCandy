package gg.generations.rarecandy.legacy.pipeline;

import gg.generations.rarecandy.arceus.model.Model;
import gg.generations.rarecandy.arceus.model.RenderingInstance;
import gg.generations.rarecandy.arceus.util.RareCandyException;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.system.MemoryStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public record ShaderProgram(
        Map<String, Consumer<UniformUploadContext>> sharedUniformSuppliers,
        Map<String, Consumer<UniformUploadContext>> instanceUniformSuppliers,
        Map<String, Consumer<UniformUploadContext>> modelUniformSuppliers,
        Map<String, Uniform> uniforms,
        Runnable preDrawBatch,
        Runnable postDrawBatch,
        int program
) {
    public void bind() {
        GL20C.glUseProgram(program);
        preDrawBatch.run();
    }

    public void unbind() {
        postDrawBatch.run();
    }

    public void updateSharedUniforms() {
        for (var name : uniforms.keySet()) {
            if (sharedUniformSuppliers.containsKey(name)) {
                var uniform = uniforms.get(name);
                sharedUniformSuppliers.get(name).accept(new UniformUploadContext(null, null, uniform));
            }
        }
    }

    public void updateInstanceUniforms(RenderingInstance instance, Model renderObject) {
        for (var name : uniforms.keySet()) {
            if (instanceUniformSuppliers.containsKey(name)) {
                var uniform = uniforms.get(name);
                instanceUniformSuppliers.get(name).accept(new UniformUploadContext(renderObject, instance, uniform));
            }
        }
    }

    public void updateModelUniforms(Model renderObject) {
        for (var name : uniforms.keySet()) {
            if (instanceUniformSuppliers.containsKey(name)) {
                var uniform = uniforms.get(name);
                instanceUniformSuppliers.get(name).accept(new UniformUploadContext(renderObject, null, uniform));
            }
        }
    }

    public static class Builder {
        public enum UniformType {
            SHARED, INSTANCE, MODEL
        }

        public Map<String, Uniform> uniforms = new HashMap<>();
        public Runnable preDrawBatch = () -> {};
        public Runnable postDrawRunBatch = () -> {};
        private Map<String, Consumer<UniformUploadContext>> sharedUniformSuppliers = new HashMap<>();
        private Map<String, Consumer<UniformUploadContext>> instanceUniformSuppliers = new HashMap<>();
        private Map<String, Consumer<UniformUploadContext>> modelUniformSuppliers = new HashMap<>();
        private int program;

        public Builder() {
        }

        public Builder(Builder base) {
            this.sharedUniformSuppliers = new HashMap<>(base.sharedUniformSuppliers);
            this.instanceUniformSuppliers = new HashMap<>(base.instanceUniformSuppliers);
            this.modelUniformSuppliers = new HashMap<>(base.modelUniformSuppliers);
            this.program = base.program;
            this.uniforms = new HashMap<>(base.uniforms);
            this.preDrawBatch = base.preDrawBatch;
            this.postDrawRunBatch = base.postDrawRunBatch;
        }

        private void addShader(String text, int type, int programId) {
            var shader = GL20C.glCreateShader(type);
            if (shader == 0) throw new RareCandyException("an error occurred creating the shader object. We don't know what it is.");
            GL20C.glShaderSource(shader, text);
            GL20C.glCompileShader(shader);
            if (GL20C.glGetShaderi(shader, GL20C.GL_COMPILE_STATUS) == 0)
                throw new RareCandyException(GL20C.glGetShaderInfoLog(shader, 1024));
            GL20C.glAttachShader(programId, shader);
        }

        private void compileShader(int programId) {
            GL20C.glLinkProgram(programId);
            if (GL20C.glGetProgrami(programId, GL20C.GL_LINK_STATUS) == 0)
                throw new RareCandyException(GL20C.glGetProgramInfoLog(programId, 1024));
            GL20C.glValidateProgram(programId);
            if (GL20C.glGetProgrami(programId, GL20C.GL_VALIDATE_STATUS) == 0)
                throw new RareCandyException(GL20C.glGetProgramInfoLog(programId, 1024));
        }

        public Builder prePostDraw(Runnable preDrawBatch, Runnable postDrawRunBatch) {
            this.preDrawBatch = preDrawBatch;
            this.postDrawRunBatch = postDrawRunBatch;
            return this;
        }

        public Builder supplyUniform(UniformType type, String name, Consumer<UniformUploadContext> provider) {
            switch (type) {
                case SHARED -> sharedUniformSuppliers.put(name, provider);
                case INSTANCE -> instanceUniformSuppliers.put(name, provider);
                case MODEL -> modelUniformSuppliers.put(name, provider);
            }
            return this;
        }

        public Builder shader(@NotNull String vs, @NotNull String fs) {
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

        public ShaderProgram build() {
            if (this.program == 0) throw new RuntimeException("Shader not created");
            return new ShaderProgram(sharedUniformSuppliers, instanceUniformSuppliers, modelUniformSuppliers, uniforms, preDrawBatch, postDrawRunBatch, program);
        }
    }
}
