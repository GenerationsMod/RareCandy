package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.pokeutils.reader.ITextureLoader;
import gg.generations.rarecandy.renderer.pipeline.traditional.TraditionalPipeline;
import gg.generations.rarecandy.renderer.pipeline.util.Scope;
import gg.generations.rarecandy.renderer.textures.FrameBuffer;
import org.lwjgl.opengl.GL11C;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class FullscreenPass {
    public record Input(String uniform, Source source) {}

    private final Supplier<TraditionalPipeline.Builder> base;
    private final List<Input> inputs;
    private final Consumer<TraditionalPipeline.Builder> extraUniforms;
    private final BooleanSupplier enabled;

    // Set by the chain immediately before draw; the sampler suppliers read them.
    private FrameBuffer gbuffer;
    private FrameBuffer previous;

    private TraditionalPipeline pipeline;

    private FullscreenPass(Builder builder) {
        this.base = builder.base;
        this.inputs = List.copyOf(builder.inputs);
        this.extraUniforms = builder.extraUniforms;
        this.enabled = builder.enabled;
        this.pipeline = compile();
    }

    public static Builder of(Supplier<TraditionalPipeline.Builder> base) {
        return new Builder(base);
    }

    public boolean isEnabled() {
        return enabled.getAsBoolean();
    }

    public void reload() {
        pipeline = compile();
    }

    private TraditionalPipeline compile() {
        TraditionalPipeline.Builder builder = base.get();

        // Unit is the declaration index, so no two inputs can collide.
        for (int unit = 0; unit < inputs.size(); unit++) {
            Input input = inputs.get(unit);
            builder.autoSampler2D(Scope.GLOBAL, input.uniform(), unit, ctx -> resolve(input.source()));
        }

        extraUniforms.accept(builder);
        return builder.build();
    }

    private int resolve(Source source) {
        return switch (source) {
            case Source.Attachment a -> gbuffer.getColorAttachment(a.index()).id();
            case Source.Depth d -> gbuffer.getDepthTexture().id();
            case Source.Previous p -> previous.getColorAttachment(0).id();
            case Source.Named n -> ITextureLoader.instance().getTexture(n.key()).id();
        };
    }

    void render(FrameBuffer gbuffer, FrameBuffer previous, FrameBuffer target) {
        this.gbuffer = gbuffer;
        this.previous = previous;

        target.bindAndSetViewport();
        target.setDrawAll();
        draw();
    }

    void renderToScreen(FrameBuffer gbuffer, FrameBuffer previous, int width, int height) {
        this.gbuffer = gbuffer;
        this.previous = previous;

        FrameBuffer.unbindDraw();
        GL11C.glViewport(0, 0, width, height);
        draw();
    }

    private void draw() {
        pipeline.useProgram();
        pipeline.bindGlobal();
        GL11C.glDrawArrays(GL11C.GL_TRIANGLES, 0, 3);
    }

    public static final class Builder {
        private final Supplier<TraditionalPipeline.Builder> base;
        private final List<Input> inputs = new ArrayList<>();
        private Consumer<TraditionalPipeline.Builder> extraUniforms = b -> {};
        private BooleanSupplier enabled = () -> true;

        private Builder(Supplier<TraditionalPipeline.Builder> base) {
            this.base = base;
        }

        public Builder reads(String uniform, Source source) {
            inputs.add(new Input(uniform, source));
            return this;
        }

        public Builder uniforms(Consumer<TraditionalPipeline.Builder> consumer) {
            this.extraUniforms = consumer;
            return this;
        }

        public Builder enabledWhen(BooleanSupplier enabled) {
            this.enabled = enabled;
            return this;
        }

        public FullscreenPass build() {
            return new FullscreenPass(this);
        }
    }
}