package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.arceus.model.pk.MultiRenderObjectInstance;
import gg.generations.rarecandy.arceus.model.pk.PkMaterial;
import gg.generations.rarecandy.arceus.model.pk.TextureLoader;
import gg.generations.rarecandy.legacy.animation.AnimationController;
import gg.generations.rarecandy.legacy.pipeline.ShaderProgram;
import gg.generationsmod.rarecandy.model.animation.Transform;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.function.Function;
import java.util.function.Supplier;

import static gg.generations.rarecandy.legacy.pipeline.ShaderProgram.Builder.UniformType.*;

public class GuiPipelines {
    public static Function<String, ShaderProgram> of(Supplier<Matrix4f> projectionMatrix, Matrix4f viewMatrix, RareCandyCanvas candyCanvas) {
        var ROOT = new ShaderProgram.Builder()
                .supplyUniform(SHARED, "viewMatrix", ctx -> ctx.uniform().uploadMat4f(viewMatrix))
                .supplyUniform(INSTANCE, "modelMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().getTransform()))
                .supplyUniform(SHARED, "projectionMatrix", (ctx) -> ctx.uniform().uploadMat4f(projectionMatrix.get()))
                .supplyUniform(INSTANCE, "boneTransforms", ctx -> {
                    Matrix4f[] mats = ctx.instance() instanceof MultiRenderObjectInstance instance ? instance.object().getTransforms() : AnimationController.NO_ANIMATION;
                    ctx.uniform().uploadMat4fs(mats);
                })
                .supplyUniform(INSTANCE, "offset", ctx -> {
                    Transform offsets = ctx.instance() instanceof MultiRenderingInstance instance ? instance.object().getOffset(ctx.material().name()) : AnimationController.NO_OFFSET;
                    ctx.uniform().uploadVec2f(offsets.offset());
                })
                .supplyUniform(INSTANCE, "scale", ctx -> {
                    Transform offsets = ctx.instance() instanceof MultiRenderingInstance instance ? instance.object().getOffset(ctx.material().name()) : AnimationController.NO_OFFSET;
                    ctx.uniform().uploadVec2f(offsets.scale());
                })
                .prePostMaterial(material -> {
                    if (material instanceof PkMaterial pkMaterial) {
                        if (pkMaterial.getBoolean("disableDepth")) {
                            GL11.glDisable(GL11.GL_DEPTH_TEST);
                        }

                        pkMaterial.cullType().enable();
                        pkMaterial.blendType().enable();
                    }
                }, material -> {
                    if (material instanceof PkMaterial pkMaterial) {

                        if (pkMaterial.getBoolean("disableDepth")) {
                            GL11.glEnable(GL11.GL_DEPTH_TEST);
                        }

                        pkMaterial.cullType().disable();
                        pkMaterial.blendType().disable();
                    }
                });

        var BASE = new ShaderProgram.Builder(ROOT)
                .configure(GuiPipelines::addDiffuse)
                .configure(GuiPipelines::addLight);

        var LAYERED_BASE = new ShaderProgram.Builder(BASE)
                .shader(builtin("animated/animated.vs.glsl"), builtin("animated/layered.fs.glsl"))
                .configure(GuiPipelines::baseColors)
                .configure(GuiPipelines::emissionColors)
                .supplyUniform(MATERIAL, "layer", ctx -> {
                    var texture = ((PkMaterial) ctx.material()).getTexture("layer");

                    if (texture == null) texture = TextureLoader.instance().getDarkFallback();


                    texture.bind(2);
                    ctx.uniform().uploadInt(2);
                }).supplyUniform(MATERIAL, "mask", ctx -> {
                    var texture = ((PkMaterial) ctx.material()).getTexture("mask");

                    if (texture == null) texture = TextureLoader.instance().getDarkFallback();

                    texture.bind(3);
                    ctx.uniform().uploadInt(3);
                });

        var LAYERED = new ShaderProgram.Builder(LAYERED_BASE)
                .supplyUniform(SHARED, "frame", ctx -> {
                    ctx.uniform().uploadInt(-1);
                })
                .build();

        var SOLID = new ShaderProgram.Builder(BASE)
                .shader(builtin("animated/animated.vs.glsl"), builtin("animated/solid.fs.glsl"))
                .build();
        var MASKED = new ShaderProgram.Builder(BASE)
                .shader(builtin("animated/animated.vs.glsl"), builtin("animated/masked.fs.glsl"))
                .supplyUniform(MATERIAL, "mask", ctx -> {

                    var texture = ((PkMaterial) ctx.material()).getTexture("mask");

                    if (texture == null) texture = TextureLoader.instance().getDarkFallback();

                    ctx.bindAndUploadTex(texture, 2);
                })
                .supplyUniform(MATERIAL, "color", ctx -> {
                    var color = (Vector3f) ((PkMaterial) ctx.material()).getValue("color");
                    ctx.uniform().uploadVec3f(color);
                })
                .build();
        var PARADOX = new ShaderProgram.Builder(LAYERED_BASE)
                .supplyUniform(SHARED, "frame", ctx -> ctx.uniform().uploadInt((int)((candyCanvas.time * 25) % 16)))
                .build();

//        try {
//            var simple = PlaneGenerator.simple(projectionMatrix.get(), viewMatrix);
//            return s -> simple;
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
        return s -> switch (s) {
            case "masked" -> MASKED;
            case "paradox" -> PARADOX;
            case "layered" -> LAYERED;
            default -> SOLID;
        };
    }

    private static void addDiffuse(ShaderProgram.Builder builder) {
        builder.supplyUniform(MATERIAL, "diffuse", ctx -> {
            var texture = ((PkMaterial) ctx.material()).getDiffuseTexture();

            if(texture == null) {
                texture = TextureLoader.instance().getNuetralFallback();
            }

            ctx.bindAndUploadTex(texture, 0);
        });
    }

    private static void baseColors(ShaderProgram.Builder builder) {
        builder.supplyUniform(MATERIAL, "baseColor1", ctx -> ctx.uniform().uploadVec3f(((PkMaterial) ctx.material()).getValue("baseColor1") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
                .supplyUniform(MATERIAL, "baseColor2", ctx -> ctx.uniform().uploadVec3f(((PkMaterial) ctx.material()).getValue("baseColor2") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
                .supplyUniform(MATERIAL, "baseColor3", ctx -> ctx.uniform().uploadVec3f(((PkMaterial) ctx.material()).getValue("baseColor3") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
                .supplyUniform(MATERIAL, "baseColor4", ctx -> ctx.uniform().uploadVec3f(((PkMaterial) ctx.material()).getValue("baseColor4") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
                .supplyUniform(MATERIAL, "baseColor5", ctx -> ctx.uniform().uploadVec3f(((PkMaterial) ctx.material()).getValue("baseColor5") instanceof Vector3f vec ? vec : GuiPipelines.ONE));
    }

    private static void addLight(ShaderProgram.Builder builder) {
        builder.supplyUniform(SHARED, "lightLevel", ctx -> ctx.uniform().uploadFloat(/*RareCandyCanvas.getLightLevel()*/0.5f))
//                .supplyUniform(MATERIAL, "emission", ctx -> {
//                    var texture = ((PkMaterial) ctx.material()).getTexture("emission");
//
//                    if(texture == null) {
//                        texture = TextureLoader.instance().getDarkFallback();
//                    }
//
//                    texture.bind(1);
//                    ctx.uniform().uploadInt(1);
//                })
                .supplyUniform(MATERIAL, "useLight", ctx -> ctx.uniform().uploadBoolean(((PkMaterial) ctx.material()).getValue("useLight") instanceof Boolean bool ? bool : true));
    }

    private static void emissionColors(ShaderProgram.Builder builder) {
        builder.supplyUniform(MATERIAL, "emiColor1", ctx -> ctx.uniform().uploadVec3f(((PkMaterial) ctx.material()).getValue("emiColor1") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
                .supplyUniform(MATERIAL, "emiColor2", ctx -> ctx.uniform().uploadVec3f(((PkMaterial) ctx.material()).getValue("emiColor2") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
                .supplyUniform(MATERIAL, "emiColor3", ctx -> ctx.uniform().uploadVec3f(((PkMaterial) ctx.material()).getValue("emiColor3") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
                .supplyUniform(MATERIAL, "emiColor4", ctx -> ctx.uniform().uploadVec3f(((PkMaterial) ctx.material()).getValue("emiColor4") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
                .supplyUniform(MATERIAL, "emiColor5", ctx -> ctx.uniform().uploadVec3f(((PkMaterial) ctx.material()).getValue("emiColor5") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
                .supplyUniform(MATERIAL, "emiIntensity1", ctx -> ctx.uniform().uploadFloat(((PkMaterial) ctx.material()).getValue("emiIntensity1") instanceof Float vec ? vec : 0.0f))
                .supplyUniform(MATERIAL, "emiIntensity2", ctx -> ctx.uniform().uploadFloat(((PkMaterial) ctx.material()).getValue("emiIntensity2") instanceof Float vec ? vec : 0.0f))
                .supplyUniform(MATERIAL, "emiIntensity3", ctx -> ctx.uniform().uploadFloat(((PkMaterial) ctx.material()).getValue("emiIntensity3") instanceof Float vec ? vec : 0.0f))
                .supplyUniform(MATERIAL, "emiIntensity4", ctx -> ctx.uniform().uploadFloat(((PkMaterial) ctx.material()).getValue("emiIntensity4") instanceof Float vec ? vec : 0.0f))
                .supplyUniform(MATERIAL, "emiIntensity5", ctx -> ctx.uniform().uploadFloat(((PkMaterial) ctx.material()).getValue("emiIntensity5") instanceof Float vec ? vec : 1.0f));
    }
    private static final Vector3f ONE = new Vector3f(1,1, 1);

    private static String builtin(String name) {
        try (var is = ShaderProgram.class.getResourceAsStream("/shaders/" + name)) {
            assert is != null;
            return new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read built in shader", e);
        }
    }
}