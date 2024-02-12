//package gg.generations.rarecandy.tools.gui;
//
//import gg.generations.rarecandy.arceus.model.pk.PkMaterial;
//import gg.generations.rarecandy.legacy.animation.AnimationController;
//import gg.generations.rarecandy.legacy.pipeline.ShaderProgram;
//import org.joml.Vector3f;
//import org.lwjgl.opengl.GL11;
//
//import java.io.IOException;
//
//import static gg.generations.rarecandy.legacy.pipeline.ShaderProgram.Builder.UniformType.*;
//
//public class GuiPipelines {
//    private static final ShaderProgram.Builder ROOT = new ShaderProgram.Builder()
//            .supplyUniform(INSTANCE, "viewMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().viewMatrix()))
//            .supplyUniform(INSTANCE, "modelMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().transformationMatrix()))
//            .supplyUniform(SHARED, "projectionMatrix", (ctx) -> ctx.uniform().uploadMat4f(projectio))
//            .supplyUniform(INSTANCE, "boneTransforms", ctx -> {
//                var mats = ctx.instance() instanceof AnimatedObjectInstance instance ? instance.getTransforms() != null ? instance.getTransforms() : AnimationController.NO_ANIMATION : AnimationController.NO_ANIMATION;
//                ctx.uniform().uploadMat4fs(mats);
//            })
//            .supplyUniform(INSTANCE, "offset", ctx -> {
//                var offsets = ctx.instance() instanceof AnimatedObjectInstance instance ? instance.getOffset(ctx.getMaterial().getMaterialName()) != null ? instance.getOffset(ctx.getMaterial().getMaterialName()) : AnimationController.NO_OFFSET : AnimationController.NO_OFFSET;
//                ctx.uniform().uploadVec2f(offsets.offset());
//            })
//            .supplyUniform(INSTANCE, "scale", ctx -> {
//                var offsets = ctx.instance() instanceof AnimatedObjectInstance instance ? instance.getOffset(ctx.getMaterial().getMaterialName()) != null ? instance.getOffset(ctx.getMaterial().getMaterialName()) : AnimationController.NO_OFFSET : AnimationController.NO_OFFSET;
//                ctx.uniform().uploadVec2f(offsets.scale());
//            })
//            .prePostMaterial(material -> {
//                if(material instanceof PkMaterial pkMaterial) {
//                    if(pkMaterial.getBoolean("disableDepth")) {
//                        GL11.glDisable(GL11.GL_DEPTH_TEST);
//                    }
//
//                    pkMaterial.cullType().enable();
//                    pkMaterial.blendType().enable();
//                }
//            }, material -> {
//                if(material instanceof PkMaterial pkMaterial) {
//
//                    if (pkMaterial.getBoolean("disableDepth")) {
//                        GL11.glEnable(GL11.GL_DEPTH_TEST);
//                    }
//
//                    pkMaterial.cullType().disable();
//                    pkMaterial.blendType().disable();
//                }
//            });
//
//    private static final ShaderProgram.Builder BASE = new ShaderProgram.Builder(ROOT)
//            .configure(GuiPipelines::addDiffuse)
//            .configure(GuiPipelines::addLight);
//
//    private static void addDiffuse(ShaderProgram.Builder builder) {
//        builder.supplyUniform(MATERIAL, "diffuse", ctx -> {
//            var texture = ctx.object().getVariant(ctx.instance().variant()).getDiffuseTexture();
//
//            if(texture == null) {
//                texture = ITextureLoader.instance().getNuetralFallback();
//            }
//
//            texture.bind(0);
//            ctx.uniform().uploadInt(0);
//        });
//    }
//
//    private static void baseColors(ShaderProgram.Builder builder) {
//        builder.supplyUniform("baseColor1", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("baseColor1") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
//                .supplyUniform("baseColor2", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("baseColor2") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
//                .supplyUniform("baseColor3", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("baseColor3") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
//                .supplyUniform("baseColor4", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("baseColor4") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
//                .supplyUniform("baseColor5", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("baseColor5") instanceof Vector3f vec ? vec : GuiPipelines.ONE));
//    }
//
//    private static void addLight(Pipeline.Builder builder) {
//        builder.supplyUniform("lightLevel", ctx -> ctx.uniform().uploadFloat(RareCandyCanvas.getLightLevel()))
//                .supplyUniform("emission", ctx -> {
//                    var texture = ctx.object().getVariant(ctx.instance().variant()).getTexture("emission");
//
//                    if(texture == null) {
//                        texture = ITextureLoader.instance().getDarkFallback();
//                    }
//
//                    texture.bind(1);
//                    ctx.uniform().uploadInt(1);
//                })
//                .supplyUniform("useLight", ctx -> ctx.uniform().uploadBoolean(ctx.getValue("useLight") instanceof Boolean bool ? bool : true));
//    }
//
//    public static final Pipeline.Builder LAYERED_BASE = new Pipeline.Builder(BASE)
//            .shader(builtin("animated/animated.vs.glsl"), builtin("animated/layered.fs.glsl"))
//            .configure(GuiPipelines::baseColors)
//            .configure(GuiPipelines::emissionColors)
//            .supplyUniform("layer", ctx -> {
//                var texture = ctx.getTexture("layer");
//
//                if(texture == null) texture = ITextureLoader.instance().getDarkFallback();
//
//
//                texture.bind(2);
//                ctx.uniform().uploadInt(2);
//            }).supplyUniform("mask", ctx -> {
//                var texture = ctx.getTexture("mask");
//
//                if(texture == null) texture = ITextureLoader.instance().getDarkFallback();
//
//                texture.bind(3);
//                ctx.uniform().uploadInt(3);
//            });
//
//    public static final Pipeline LAYERED = new Pipeline.Builder(LAYERED_BASE)
//            .supplyUniform("frame", ctx -> {
//                ctx.uniform().uploadInt(-1);
//            })
//            .build();
//
//    public static final Pipeline SOLID = new Pipeline.Builder(BASE)
//            .shader(builtin("animated/animated.vs.glsl"), builtin("animated/solid.fs.glsl"))
//            .build();
//    public static final Pipeline MASKED = new Pipeline.Builder(BASE)
//            .shader(builtin("animated/animated.vs.glsl"), builtin("animated/masked.fs.glsl"))
//            .supplyUniform("diffuse", ctx -> {
//                var texture = ctx.object().getVariant(ctx.instance().variant()).getDiffuseTexture();
//
//                if(texture == null) {
//                    texture = ITextureLoader.instance().getBrightFallback();
//                }
//
//                texture.bind(0);
//                ctx.uniform().uploadInt(0);
//            })
//            .supplyUniform("mask", ctx -> {
//
//                var texture = ctx.getTexture("mask");
//
//                if(texture == null) texture = ITextureLoader.instance().getDarkFallback();
//
//                texture.bind(2);
//                ctx.uniform().uploadInt(2);
//            })
//            .supplyUniform("color", ctx -> {
//                var color = (Vector3f) ctx.object().getMaterial(ctx.instance().variant()).getValue("color");
//                ctx.uniform().uploadVec3f(color);
//            })
//            .build();
//    public static final Pipeline PARADOX = new Pipeline.Builder(LAYERED_BASE)
//            .supplyUniform("frame", ctx -> ctx.uniform().uploadInt((int) ((RareCandyCanvas.getTime() * 200) % 16)))
//            .build();
//
//    private static void emissionColors(ShaderProgram.Builder builder) {
//        builder.supplyUniform(MATERIAL, "emiColor1", ctx -> ctx.uniform().uploadVec3f(ctx.material() instanceof PkMaterial pkMaterial ? pkMaterial.getValue("emiColor1") instanceof Vector3f vec ? vec : GuiPipelines.ONE : GuiPipelines.ONE))
//                .supplyUniform(MATERIAL, "emiColor2", ctx -> ctx.uniform().uploadVec3f(ctx.material() instanceof PkMaterial pkMaterial ? pkMaterial.getValue("emiColor2") instanceof Vector3f vec ? vec : GuiPipelines.ONE : GuiPipelines.ONE))
//                .supplyUniform(MATERIAL, "emiColor3", ctx -> ctx.uniform().uploadVec3f(ctx.material() instanceof PkMaterial pkMaterial ? pkMaterial.getValue("emiColor3") instanceof Vector3f vec ? vec : GuiPipelines.ONE : GuiPipelines.ONE))
//                .supplyUniform(MATERIAL, "emiColor4", ctx -> ctx.uniform().uploadVec3f(ctx.material() instanceof PkMaterial pkMaterial ? pkMaterial.getValue("emiColor4") instanceof Vector3f vec ? vec : GuiPipelines.ONE : GuiPipelines.ONE))
//                .supplyUniform(MATERIAL, "emiColor5", ctx -> ctx.uniform().uploadVec3f(ctx.material() instanceof PkMaterial pkMaterial ? pkMaterial.getValue("emiColor5") instanceof Vector3f vec ? vec : GuiPipelines.ONE : GuiPipelines.ONE))
//                .supplyUniform(MATERIAL, "emiIntensity1", ctx -> ctx.uniform().uploadFloat(ctx.material() instanceof PkMaterial pkMaterial ? pkMaterial.getValue("emiIntensity1") instanceof Float vec ? vec : 0.0f : 0.f))
//                .supplyUniform(MATERIAL, "emiIntensity2", ctx -> ctx.uniform().uploadFloat(ctx.material() instanceof PkMaterial pkMaterial ? pkMaterial.getValue("emiIntensity2") instanceof Float vec ? vec : 0.0f : 0.f))
//                .supplyUniform(MATERIAL, "emiIntensity3", ctx -> ctx.uniform().uploadFloat(ctx.material() instanceof PkMaterial pkMaterial ? pkMaterial.getValue("emiIntensity3") instanceof Float vec ? vec : 0.0f : 0.f))
//                .supplyUniform(MATERIAL, "emiIntensity4", ctx -> ctx.uniform().uploadFloat(ctx.material() instanceof PkMaterial pkMaterial ? pkMaterial.getValue("emiIntensity4") instanceof Float vec ? vec : 0.0f : 0.f))
//                .supplyUniform(MATERIAL, "emiIntensity5", ctx -> ctx.uniform().uploadFloat(ctx.material() instanceof PkMaterial pkMaterial ? pkMaterial.getValue("emiIntensity5") instanceof Float vec ? vec : 1.0f : 1.f));
//    }
//    private static final Vector3f ONE = new Vector3f(1,1, 1);
//
//
//
//    public static void onInitialize() {
//    }
//
//    private static String builtin(String name) {
//        try (var is = ShaderProgram.class.getResourceAsStream("/shaders/" + name)) {
//            assert is != null;
//            return new String(is.readAllBytes());
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to read built in shader", e);
//        }
//    }
//}