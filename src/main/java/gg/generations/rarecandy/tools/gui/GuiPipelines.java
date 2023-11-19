package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.pokeutils.tranm.Vec3f;
import gg.generations.rarecandy.renderer.animation.AnimationController;
import gg.generations.rarecandy.renderer.animation.GfbAnimation;
import gg.generations.rarecandy.renderer.animation.GfbAnimationInstance;
import gg.generations.rarecandy.renderer.pipeline.Pipeline;
import gg.generations.rarecandy.renderer.pipeline.UniformUploadContext;
import gg.generations.rarecandy.renderer.storage.AnimatedObjectInstance;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.Vector;
import java.util.function.Consumer;

import static gg.generations.rarecandy.tools.gui.RareCandyCanvas.projectionMatrix;

public class GuiPipelines {
    private static final Pipeline.Builder BASE = new Pipeline.Builder()
            .supplyUniform("viewMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().viewMatrix()))
            .supplyUniform("modelMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().transformationMatrix()))
            .supplyUniform("projectionMatrix", (ctx) -> ctx.uniform().uploadMat4f(projectionMatrix))
            .supplyUniform("boneTransforms", ctx -> ctx.uniform().uploadMat4fs(ctx.instance() instanceof AnimatedObjectInstance instance ? instance.getTransforms() != null ? instance.getTransforms() : AnimationController.NO_ANIMATION : AnimationController.NO_ANIMATION))
            .supplyUniform("diffuse", ctx -> {
                ctx.object().getVariant(ctx.instance().variant()).getDiffuseTexture().bind(0);
                ctx.uniform().uploadInt(0);
            })
            .prePostDraw(material -> {
                material.cullType().enable();
                material.blendType().enable();
            }, material -> {
                material.cullType().disable();
                material.blendType().disable();
            });

    public static final Pipeline.Builder LIGHT = new Pipeline.Builder(BASE)
            .supplyUniform("lightLevel", ctx -> ctx.uniform().uploadFloat(RareCandyCanvas.getLightLevel()));

    public static final Pipeline EYE = new Pipeline.Builder(LIGHT)
            .supplyUniform("eyeOffset", ctx -> {
                if (ctx.instance() instanceof AnimatedObjectInstance instance) {
                    if (instance.currentAnimation instanceof GfbAnimationInstance gfbAnimation) {
                        ctx.uniform().uploadVec2f(gfbAnimation.getEyeOffset());
                    }
                    else ctx.uniform().uploadVec2f(AnimationController.NO_OFFSET);
                }
                else ctx.uniform().uploadVec2f(AnimationController.NO_OFFSET);
            })
            .shader(builtin("animated/animated_eye.vs.glsl"), builtin("animated/solid.fs.glsl"))
            .build();

    public static final Pipeline LAYERED = new Pipeline.Builder(LIGHT)
            .shader(builtin("animated/animated.vs.glsl"), builtin("animated/layered.fs.glsl"))
            .supplyUniform("baseColor1", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("baseColor1") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
            .supplyUniform("baseColor2", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("baseColor2") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
            .supplyUniform("baseColor3", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("baseColor3") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
            .supplyUniform("baseColor4", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("baseColor4") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
            .supplyUniform("emiColor1", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("emiColor1") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
            .supplyUniform("emiColor2", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("emiColor2") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
            .supplyUniform("emiColor3", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("emiColor3") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
            .supplyUniform("emiColor4", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("emiColor4") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
            .supplyUniform("emiIntensity1", ctx -> ctx.uniform().uploadFloat(ctx.getValue("emiIntensity1") instanceof Float vec ? vec : 0.0f))
            .supplyUniform("emiIntensity2", ctx -> ctx.uniform().uploadFloat(ctx.getValue("emiIntensity2") instanceof Float vec ? vec : 0.0f))
            .supplyUniform("emiIntensity3", ctx -> ctx.uniform().uploadFloat(ctx.getValue("emiIntensity3") instanceof Float vec ? vec : 0.0f))
            .supplyUniform("emiIntensity4", ctx -> ctx.uniform().uploadFloat(ctx.getValue("emiIntensity4") instanceof Float vec ? vec : 0.0f))
            .supplyUniform("layer", ctx -> {
                ctx.getTexture("layer").bind(1);
                ctx.uniform().uploadInt(1);
            })
            .build();

    private static final Vector3f ONE = new Vector3f(1,1, 1);
    public static final Pipeline SOLID = new Pipeline.Builder(LIGHT)
            .shader(builtin("animated/animated.vs.glsl"), builtin("animated/solid.fs.glsl"))
            .build();

    public static final Pipeline MASKED = new Pipeline.Builder(LIGHT)
            .shader(builtin("animated/animated.vs.glsl"), builtin("animated/masked.fs.glsl"))
            .supplyUniform("mask", ctx -> {
                ctx.object().getVariant(ctx.instance().variant()).getTexture("mask").bind(1);
                ctx.uniform().uploadInt(1);
            })
            .supplyUniform("color", ctx -> {
                var color = (Vector3f) ctx.object().getMaterial(ctx.instance().variant()).getValue("color");
                ctx.uniform().uploadVec3f(color);
            })
            .build();


    public static final Pipeline TRANSPARENT = new Pipeline.Builder(LIGHT)
            .shader(builtin("animated/animated.vs.glsl"), builtin("animated/transparent.fs.glsl"))
            .build();

    public static final Pipeline UNLIT = new Pipeline.Builder(BASE)
            .shader(builtin("animated/animated.vs.glsl"), builtin("animated/unlit.fs.glsl"))
            .build();

    public static void onInitialize() {
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