package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.pokeutils.resource.ResourceReader;
import gg.generations.rarecandy.renderer.pipeline.ShaderSource;
import gg.generations.rarecandy.renderer.pipeline.traditional.TraditionalPipeline;
import gg.generations.rarecandy.renderer.pipeline.util.Scope;
import gg.generations.rarecandy.renderer.pipeline.util.Uniform;
import gg.generations.rarecandy.renderer.pipeline.util.UniformCallback;
import gg.generations.rarecandy.renderer.pipeline.util.UniformUploadContext;
import org.joml.Vector3f;

public class RenderPasses {
    public static PassChain chain;

    public static void init(ResourceReader reader, ShaderSource source, RareCandyCanvas canvas,
                            PokeUtilsGui.Settings settings, Vector3f light0, Vector3f light1) {

        chain = new PassChain(canvas.getWidth(), canvas.getHeight());

        chain.add(FullscreenPass.of(() -> TraditionalPipeline.builder(source.compileSet(reader, "light")))
                .reads("inAlbedo", Source.attachment(0))
                .reads("inNormal", Source.attachment(1))
                .reads("inEmissive", Source.attachment(2))
                .reads("lightmap", Source.named("light_map"))
                .uniforms(builder -> builder
                        .addUniform(Scope.GLOBAL, "light", (uniform, ctx) -> uniform.upload2i(
                                settings.values.blockLight.getValue(),
                                settings.values.skyLight.getValue()))
                        .autoVec3(Scope.GLOBAL, "Light0_Direction", ctx -> light0)
                        .autoVec3(Scope.GLOBAL, "Light1_Direction", ctx -> light1))
                .build());

        chain.add(FullscreenPass.of(() -> TraditionalPipeline.builder(source.compileSet(reader, "fog")))
                .reads("inAlbedo", Source.previous())
                .reads("inDepth", Source.depth())
                .uniforms(builder -> builder
                        .autoMat4(Scope.GLOBAL, "inverseProjectionMatrix",
                                ctx -> canvas.camera.getInverseProjectionMatrix())
                        .addUBO(Scope.GLOBAL, "Fog", 0, ctx -> canvas.getFogUploader().id))
                .enabledWhen(() -> settings.features.fog.getValue())
                .build());
        chain.add(FullscreenPass.of(() -> TraditionalPipeline.builder(source.compileSet(reader, "outline")))
                .reads("inAlbedo", Source.previous())
                .reads("inObject", Source.attachment(3))
                .uniforms(ctx -> {
                    ctx.addUniform(Scope.GLOBAL, "size", (uniform, ctx1) -> uniform.upload2f(canvas.getWidth(), canvas.getHeight()));
                    ctx.autoFloat(Scope.GLOBAL, "lineWidth", contex -> 3.0f);
                }).enabledWhen(() -> true)
                .build());
    }
}