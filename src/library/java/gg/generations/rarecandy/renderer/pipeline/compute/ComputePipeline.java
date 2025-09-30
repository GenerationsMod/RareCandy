package gg.generations.rarecandy.renderer.pipeline.compute;

import gg.generations.rarecandy.renderer.pipeline.Pipeline;
import gg.generations.rarecandy.renderer.pipeline.util.*;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL43C;

import java.util.List;
import java.util.Map;

import static gg.generations.rarecandy.renderer.pipeline.Pipeline.Builder.attachShader;
import static gg.generations.rarecandy.renderer.pipeline.Pipeline.Builder.linkProgram;
import static org.lwjgl.opengl.GL42.glMemoryBarrier;

public class ComputePipeline extends Pipeline {
    public ComputePipeline (int program, Map<String, Uniform> uniforms, Map<Scope, List<UBOBinding>> ubos,
                            Map<Scope, List<SSBOBinding>> ssbos, Map<Scope, List<UniformBinding>> uniformBindings) {
        super(program, uniforms, ubos, ssbos, uniformBindings);
    }

    public void dispatch(int flag, int xGroup, int yGroup, int zGroup) {
        GL43.glDispatchCompute(xGroup, yGroup, zGroup);
        glMemoryBarrier(flag);
    }

    public static ComputePipeline.Builder builder(String computeSource) {
        int programId = GL20C.glCreateProgram();
        attachShader(programId, computeSource, GL43C.GL_COMPUTE_SHADER);
        linkProgram(programId);
        return new ComputePipeline.Builder(programId);
    }


    public static class Builder extends Pipeline.Builder<ComputePipeline, ComputePipeline.Builder> {

        protected Builder(int program) {
            super(program);
        }

        @Override
        public ComputePipeline createPipeline() {
            return new ComputePipeline(this.program, uniformMap, ubos, ssbos, uniforms);
        }
    }
}
