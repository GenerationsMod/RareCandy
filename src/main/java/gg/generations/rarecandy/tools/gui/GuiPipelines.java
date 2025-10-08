package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.pokeutils.BlendType;
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader;
import gg.generations.rarecandy.renderer.animation.Transform;
import gg.generations.rarecandy.renderer.model.material.MaterialUploader;
import gg.generations.rarecandy.renderer.model.material.PipelineRegistry;
import gg.generations.rarecandy.renderer.pipeline.traditional.TraditionalPipeline;
import gg.generations.rarecandy.renderer.pipeline.util.BufferSupplier;
import gg.generations.rarecandy.renderer.pipeline.util.Scope;
import gg.generations.rarecandy.renderer.pipeline.util.TextureIdSupplier;
import gg.generations.rarecandy.renderer.pipeline.util.UniformUploadContext;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import gg.generations.rarecandy.renderer.storage.AnimatedObjectInstance;
import gg.generations.rarecandy.renderer.storage.InstanceBlockUploader;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

import static gg.generations.rarecandy.renderer.pipeline.Pipelines.builtin;
import static gg.generations.rarecandy.tools.gui.RareCandyCanvas.projectionMatrix;

public class GuiPipelines {
//    public static final Vector3f light0 = new Vector3f(0.178f, 0.893f, -0.625f);
//    public static final Vector3f light1 = new Vector3f(-0.178f, 0.893f, 0.625f);

    public static final Vector3f light0 = new Vector3f(0.5f, 0.5f, -0.5f).normalize();
    public static final Vector3f light1 = new Vector3f(-0.3f, 0.4f, 0.7f).normalize();

    public static final Vector4f colorMOdulator = new Vector4f(1f, 1f, 1f, 1f);

    public static final Vector4f fogColor = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);

    private static final Vector3f ONE = new Vector3f(1,1, 1);

    public static double pingpong(double time) {
        return (int) (Math.sin(time * Math.PI * 2) * 7 + 7);
    }

    public static void onInitialize(RareCandyCanvas canvas, PokeUtilsGui.Settings settings) {
        InstanceBlockUploader.register(ObjectInstance.class, ObjectInstance.MAT4_SIZE);
        InstanceBlockUploader.register(AnimatedObjectInstance.class, ObjectInstance.MAT4_SIZE * 221);
        MaterialUploader.setup();

        TraditionalPipeline ANIMATED = TraditionalPipeline.builder(builtin("experimental/animated.vs.glsl"), builtin("experimental/animated.fs.glsl1"))
                .autoMat4(Scope.GLOBAL, "viewMatrix", (ctx) -> RareCandyCanvas.viewMatrix)
                .autoMat4(Scope.GLOBAL, "projectionMatrix", (ctx) -> projectionMatrix)
                .autoVec2(Scope.DRAW, "uvOffset", (ctx) -> {
                    var variant = ctx.object().getVariant(ctx.mesh(), ctx.instance().variant());

                    Transform transform = variant.offset();

                    if (ctx.instance() instanceof AnimatedObjectInstance animatedInstance) {

                        var material = variant.material();

                        var t = animatedInstance.getTransform(material);

                        if (t != null && !t.isUnit()) {
                            transform = t;
                        }
                    }

                    if(transform == null) {
                        transform = Transform.DEFAULT;
                    }

                    return transform.offset();
                })
                .autoVec2(Scope.DRAW, "uvScale", (ctx) -> {
                    var variant = ctx.object().getVariant(ctx.mesh(), ctx.instance().variant());

                    Transform transform = variant.offset();

                    if (ctx.instance() instanceof AnimatedObjectInstance animatedInstance) {

                        var material = variant.material();

                        var t = animatedInstance.getTransform(material);

                        if (t != null && !t.isUnit()) {
                            transform = t;
                        }
                    }

                    if(transform == null) {
                        transform = Transform.DEFAULT;
                    }

                    return transform.scale();
                })
                .autoVec3(Scope.GLOBAL, "Light0_Direction", (ctx) -> light0)
                .autoVec3(Scope.GLOBAL, "Light1_Direction", (ctx) -> light1)
                .autoBool(Scope.GLOBAL, "tera", (ctx) -> settings.terastalization.enabled.getValue())
                .autoVec3(Scope.GLOBAL, "teraTint", (ctx) -> settings.terastalization.tint.getValue())
                .addUniform(Scope.GLOBAL, "light", (uniform, ctx) -> {
                    var light = (int) (RareCandyCanvas.getLightLevel() * 15);

                    uniform.upload2i(0, light);
                })
                .autoVec3(Scope.GLOBAL, "tint", (ctx) -> ONE)
                .autoVec4(Scope.GLOBAL, "ColorModulator", (ctx) -> colorMOdulator)
                .autoInt(Scope.GLOBAL, "frame", (ctx) -> (int) pingpong(RareCandyCanvas.getTime() % 1d))
                .autoSampler2DArray(Scope.INSTANCE, "images", 0, ctx -> ctx.object().images)
                .autoSampler2D(Scope.GLOBAL, "lightmap", 1, (ctx) -> ITextureLoader.instance().getTexture("light_map").getId())
                .autoSampler2D(Scope.GLOBAL, "paradoxMask", 2, (ctx) -> ITextureLoader.instance().getTexture("paradox_mask").getId())
                .prePostDraw(material -> {

                    if(material.disableDepth()) {
                        GL11.glDisable(GL11.GL_DEPTH_TEST);
                    }

                    material.cullType().enable();
                    material.blendType().enable();
                }, material -> {
                    if(material.disableDepth()) {
                        GL11.glEnable(GL11.GL_DEPTH_TEST);
                    }

                    material.cullType().disable();
                    material.blendType().disable();
                })
                .addUBO(Scope.GLOBAL, "Fog", 0, (ctx) -> canvas.getFogUploader().id)
                .addUBO(Scope.INSTANCE, "Instance", 1, (ctx) -> InstanceBlockUploader.bind(ctx.instance()))
                .addUBO(Scope.DRAW, "Material", 2, (ctx) -> ctx.object().getMaterial(ctx.mesh(), ctx.instance().variant()).bindMaterial())
                .addSSBORange(Scope.MODEL, "VertexBuffer", 0, ctx -> ctx.object().buffer, ctx -> ctx.object().vertex)
//                .addSSBORange(Scope.MODEL, "PositionBuffer", 0, ctx -> ctx.object().buffer, ctx -> ctx.object().position)
//                .addSSBORange(Scope.MODEL, "UVBuffer", 1, ctx -> ctx.object().buffer, ctx -> ctx.object().uv)
//                .addSSBORange(Scope.MODEL, "NormalBuffer", 2, ctx -> ctx.object().buffer, ctx -> ctx.object().normal)
//                .addSSBORange(Scope.MODEL, "JointBuffer", 3, ctx -> ctx.object().buffer, ctx -> ctx.object().joint)
//                .addSSBORange(Scope.MODEL, "WeightBuffer", 4, ctx -> ctx.object().buffer, ctx -> ctx.object().weight)
                .addSSBORange(Scope.MODEL, "IndexBuffer", 1, ctx -> ctx.object().buffer, ctx -> ctx.object().index)
                .build();

        TraditionalPipeline PLANE = TraditionalPipeline.builder(builtin("original/animated/plane.vs.glsl"), builtin("original/animated/plane.fs.glsl"))
                .autoMat4(Scope.GLOBAL, "viewMatrix", (ctx) -> RareCandyCanvas.viewMatrix)
                .autoMat4(Scope.GLOBAL, "projectionMatrix", (ctx) -> projectionMatrix)
                .autoFloat(Scope.GLOBAL, "lightLevel", (ctx) -> RareCandyCanvas.getLightLevel())
                .autoFloat(Scope.GLOBAL, "radius", (ctx) -> RareCandyCanvas.radius)
                .autoBool(Scope.GLOBAL, "render", (ctx) -> RareCandyCanvas.renderingFrame)
                .prePostDraw(material -> BlendType.Regular.enable(), material -> BlendType.Regular.disable())
                .build();

        TraditionalPipeline SCREEN_QUAD = TraditionalPipeline.builder(builtin("original/screen/screen_quad.vs.glsl"), builtin("original/screen/screen_quad.fs.glsl"))
                .autoSampler2D(Scope.GLOBAL, "screenTexture", 0, (ctx) -> RareCandyCanvas.framebuffer.getId())
                .build();

        PipelineRegistry.setFunction(s -> {
            return switch (s) {
                case "plane" -> PLANE;
                case "screen" -> SCREEN_QUAD;
                default -> ANIMATED;
            };
        });
    }
}