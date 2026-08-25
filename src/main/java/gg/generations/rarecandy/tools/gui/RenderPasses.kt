package gg.generations.rarecandy.tools.gui

import gg.generations.rarecandy.pokeutils.resource.ResourceReader
import gg.generations.rarecandy.renderer.pipeline.ShaderSource
import gg.generations.rarecandy.renderer.pipeline.traditional.TraditionalPipeline
import gg.generations.rarecandy.renderer.pipeline.util.Scope
import gg.generations.rarecandy.renderer.pipeline.util.Uniform
import gg.generations.rarecandy.renderer.pipeline.util.UniformCallback
import gg.generations.rarecandy.renderer.pipeline.util.UniformUploadContext
import gg.generations.rarecandy.tools.gui.Source.Companion.attachment
import gg.generations.rarecandy.tools.gui.Source.Companion.depth
import gg.generations.rarecandy.tools.gui.Source.Companion.named
import org.joml.Vector3f
import java.util.function.Function

object RenderPasses {
    var chain: PassChain? = null

    @JvmStatic
    fun init(
        reader: ResourceReader, source: ShaderSource, canvas: RareCandyCanvas,
        settings: PokeUtilsGui.Settings, light0: Vector3f, light1: Vector3f
    ) {
        chain = PassChain(canvas.width, canvas.height)

        val generate: Function<String?, FullscreenPass.Builder?> = Function { name: String? ->
            FullscreenPass.of({
                TraditionalPipeline.builder(
                    source.compileSet(
                        reader,
                        "fullscreen",
                        name
                    )
                )
            }).writesTo(name!!)
        }

        val light = Vector3f(0f, 1.5f, 2f)

        chain!!.add(
            generate.apply("light/diffuse")!!
                .reads("inAlbedo", attachment(0))
                .reads("inNormal", attachment(1))
                .reads("inDepth", depth())
                .uniforms { ctx: TraditionalPipeline.Builder? ->
                    ctx!!.autoVec3(
                        Scope.GLOBAL,
                        "lightPos",
                        Function { uniformUploadContext: UniformUploadContext? -> light })
                    ctx.autoVec3(
                        Scope.GLOBAL,
                        "lightColor",
                        Function { uniformUploadContext: UniformUploadContext? -> settings.light.standard.lightColor })
                    ctx.autoFloat(
                        Scope.GLOBAL,
                        "lightRange",
                        Function { uniformUploadContext: UniformUploadContext? -> settings.light.standard.lightRange.get() })
                    ctx.autoVec3(
                        Scope.GLOBAL,
                        "ambientColor",
                        Function { uniformUploadContext: UniformUploadContext? -> settings.light.standard.ambientColor })
                    ctx.autoFloat(
                        Scope.GLOBAL,
                        "shininess",
                        Function { uniformUploadContext: UniformUploadContext? -> settings.light.standard.shininess.get() })
                    ctx.autoMat4(
                        Scope.GLOBAL,
                        "inverseProjectionMatrix",
                        Function { uniformUploadContext: UniformUploadContext? -> canvas.camera.getInverseProjectionMatrix() })
                    ctx.autoMat4(
                        Scope.GLOBAL,
                        "inverseViewMatrix",
                        Function { uniformUploadContext: UniformUploadContext? -> canvas.camera.getInverseViewMatrix() })
                }
                .enabledWhen { settings.light.selected.get() == 3 }
                .build()
        )

        chain!!.add(
            generate.apply("light/minecraft")!!
                .reads("inAlbedo", attachment(0))
                .reads("inNormal", attachment(1))
                .reads("inEmissive", attachment(2))
                .reads("lightmap", named("light_map"))
                .uniforms { builder: TraditionalPipeline.Builder? ->
                    builder!!
                        .addUniform(
                            Scope.GLOBAL,
                            "light",
                            UniformCallback { uniform: Uniform?, ctx: UniformUploadContext? ->
                                uniform!!.upload2i(
                                    settings.light.minecraft.block.getValue(),
                                    settings.light.minecraft.sky.getValue()
                                )
                            })
                        .autoVec3(Scope.GLOBAL, "Light0_Direction", Function { ctx: UniformUploadContext? -> light0 })
                        .autoVec3(Scope.GLOBAL, "Light1_Direction", Function { ctx: UniformUploadContext? -> light1 })
                }
                .enabledWhen { settings.light.selected.get() == 2 }
                .build())

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