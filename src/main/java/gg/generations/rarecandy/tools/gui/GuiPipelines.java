package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.pokeutils.BlendType;
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader;
import gg.generations.rarecandy.renderer.animation.AnimationController;
import gg.generations.rarecandy.renderer.animation.Transform;
import gg.generations.rarecandy.renderer.model.material.MaterialImages;
import gg.generations.rarecandy.renderer.model.material.MaterialUploader;
import gg.generations.rarecandy.renderer.model.material.MaterialValues;
import gg.generations.rarecandy.renderer.model.material.PipelineRegistry;
import gg.generations.rarecandy.renderer.pipeline.Pipeline;
import gg.generations.rarecandy.renderer.pipeline.UniformUploadContext;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import gg.generations.rarecandy.renderer.storage.AnimatedObjectInstance;
import gg.generations.rarecandy.renderer.storage.InstanceBlockUploader;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static gg.generations.rarecandy.tools.gui.RareCandyCanvas.projectionMatrix;
import static java.lang.Math.floor;

public class GuiPipelines {
//    public static final Vector3f light0 = new Vector3f(0.178f, 0.893f, -0.625f);
//    public static final Vector3f light1 = new Vector3f(-0.178f, 0.893f, 0.625f);

    public static final Vector3f light0 = new Vector3f(0.5f, 0.5f, -0.5f).normalize();
    public static final Vector3f light1 = new Vector3f(-0.3f, 0.4f, 0.7f).normalize();

    public static final Vector4f colorMOdulator = new Vector4f(1f, 1f, 1f, 1f);

    public static final Vector4f fogColor = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);

    private static final Vector3f ONE = new Vector3f(1,1, 1);

    public static final Pipeline ANIMATED = new Pipeline.Builder()
            .supplyUniform("viewMatrix", ctx -> ctx.uniform().uploadMat4f(RareCandyCanvas.viewMatrix))
//            .supplyUniform("modelMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().transformationMatrix()))
            .supplyUniform("projectionMatrix", (ctx) -> ctx.uniform().uploadMat4f(projectionMatrix))
//            .supplyUniform("boneTransforms", ctx -> {
//                var mats = ctx.instance() instanceof AnimatedObjectInstance instance ? instance.getTransforms() != null ? instance.getTransforms() : AnimationController.NO_ANIMATION : AnimationController.NO_ANIMATION;
//                ctx.uniform().uploadMat4fs(mats);
//            })
            .supplyUniform("uvOffset", ctx -> {
                Transform transform = ctx.object().getTransform(ctx.instance().variant());

                if (ctx.instance() instanceof AnimatedObjectInstance instance) {
                    var t = instance.getTransform(ctx.getMaterial().getMaterialName());

                    if (t != null && !t.isUnit()) {
                        transform = t;
                    }
                }

                var offset = transform.offset();

                if(offset == null) offset = Transform.DEFAULT_OFFSET;

                ctx.uniform().uploadVec2f(offset);
            })
            .supplyUniform("uvScale", ctx -> {
                Transform transform = ctx.object().getTransform(ctx.instance().variant());

                if (ctx.instance() instanceof AnimatedObjectInstance instance) {
                    var t = instance.getTransform(ctx.getMaterial().getMaterialName());

                    if (t != null && !t.isUnit()) {
                        transform = t;
                    }
                }

                var scale = transform.scale();

                if(scale == null) scale = Transform.DEFAULT_SCALE;

                ctx.uniform().uploadVec2f(scale);
            })
            .supplyUniform("Light0_Direction", uniformUploadContext -> uniformUploadContext.uniform().uploadVec3f(light0))
            .supplyUniform("Light1_Direction", uniformUploadContext -> uniformUploadContext.uniform().uploadVec3f(light1))
            .supplyUniform("tera", ctx -> ctx.uniform().uploadBoolean(TerasalizationEffect.enabled.get()))
            .supplyUniform("teraTint", ctx -> ctx.uniform().uploadVec3f(TerasalizationEffect.teraColor))
            .supplyUniform("light", ctx -> {
                var light = (int) (RareCandyCanvas.getLightLevel() * 15);

                ctx.uniform().upload2i(0, light);
            })
            .supplyUniform("tint", ctx -> ctx.uniform().uploadVec3f(ONE))
            .supplyUniform("ColorModulator", ctx -> ctx.uniform().uploadVec4f(colorMOdulator))
            .supplyUniform("frame", ctx -> {
                var i = (int) pingpong(RareCandyCanvas.getTime() % 1d);

                ctx.uniform().uploadInt(i);
            })
            .supplySampler("diffuse", 0, MaterialImages::getDiffuse)
            .supplySampler("emission", 2, MaterialImages::getEmission)
            .supplySampler("layer", 3, MaterialImages::getLayer)
            .supplySampler("mask", 4, MaterialImages::getMask)
            .supplySampler("lightmap", 5, "light_map")
            .supplySampler("paradoxMask", 6, "paradox_mask")
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
            .shader(builtin("experimental/animated.vs.glsl"), builtin("experimental/animated.fs.glsl"))
            .build();

    public static final Pipeline PLANE = new Pipeline.Builder()
            .supplyUniform("viewMatrix", ctx -> ctx.uniform().uploadMat4f(RareCandyCanvas.viewMatrix))
            .supplyUniform("projectionMatrix", (ctx) -> ctx.uniform().uploadMat4f(projectionMatrix))
            .supplyUniform("lightLevel", ctx -> ctx.uniform().uploadFloat(RareCandyCanvas.getLightLevel()))
            .supplyUniform("radius", ctx -> ctx.uniform().uploadFloat(RareCandyCanvas.radius))
            .supplyUniform("render", ctx -> ctx.uniform().uploadBoolean(RareCandyCanvas.renderingFrame))
            .prePostDraw(material -> BlendType.Regular.enable(), material -> BlendType.Regular.disable())
            .shader(builtin("original/animated/plane.vs.glsl"), builtin("original/animated/plane.fs.glsl")).build();

    public static final Pipeline SCREEN_QUAD = new Pipeline.Builder()
            .supplyUniform("screenTexture", ctx -> {
                RareCandyCanvas.framebuffer.bind(0);
                ctx.uniform().uploadInt(0);
            })
            .shader(builtin("original/screen/screen_quad.vs.glsl"),
                    builtin("original/screen/screen_quad.fs.glsl")).build();

    public static double pingpong(double time) {
        return (int) (Math.sin(time * Math.PI * 2) * 7 + 7);
    }

    public static void onInitialize() {
//        MaterialUploader.setup(0);
        InstanceBlockUploader.register(ObjectInstance.class, 0, ObjectInstance.MAT4_SIZE);
        InstanceBlockUploader.register(AnimatedObjectInstance.class, 1, ObjectInstance.MAT4_SIZE * 221);
        MaterialUploader.setup();
        PipelineRegistry.setFunction(s -> {
            return switch (s) {
                case "plane" -> GuiPipelines.PLANE;
                case "screen" -> GuiPipelines.SCREEN_QUAD;
                default -> GuiPipelines.ANIMATED;
            };
        });
    }

    private static String builtin(String name) {
        try (var is = Pipeline.class.getResourceAsStream("/shaders/" + name)) {
            assert is != null;
            return new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read built in shader", e);
        }
    }
}