package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.pokeutils.reader.ITextureLoader;
import gg.generations.rarecandy.renderer.animation.AnimationController;
import gg.generations.rarecandy.renderer.animation.Transform;
import gg.generations.rarecandy.renderer.pipeline.Pipeline;
import gg.generations.rarecandy.renderer.storage.AnimatedObjectInstance;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.List;

import static gg.generations.rarecandy.tools.gui.RareCandyCanvas.projectionMatrix;
import static java.lang.Math.floor;

public class GuiPipelines {
    private static final Pipeline.Builder ROOT = new Pipeline.Builder()
            .supplyUniform("viewMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().viewMatrix()))
            .supplyUniform("modelMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().transformationMatrix()))
            .supplyUniform("projectionMatrix", (ctx) -> ctx.uniform().uploadMat4f(projectionMatrix))
            .supplyUniform("boneTransforms", ctx -> {
                var mats = ctx.instance() instanceof AnimatedObjectInstance instance ? instance.getTransforms() != null ? instance.getTransforms() : AnimationController.NO_ANIMATION : AnimationController.NO_ANIMATION;
                ctx.uniform().uploadMat4fs(mats);
            })
            .supplyUniform("offset", ctx -> {

                Transform offsets = null;
                if (ctx.instance() instanceof AnimatedObjectInstance instance) {
                    if (instance.getOffset(ctx.getMaterial().getMaterialName()) != null)
                        offsets = instance.getOffset(ctx.getMaterial().getMaterialName());
                }

                Vector2f translate = offsets != null ? offsets.offset() : ctx.object().getOffsets(ctx.instance().variant());


                ctx.uniform().uploadVec2f(translate);
            })
            .supplyUniform("scale", ctx -> {
                var offsets = ctx.instance() instanceof AnimatedObjectInstance instance ? instance.getOffset(ctx.getMaterial().getMaterialName()) != null ? instance.getOffset(ctx.getMaterial().getMaterialName()) : AnimationController.NO_OFFSET : AnimationController.NO_OFFSET;
                ctx.uniform().uploadVec2f(offsets.scale());
            })
            .prePostDraw(material -> {
                if(material.getBoolean("disableDepth")) {
                    GL11.glDisable(GL11.GL_DEPTH_TEST);
                }

                material.cullType().enable();
                material.blendType().enable();
            }, material -> {
                if(material.getBoolean("disableDepth")) {
                    GL11.glEnable(GL11.GL_DEPTH_TEST);
                }

                material.cullType().disable();
                material.blendType().disable();
            });

    public static final Pipeline BONE = new Pipeline.Builder(ROOT)
            .shader(builtin("animated/bone.vs.glsl"), builtin("animated/bone.fs.glsl"))
            .build();

    private static final Pipeline.Builder BASE = new Pipeline.Builder(ROOT)
            .configure(GuiPipelines::addDiffuse)
            .configure(GuiPipelines::addLight);

    private static void addDiffuse(Pipeline.Builder builder) {
        builder.supplyUniform("diffuse", ctx -> {
            var texture = ctx.object().getVariant(ctx.instance().variant()).getDiffuseTexture();

            if(texture == null) {
                texture = ITextureLoader.instance().getNuetralFallback();
            }

            texture.bind(0);
            ctx.uniform().uploadInt(0);
        });
    }

    private static void baseColors(Pipeline.Builder builder) {
        builder.supplyUniform("baseColor1", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("baseColor1") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
                .supplyUniform("baseColor2", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("baseColor2") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
                .supplyUniform("baseColor3", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("baseColor3") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
                .supplyUniform("baseColor4", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("baseColor4") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
                .supplyUniform("baseColor5", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("baseColor5") instanceof Vector3f vec ? vec : GuiPipelines.ONE));
    }

    private static void addLight(Pipeline.Builder builder) {
        builder.supplyUniform("lightLevel", ctx -> ctx.uniform().uploadFloat(RareCandyCanvas.getLightLevel()))
                .supplyUniform("emission", ctx -> {
                    var texture = ctx.object().getVariant(ctx.instance().variant()).getTexture("emission");

                    if(texture == null) {
                        texture = ITextureLoader.instance().getDarkFallback();
                    }

                    texture.bind(1);
                    ctx.uniform().uploadInt(1);
                })
                .supplyUniform("useLight", ctx -> ctx.uniform().uploadBoolean(ctx.getValue("useLight") instanceof Boolean bool ? bool : true));
    }

    public static final Pipeline.Builder LAYERED_BASE = new Pipeline.Builder(BASE)
            .shader(builtin("animated/animated.vs.glsl"), builtin("animated/layered.fs.glsl"))
            .configure(GuiPipelines::baseColors)
            .configure(GuiPipelines::emissionColors)
            .supplyUniform("layer", ctx -> {
                var texture = ctx.getTexture("layer");

                if(texture == null) texture = ITextureLoader.instance().getDarkFallback();


                texture.bind(2);
                ctx.uniform().uploadInt(2);
            }).supplyUniform("mask", ctx -> {
                var texture = ctx.getTexture("mask");

                if(texture == null) texture = ITextureLoader.instance().getDarkFallback();

                texture.bind(3);
                ctx.uniform().uploadInt(3);
            });

    public static final Pipeline LAYERED = new Pipeline.Builder(LAYERED_BASE)
            .supplyUniform("frame", ctx -> {
                ctx.uniform().uploadInt(-1);
            })
            .build();

    public static final Pipeline SOLID = new Pipeline.Builder(BASE)
            .shader(builtin("animated/animated.vs.glsl"), builtin("animated/solid.fs.glsl"))
            .build();
    public static final Pipeline MASKED = new Pipeline.Builder(BASE)
            .shader(builtin("animated/animated.vs.glsl"), builtin("animated/masked.fs.glsl"))
            .supplyUniform("diffuse", ctx -> {
                var texture = ctx.object().getVariant(ctx.instance().variant()).getDiffuseTexture();

                if(texture == null) {
                    texture = ITextureLoader.instance().getBrightFallback();
                }

                texture.bind(0);
                ctx.uniform().uploadInt(0);
            })
            .supplyUniform("mask", ctx -> {

                var texture = ctx.getTexture("mask");

                if(texture == null) texture = ITextureLoader.instance().getDarkFallback();

                texture.bind(2);
                ctx.uniform().uploadInt(2);
            })
            .supplyUniform("color", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("color") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
            .build();
    public static final Pipeline PARADOX = new Pipeline.Builder(LAYERED_BASE)
            .supplyUniform("frame", ctx -> {
                var i = (int) pingpong(RareCandyCanvas.getTime() % 1d);

                ctx.uniform().uploadInt(i);
            }).build();

    public static double fract(double a) {
        return a - floor(a);
    }

    public static double pingpong(double time) {
        return (int) (Math.sin(time * Math.PI * 2) * 7 + 7);
    }

    private static void emissionColors(Pipeline.Builder builder) {
        builder.supplyUniform("emiColor1", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("emiColor1") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
                .supplyUniform("emiColor2", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("emiColor2") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
                .supplyUniform("emiColor3", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("emiColor3") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
                .supplyUniform("emiColor4", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("emiColor4") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
                .supplyUniform("emiColor5", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("emiColor5") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
                .supplyUniform("emiIntensity1", ctx -> ctx.uniform().uploadFloat(ctx.getValue("emiIntensity1") instanceof Float vec ? vec : 0.0f))
                .supplyUniform("emiIntensity2", ctx -> ctx.uniform().uploadFloat(ctx.getValue("emiIntensity2") instanceof Float vec ? vec : 0.0f))
                .supplyUniform("emiIntensity3", ctx -> ctx.uniform().uploadFloat(ctx.getValue("emiIntensity3") instanceof Float vec ? vec : 0.0f))
                .supplyUniform("emiIntensity4", ctx -> ctx.uniform().uploadFloat(ctx.getValue("emiIntensity4") instanceof Float vec ? vec : 0.0f))
                .supplyUniform("emiIntensity5", ctx -> ctx.uniform().uploadFloat(ctx.getValue("emiIntensity5") instanceof Float vec ? vec : 1.0f));
    }
    private static final Vector3f ONE = new Vector3f(1,1, 1);



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