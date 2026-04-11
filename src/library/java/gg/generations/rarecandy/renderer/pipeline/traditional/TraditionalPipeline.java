package gg.generations.rarecandy.renderer.pipeline.traditional;

import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.pipeline.Pipeline;
import gg.generations.rarecandy.renderer.pipeline.util.*;
import org.joml.*;
import org.lwjgl.opengl.*;

import java.util.*;
import java.util.function.Consumer;

import static gg.generations.rarecandy.renderer.pipeline.Pipeline.Builder.attachShader;
import static gg.generations.rarecandy.renderer.pipeline.Pipeline.Builder.linkProgram;

/**
 * A fully constructed and immutable pipeline instance.
 * Holds shader program, uniform map, and binding declarations.
 * Provides methods to bind resources by scope for rendering.
 */


public final class TraditionalPipeline extends Pipeline {

    private final Consumer<Material> preDraw;
    private final Consumer<Material> postDraw;

    /**
     *
     */
    public TraditionalPipeline(int program, Map<String, Uniform> uniforms, Map<Scope, List<UBOBinding>> ubos,
                               Map<Scope, List<SSBOBinding>> ssbos, Map<Scope, List<UniformBinding>> uniformBindings,
                               Consumer<Material> preDraw, Consumer<Material> postDraw) {
        super(program, uniforms, ubos, ssbos, uniformBindings);
        this.preDraw = preDraw;
        this.postDraw = postDraw;
    }

    public void preDraw(Material material) {
        preDraw.accept(material);
    }

    public void postDraw(Material material) {
        postDraw.accept(material);
    }

    public Consumer<Material> preDraw() {
        return preDraw;
    }

    public Consumer<Material> postDraw() {
        return postDraw;
    }

    public static TraditionalPipeline.Builder builder(String vertexSource, String fragmentSource) {
        int programId = GL20C.glCreateProgram();
        attachShader(programId, vertexSource, GL20C.GL_VERTEX_SHADER);
        attachShader(programId, fragmentSource, GL20C.GL_FRAGMENT_SHADER);
        linkProgram(programId);
        return new Builder(programId);
    }

    public static TraditionalPipeline.Builder builder(String vertexSource, String geometrySource, String fragmentSource) {
        int programId = GL20C.glCreateProgram();
        attachShader(programId, vertexSource, GL20C.GL_VERTEX_SHADER);
        attachShader(programId, geometrySource, GL32C.GL_GEOMETRY_SHADER);
        attachShader(programId, fragmentSource, GL20C.GL_FRAGMENT_SHADER);
        linkProgram(programId);
        return new TraditionalPipeline.Builder(programId);
    }

    /**
     * Builder for creating fully configured Pipeline instances.
     * Handles shader compilation, reflection, uniform population,
     * and binding registration for UBOs, SSBOs, and uniforms.
     */
    public static class Builder extends Pipeline.Builder<TraditionalPipeline, TraditionalPipeline.Builder> {
        private Consumer<Material> preDraw = material -> {
        };
        private Consumer<Material> postDraw = material -> {
        };

        public Builder(int program) {
            super(program);
        }

        @Override
        public TraditionalPipeline createPipeline() {
            return new TraditionalPipeline(program, Map.copyOf(uniformMap), deepCopy(ubos), deepCopy(ssbos), deepCopy(uniforms), preDraw, postDraw);
        }

        public TraditionalPipeline.Builder prePostDraw(Consumer<Material> preDraw, Consumer<Material> postDraw) {
            this.preDraw = preDraw;
            this.postDraw = postDraw;
            return this;
        }
    }
}
