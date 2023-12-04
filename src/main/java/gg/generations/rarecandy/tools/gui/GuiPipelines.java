package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.pokeutils.reader.TextureLoader;
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
    private static final Pipeline.Builder ROOT = new Pipeline.Builder()
            .supplyUniform("viewMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().viewMatrix()))
            .supplyUniform("modelMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().transformationMatrix()))
            .supplyUniform("projectionMatrix", (ctx) -> ctx.uniform().uploadMat4f(projectionMatrix))
            .supplyUniform("boneTransforms", ctx -> {
                var mats = ctx.instance() instanceof AnimatedObjectInstance instance ? instance.getTransforms() != null ? instance.getTransforms() : AnimationController.NO_ANIMATION : AnimationController.NO_ANIMATION;
                ctx.uniform().uploadMat4fs(mats);
            });

    public static final Pipeline BONE = new Pipeline.Builder(ROOT)
            .shader(builtin("animated/bone.vs.glsl"), builtin("animated/bone.fs.glsl"))
            .build();

    private static final Pipeline.Builder BASE = new Pipeline.Builder(ROOT)
            .supplyUniform("diffuse", ctx -> {
                var texture = ctx.object().getVariant(ctx.instance().variant()).getDiffuseTexture();

                if(texture == null) {
                    System.out.printf("Error! Can't find %s!%n", "diffuse");
                    texture = TextureLoader.instance().getNuetralFallback();
                }

                texture.bind(0);
                ctx.uniform().uploadInt(0);
            })
            .supplyUniform("lightLevel", ctx -> ctx.uniform().uploadFloat(0.1f))//RareCandyCanvas.getLightLevel()))
            .supplyUniform("emission", ctx -> {
                var texture = ctx.object().getVariant(ctx.instance().variant()).getTexture("emission");

                if(texture == null) {
                    texture = TextureLoader.instance().getBrightFallback();
                }

                texture.bind(1);
                ctx.uniform().uploadInt(1);
            })
            .supplyUniform("useLight", ctx -> ctx.uniform().uploadBoolean(ctx.getValue("useLight") instanceof Boolean bool ? bool : true))
            .prePostDraw(material -> {
                material.cullType().enable();
                material.blendType().enable();
            }, material -> {
                material.cullType().disable();
                material.blendType().disable();
            });

    public static final Pipeline EYE = new Pipeline.Builder(BASE)
            .supplyUniform("eyeOffset", ctx -> {
                if (ctx.instance() instanceof AnimatedObjectInstance instance) {
                    if (instance.currentAnimation instanceof GfbAnimationInstance gfbAnimation) {
                        ctx.uniform().uploadVec2f(gfbAnimation.getEyeOffset(ctx.getMaterial().getMaterialName()));
                    }
                    else ctx.uniform().uploadVec2f(AnimationController.NO_OFFSET);
                }
                else ctx.uniform().uploadVec2f(AnimationController.NO_OFFSET);
            })
            .shader(builtin("animated/animated_eye.vs.glsl"), builtin("animated/solid.fs.glsl"))
            .build();

    public static final Pipeline LAYERED = new Pipeline.Builder(BASE)
            .shader(builtin("animated/animated.vs.glsl"), builtin("animated/layered.fs.glsl"))
            .supplyUniform("baseColor1", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("baseColor1") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
            .supplyUniform("baseColor2", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("baseColor2") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
            .supplyUniform("baseColor3", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("baseColor3") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
            .supplyUniform("baseColor4", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("baseColor4") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
            .supplyUniform("baseColor5", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("baseColor5") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
            .supplyUniform("emiColor1", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("emiColor1") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
            .supplyUniform("emiColor2", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("emiColor2") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
            .supplyUniform("emiColor3", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("emiColor3") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
            .supplyUniform("emiColor4", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("emiColor4") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
            .supplyUniform("emiColor5", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("emiColor5") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
            .supplyUniform("emiIntensity1", ctx -> ctx.uniform().uploadFloat(ctx.getValue("emiIntensity1") instanceof Float vec ? vec : 0.0f))
            .supplyUniform("emiIntensity2", ctx -> ctx.uniform().uploadFloat(ctx.getValue("emiIntensity2") instanceof Float vec ? vec : 0.0f))
            .supplyUniform("emiIntensity3", ctx -> ctx.uniform().uploadFloat(ctx.getValue("emiIntensity3") instanceof Float vec ? vec : 0.0f))
            .supplyUniform("emiIntensity4", ctx -> ctx.uniform().uploadFloat(ctx.getValue("emiIntensity4") instanceof Float vec ? vec : 0.0f))
            .supplyUniform("emiIntensity5", ctx -> ctx.uniform().uploadFloat(ctx.getValue("emiIntensity5") instanceof Float vec ? vec : 0.0f))
            .supplyUniform("layer", ctx -> {
                var texture = ctx.getTexture("layer");

                if(texture == null) texture = TextureLoader.instance().getDarkFallback();


                texture.bind(2);
                ctx.uniform().uploadInt(2);
            }).supplyUniform("mask", ctx -> {
                var texture = ctx.getTexture("mask");

                if(texture == null) texture = TextureLoader.instance().getDarkFallback();

                texture.bind(3);
                ctx.uniform().uploadInt(3);
            })
            .build();

    private static final Vector3f ONE = new Vector3f(1,1, 1);
    public static final Pipeline SOLID = new Pipeline.Builder(BASE)
            .shader(builtin("animated/animated.vs.glsl"), builtin("animated/solid.fs.glsl"))
            .build();

    public static final Pipeline MASKED = new Pipeline.Builder(BASE)
            .shader(builtin("animated/animated.vs.glsl"), builtin("animated/masked.fs.glsl"))
            .supplyUniform("mask", ctx -> {

                var texture = ctx.getTexture("mask");

                if(texture == null) texture = TextureLoader.instance().getBrightFallback();

                texture.bind(2);
                ctx.uniform().uploadInt(2);
            })
            .supplyUniform("color", ctx -> {
                var color = (Vector3f) ctx.object().getMaterial(ctx.instance().variant()).getValue("color");
                ctx.uniform().uploadVec3f(color);
            })
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