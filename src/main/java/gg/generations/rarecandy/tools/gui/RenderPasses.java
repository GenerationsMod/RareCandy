package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.pokeutils.resource.ResourceReader;
import gg.generations.rarecandy.renderer.pipeline.ShaderSource;
import gg.generations.rarecandy.renderer.pipeline.traditional.TraditionalPipeline;
import gg.generations.rarecandy.renderer.pipeline.util.Scope;
import org.joml.Vector3f;

import java.util.function.Function;

public class RenderPasses {
    public static PassChain chain;

    public static void init(ResourceReader reader, ShaderSource source, RareCandyCanvas canvas,
                            PokeUtilsGui.Settings settings, Vector3f light0, Vector3f light1) {

        chain = new PassChain(canvas.getWidth(), canvas.getHeight());

        Function<String, FullscreenPass.Builder> generate = name -> FullscreenPass.of(() -> TraditionalPipeline.builder(source.compileSet(reader, "fullscreen", name))).writesTo(name);

        var light = new Vector3f(0, 1.5f, 2);

        chain.add(generate.apply("light/diffuse")
                .reads("inAlbedo", Source.attachment(0))
                .reads("inNormal", Source.attachment(1))
                .reads("inDepth", Source.depth())
                .uniforms(ctx -> {
                    ctx.autoVec3(Scope.GLOBAL, "lightPos", uniformUploadContext -> light);
                    ctx.autoVec3(Scope.GLOBAL, "lightColor", uniformUploadContext -> settings.light.standard.lightColor.getValue());
                    ctx.autoFloat(Scope.GLOBAL, "lightRange", uniformUploadContext -> settings.light.standard.lightRange.get());
                    ctx.autoVec3(Scope.GLOBAL, "ambientColor", uniformUploadContext -> settings.light.standard.ambientColor.getValue());
                    ctx.autoFloat(Scope.GLOBAL, "shininess", uniformUploadContext -> settings.light.standard.shininess.get());
                    ctx.autoMat4(Scope.GLOBAL, "inverseProjectionMatrix", uniformUploadContext -> canvas.camera.getInverseProjectionMatrix());
                    ctx.autoMat4(Scope.GLOBAL, "inverseViewMatrix", uniformUploadContext -> canvas.camera.getInverseViewMatrix());
                })
                .enabledWhen(() -> settings.light.selected.get() == 3)
                .build()
        );

        chain.add(generate.apply("light/minecraft")
                .reads("inAlbedo", Source.attachment(0))
                .reads("inNormal", Source.attachment(1))
                .reads("inEmissive", Source.attachment(2))
                .reads("lightmap", Source.named("light_map"))
                .uniforms(builder -> builder
                        .addUniform(Scope.GLOBAL, "light", (uniform, ctx) -> uniform.upload2i(
                                settings.light.minecraft.block.getValue(),
                                settings.light.minecraft.sky.getValue()))
                        .autoVec3(Scope.GLOBAL, "Light0_Direction", ctx -> light0)
                        .autoVec3(Scope.GLOBAL, "Light1_Direction", ctx -> light1)
                )
                .enabledWhen(() -> settings.light.selected.get() == 2)
                .build());

//        chain.add(generate.apply("mask_albedo")
//                .reads("inAlbedo", Source.previous())
//                .reads("inObject", Source.attachment(3))
//                .build());
//
//        chain.add(generate.apply("h_guassian").reads("inAlbedo", Source.previous()).build());
//        chain.add(generate.apply("v_guassian").reads("inAlbedo", Source.previous()).build());
//        chain.add(generate.apply("merge")
//                .reads("blurred", Source.previous())
//                .reads("inAlbedo", Source.pass("light"))
//                .build());

//        chain.add(generate.apply("fog")
//                .reads("inAlbedo", Source.previous())
//                .reads("inDepth", Source.depth())
//                .uniforms(builder -> builder
//                        .autoMat4(Scope.GLOBAL, "inverseProjectionMatrix",
//                                ctx -> canvas.camera.getInverseProjectionMatrix())
//                        .addUBO(Scope.GLOBAL, "Fog", 0, ctx -> canvas.getFogUploader().id))
//                .enabledWhen(() -> settings.features.fog.getValue())
//                .build());
//        chain.add(generate.apply("outline")
//                .reads("inAlbedo", Source.previous())
//                .reads("inObject", Source.attachment(3))
//                .uniforms(ctx -> {
//                    ctx.addUniform(Scope.GLOBAL, "size", (uniform, ctx1) -> uniform.upload2f(canvas.getWidth(), canvas.getHeight()));
//                    ctx.autoFloat(Scope.GLOBAL, "lineWidth", contex -> 3.0f);
//                }).enabledWhen(() -> true)
//                .build());
    }
}