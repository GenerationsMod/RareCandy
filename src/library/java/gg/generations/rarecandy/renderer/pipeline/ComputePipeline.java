package gg.generations.rarecandy.renderer.pipeline;

import gg.generations.rarecandy.renderer.loading.ITexture;
import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.rendering.RareCandy;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL43;
import org.lwjgl.system.MemoryStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.lwjgl.opengl.GL42.glMemoryBarrier;

public record ComputePipeline(Map<String, Consumer<Uniform>> uniformSuppliers, Map<String, Uniform> uniforms, int program) {

    public void dispatch(int flag, int xGroup, int yGroup, int zGroup) {
        GL20C.glUseProgram(program);
        updateOtherUniforms();
        updateTexUniforms();

        GL43.glDispatchCompute(xGroup, yGroup, zGroup);
        glMemoryBarrier(flag);
    }

    public void updateOtherUniforms() {
        for (var name : uniforms.keySet()) {
            var uniform = uniforms.get(name);
            if (!uniformSuppliers.containsKey(name))
                RareCandy.fatal("No handler for uniform with name \"" + name + "\"");
            if (uniform.type != GL20C.GL_SAMPLER_2D)
                uniformSuppliers.get(name).accept(uniform);
        }
    }

    public void updateTexUniforms() {
        for (var name : uniforms.keySet()) {
            var uniform = uniforms.get(name);
            if (!uniformSuppliers.containsKey(name))
                RareCandy.fatal("No handler for uniform with name \"" + name + "\"");
            if (uniform.type == GL20C.GL_SAMPLER_2D)
                uniformSuppliers.get(name).accept(uniform);
        }
    }

    public static class Builder {

        public Map<String, Uniform> uniforms = new HashMap<>();
        public Consumer<Material> preDrawBatch = material -> {
        };
        public Consumer<Material> postDrawRunBatch = material -> {
        };
        private Map<String, Consumer<Uniform>> uniformSuppliers = new HashMap<>();
        private int program;

        public Builder() {
        }

        public Builder(Builder base) {
            this.uniformSuppliers = new HashMap<>(base.uniformSuppliers);
            this.program = base.program;
            this.uniforms = new HashMap<>(base.uniforms);
            this.preDrawBatch = base.preDrawBatch;
            this.postDrawRunBatch = base.postDrawRunBatch;
        }

        private void addShader(String text, int programId) {
            var shader = GL20C.glCreateShader(GL43.GL_COMPUTE_SHADER);
            if (shader == 0) RareCandy.fatal("an error occurred creating the shader object. We don't know what it is.");
            GL20C.glShaderSource(shader, text);
            GL20C.glCompileShader(shader);
            if (GL20C.glGetShaderi(shader, GL20C.GL_COMPILE_STATUS) == 0)
                RareCandy.fatal(GL20C.glGetShaderInfoLog(shader, 1024));
            GL20C.glAttachShader(programId, shader);
            GL20C.glLinkProgram(programId);
            if (GL20C.glGetProgrami(programId, GL20C.GL_LINK_STATUS) == 0)
                RareCandy.fatal(GL20C.glGetProgramInfoLog(programId, 1024));
        }

        private void compileShader(int programId) {
            GL20C.glLinkProgram(programId);
            if (GL20C.glGetProgrami(programId, GL20C.GL_LINK_STATUS) == 0)
                RareCandy.fatal(GL20C.glGetProgramInfoLog(programId, 1024));

//I'm suspecting that macs validate differently than windows. So Naiviely disable this check for now. - Waterpicker.
//            GL20C.glValidateProgram(programId);
//            if (GL20C.glGetProgrami(programId, GL20C.GL_VALIDATE_STATUS) == 0)
//                RareCandy.fatal(GL20C.glGetProgramInfoLog(programId, 1024));
        }

        public Builder supplyUniform(String name, Consumer<Uniform> provider) {
            uniformSuppliers.put(name, provider);
            return this;
        }

        public Builder shader(@NotNull String cs) {
            program = GL20C.glCreateProgram();
            addShader(cs, program);

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

        public ComputePipeline build() {
            if (this.program == 0) throw new RuntimeException("Shader not created");

            return new ComputePipeline(uniformSuppliers, uniforms, program);
        }

        public Builder configure(Consumer<Builder> consumer) {
            consumer.accept(this);
            return this;
        }


        public Builder supplyImage2dDirect(String name, int slot, Supplier<ITexture> function) {
            return supplyUniform(name, ctx -> ctx.uploadImage2D(function.get(), slot));
        }

        public Builder supplyImage2d(String name, int slot, Supplier<String> function) {
            return supplyUniform(name, ctx -> {
                ctx.uploadImage2D(function.get(), slot);
            });
        }

        public Builder supplySampler(String name, int slot, Supplier<String> texture) {
            return supplyUniform(name, ctx -> ctx.uploadTexture(texture.get(), slot));
        }

        public Builder supplySamplerDirect(String name, int slot, Supplier<ITexture> texture) {
            return supplyUniform(name, ctx -> ctx.uploadTexture(texture.get(), slot));
        }
    }
}
