package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.pokeutils.BlendType;
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader;
import gg.generations.rarecandy.renderer.animation.AnimationController;
import gg.generations.rarecandy.renderer.animation.Transform;
import gg.generations.rarecandy.renderer.model.material.MaterialImages;
import gg.generations.rarecandy.renderer.model.material.MaterialValues;
import gg.generations.rarecandy.renderer.model.material.PipelineRegistry;
import gg.generations.rarecandy.renderer.pipeline.Pipeline;
import gg.generations.rarecandy.renderer.pipeline.UniformUploadContext;
import gg.generations.rarecandy.renderer.storage.AnimatedObjectInstance;
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
import static gg.generations.rarecandy.tools.gui.RareCandyCanvas.viewMatrix;
import static java.lang.Math.floor;

public class GuiPipelines {
    public static final Vector3f light0 = new Vector3f(0.178f, 0.893f, -0.625f);
    public static final Vector3f light1 = new Vector3f(-0.178f, 0.893f, 0.625f);

    public static final Vector4f colorMOdulator = new Vector4f(1f, 1f, 1f, 1f);

    public static final Vector4f fogColor = new Vector4f(0.5f, 0.5f, 0.5f, 1.0f);

    private static final Vector3f ONE = new Vector3f(1,1, 1);

    public static final Pipeline ANIMATED = new Pipeline.Builder()
            .shader(builtin("experimental/animated.vs.glsl"), builtin("experimental/animated.fs.glsl"))
            .supplySampler("diffuse", 0, MaterialImages::getDiffuse)
            .supplySampler("emission", 2, MaterialImages::getEmission)
            .supplySampler("layer", 3, MaterialImages::getLayer)
            .supplySampler("mask", 4, MaterialImages::getMask)
            .supplySampler("lightmap", 5, "light_map")
            .supplySampler("paradoxMask", 6, "paradox_mask")
            .bindUniformBlock("SharedInfo", 0)
            .bindUniformBlock("InstanceInfo", 1)
            .bindUniformBlock("LightingVertex", 2)
            .bindUniformBlock("FogParams", 3)
            .bindUniformBlock("MaterialBlock", 4)
            .bindUniformBlock("RenderOptions", 5)
            .prePostDraw(material -> {
                material.bind();

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
            .build();

//    public static final Pipeline ANIMATED = new Pipeline.Builder()
//            .supplyUniform("viewMatrix", ctx -> ctx.uniform().uploadMat4f(viewMatrix))
//            .supplyUniform("modelMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().transformationMatrix()))
//            .supplyUniform("projectionMatrix", (ctx) -> ctx.uniform().uploadMat4f(projectionMatrix))
//            .supplyUniform("boneTransforms", ctx -> {
//                var mats = ctx.instance() instanceof AnimatedObjectInstance instance ? instance.getTransforms() != null ? instance.getTransforms() : AnimationController.NO_ANIMATION : AnimationController.NO_ANIMATION;
//                ctx.uniform().uploadMat4fs(mats);
//            })
//            .supplyUniform("uvOffset", ctx -> {
//                Transform transform = ctx.object().getTransform(ctx.instance().variant());
//
//                if (ctx.instance() instanceof AnimatedObjectInstance instance) {
//                    var t = instance.getTransform(ctx.getMaterial().getMaterialName());
//
//                    if (t != null && !t.isUnit()) {
//                        transform = t;
//                    }
//                }
//
//                var offset = transform.offset();
//
//                if(offset == null) offset = Transform.DEFAULT_OFFSET;
//
//                ctx.uniform().uploadVec2f(offset);
//            })
//            .supplyUniform("uvScale", ctx -> {
//                Transform transform = ctx.object().getTransform(ctx.instance().variant());
//
//                if (ctx.instance() instanceof AnimatedObjectInstance instance) {
//                    var t = instance.getTransform(ctx.getMaterial().getMaterialName());
//
//                    if (t != null && !t.isUnit()) {
//                        transform = t;
//                    }
//                }
//
//                var scale = transform.scale();
//
//                if(scale == null) scale = Transform.DEFAULT_SCALE;
//
//                ctx.uniform().uploadVec2f(scale);
//            })
//            .supplyUniform("Light0_Direction", uniformUploadContext -> uniformUploadContext.uniform().uploadVec3f(light0))
//            .supplyUniform("Light1_Direction", uniformUploadContext -> uniformUploadContext.uniform().uploadVec3f(light1))
//            .supplyUniform("dynamicVertexColor", ctx -> ctx.uniform().uploadBoolean(true))
//            .supplyUniform("FogShape", ctx -> ctx.uniform().uploadInt(0)) //TODO: Make enum for fog shape. I think 0 is spherical and 1 cylinderical
//            .supplyUniform("FogColor", ctx -> ctx.uniform().uploadVec4f(fogColor))
//            .supplyUniform("FogStart", ctx -> ctx.uniform().uploadFloat(1.0f))
//            .supplyUniform("FogEnd", ctx -> ctx.uniform().uploadFloat(5.0f))
//            .supplyUniform("colorMethod", ctx -> ctx.uniform().uploadInt(ctx.getMaterial().getColorMethod()))
//            .supplyUniform("effect", ctx -> ctx.uniform().uploadInt(ctx.getMaterial().getEffect()))
//            .supplyUniform("light", ctx -> {
//                var light = (int) (RareCandyCanvas.getLightLevel() * 15);
////
//                System.out.println(RareCandyCanvas.getLightLevel() + " " + light);
//
//                ctx.uniform().upload2i(0, light);
//            })
//            .supplyUniform("tint", ctx -> ctx.uniform().uploadVec3f(ONE))
//            .supplyUniform("ColorModulator", ctx -> ctx.uniform().uploadVec4f(colorMOdulator))
//            .supplyUniform("frame", ctx -> {
//                var i = (int) pingpong(RareCandyCanvas.getTime() % 1d);
//
//                ctx.uniform().uploadInt(i);
//            })
//            .supplyUniform("baseColor1", ctx -> ctx.uniform().uploadVec3f(ctx.getMaterial().values().getBaseColor1()))
//            .supplyUniform("baseColor2", ctx -> ctx.uniform().uploadVec3f(ctx.getMaterial().values().getBaseColor2()))
//            .supplyUniform("baseColor3", ctx -> ctx.uniform().uploadVec3f(ctx.getMaterial().values().getBaseColor3()))
//            .supplyUniform("baseColor4", ctx -> ctx.uniform().uploadVec3f(ctx.getMaterial().values().getBaseColor4()))
//            .supplyUniform("baseColor5", ctx -> ctx.uniform().uploadVec3f(ctx.getMaterial().values().getBaseColor5()))
//            .supplyUniform("emiColor1", ctx -> ctx.uniform().uploadVec3f(ctx.getMaterial().values().getEmiColor1()))
//            .supplyUniform("emiColor2", ctx -> ctx.uniform().uploadVec3f(ctx.getMaterial().values().getEmiColor2()))
//            .supplyUniform("emiColor3", ctx -> ctx.uniform().uploadVec3f(ctx.getMaterial().values().getEmiColor3()))
//            .supplyUniform("emiColor4", ctx -> ctx.uniform().uploadVec3f(ctx.getMaterial().values().getEmiColor4()))
//            .supplyUniform("emiColor5", ctx -> ctx.uniform().uploadVec3f(ctx.getMaterial().values().getEmiColor5()))
//            .supplyUniform("emiIntensity1", ctx -> ctx.uniform().uploadFloat(ctx.getMaterial().values().getEmiIntensity1()))
//            .supplyUniform("emiIntensity2", ctx -> ctx.uniform().uploadFloat(ctx.getMaterial().values().getEmiIntensity2()))
//            .supplyUniform("emiIntensity3", ctx -> ctx.uniform().uploadFloat(ctx.getMaterial().values().getEmiIntensity3()))
//            .supplyUniform("emiIntensity4", ctx -> ctx.uniform().uploadFloat(ctx.getMaterial().values().getEmiIntensity4()))
//            .supplyUniform("emiIntensity5", ctx -> ctx.uniform().uploadFloat(ctx.getMaterial().values().getEmiIntensity5()))
//            .supplyUniform("useLight", ctx -> ctx.uniform().uploadBoolean(ctx.getMaterial().values().getUseLight()))
//            .supplySampler("diffuse", 0, MaterialImages::getDiffuse)
//            .supplySampler("emission", 2, MaterialImages::getEmission)
//            .supplySampler("layer", 3, MaterialImages::getLayer)
//            .supplySampler("mask", 4, MaterialImages::getMask)
//            .supplySampler("lightmap", 5, "light_map")
//            .supplySampler("paradoxMask", 6, "paradox_mask")
//            .prePostDraw(material -> {
//                material.bind();
//
//                if(material.disableDepth()) {
//                    GL11.glDisable(GL11.GL_DEPTH_TEST);
//                }
//
//                material.cullType().enable();
//                material.blendType().enable();
//            }, material -> {
//                if(material.disableDepth()) {
//                    GL11.glEnable(GL11.GL_DEPTH_TEST);
//                }
//
//                material.cullType().disable();
//                material.blendType().disable();
//            })
//            .shader(builtin("experimental/animated.vs.glsl"), builtin("experimental/animated.fs.glsl"))
//            .build();
//
//
//    private static final Pipeline.Builder ROOT = new Pipeline.Builder()
//            .supplyUniform("viewMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().viewMatrix()))
//            .supplyUniform("modelMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().transformationMatrix()))
//            .supplyUniform("projectionMatrix", (ctx) -> ctx.uniform().uploadMat4f(projectionMatrix))
//            .supplyUniform("boneTransforms", ctx -> {
//                var mats = ctx.instance() instanceof AnimatedObjectInstance instance ? instance.getTransforms() != null ? instance.getTransforms() : AnimationController.NO_ANIMATION : AnimationController.NO_ANIMATION;
//                ctx.uniform().uploadMat4fs(mats);
//            })
//            .supplyUniform("offset", ctx -> {
//                Transform transform = ctx.object().getTransform(ctx.instance().variant());
//
//                if (ctx.instance() instanceof AnimatedObjectInstance instance) {
//                    var t = instance.getTransform(ctx.getMaterial().getMaterialName());
//
//                    if (t != null && !t.isUnit()) {
//                        transform = t;
//                    }
//                }
//
//                var offset = transform.offset();
//
//                if(offset == null) offset = Transform.DEFAULT_OFFSET;
//
//                ctx.uniform().uploadVec2f(offset);
//            })
//            .supplyUniform("scale", ctx -> {
//                Transform transform = ctx.object().getTransform(ctx.instance().variant());
//
//                if (ctx.instance() instanceof AnimatedObjectInstance instance) {
//                    var t = instance.getTransform(ctx.getMaterial().getMaterialName());
//
//                    if (t != null && !t.isUnit()) {
//                        transform = t;
//                    }
//                }
//
//                var scale = transform.scale();
//
//                if(scale == null) scale = Transform.DEFAULT_SCALE;
//
//                ctx.uniform().uploadVec2f(scale);
//            })
//            .prePostDraw(material -> {
//                if(material.disableDepth()) {
//                    GL11.glDisable(GL11.GL_DEPTH_TEST);
//                }
//
//                material.cullType().enable();
//                material.blendType().enable();
//            }, material -> {
//                if(material.disableDepth()) {
//                    GL11.glEnable(GL11.GL_DEPTH_TEST);
//                }
//
//                material.cullType().disable();
//                material.blendType().disable();
//            });

    public static final Pipeline PLANE = new Pipeline.Builder()
            .supplyUniform("viewMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().viewMatrix()))
            .supplyUniform("modelMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().transformationMatrix()))
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
//
//    private static final Pipeline.Builder BASE = new Pipeline.Builder(ROOT)
//            .configure(GuiPipelines::addDiffuse)
//            .configure(GuiPipelines::addLight);
//
//    private static Map<String, Pipeline> shaderMap;
//
//    private static void addDiffuse(Pipeline.Builder builder) {
//        builder.supplySampler("diffuse", 0, MaterialImages::getDiffuse);
//    }
//
//    private static void baseColors(Pipeline.Builder builder) {
//        builder.supplyUniform("baseColor1", ctx -> ctx.uniform().uploadVec3f(ctx.getMaterial().values().getBaseColor1()))
//                .supplyUniform("baseColor2", ctx -> ctx.uniform().uploadVec3f(ctx.getMaterial().values().getBaseColor2()))
//                .supplyUniform("baseColor3", ctx -> ctx.uniform().uploadVec3f(ctx.getMaterial().values().getBaseColor3()))
//                .supplyUniform("baseColor4", ctx -> ctx.uniform().uploadVec3f(ctx.getMaterial().values().getBaseColor4()))
//                .supplyUniform("baseColor5", ctx -> ctx.uniform().uploadVec3f(ctx.getMaterial().values().getBaseColor5()));
//    }
//
//    private static void addLight(Pipeline.Builder builder) {
//        builder.supplyUniform("lightLevel", ctx -> ctx.uniform().uploadFloat(RareCandyCanvas.getLightLevel()))
//                .supplySampler("emission", 1, MaterialImages::getEmission)
//                .supplyUniform("useLight", ctx -> ctx.uniform().uploadBoolean(ctx.getMaterial().values().getUseLight()));
//    }
//
//    public static Pipeline.Builder createLayered(String effect) {
//        return new Pipeline.Builder(BASE)
//                .shader(builtin("original/animated/animated.vs.glsl"), builtin("original/animated/layered.fs.glsl", "original/process/%s.lib.glsl".formatted(effect)))
//                .configure(GuiPipelines::baseColors)
//                .configure(GuiPipelines::emissionColors)
//                .supplySampler("layer", 2, MaterialImages::getLayer)
//                .supplySampler("mask", 3, MaterialImages::getMask);
//    }
//
//    public static Pipeline.Builder createSolid(String effect) {
//        return new Pipeline.Builder(BASE).shader(builtin("original/animated/animated.vs.glsl"), builtin("original/animated/solid.fs.glsl", "original/process/%s.lib.glsl".formatted(effect)));
//    }
//
//    public static Pipeline.Builder createMasked(String effect) {
//        return new Pipeline.Builder(BASE)
//                .shader(builtin("original/animated/animated.vs.glsl"), builtin("original/animated/masked.fs.glsl", "original/process/%s.lib.glsl".formatted(effect)))
//                .supplySampler("diffuse", 0, MaterialImages::getDiffuse)
//                .supplySampler("mask", 2, MaterialImages::getMask)
//                .supplyUniform("color", ctx -> ctx.uniform().uploadVec3f(ctx.getMaterial().values().getBaseColor1()));
//    }
//
//    public static void addParadox(Pipeline.Builder builder, String shader) {
//        var slot = switch (shader) {
//            case "masked" -> 3;
//            case "layered" -> 4;
//            default -> 2;
//        };
//
//        builder.supplyUniform("frame", ctx -> {
//            var i = (int) pingpong(RareCandyCanvas.getTime() % 1d);
//
//            ctx.uniform().uploadInt(i);
//        }).supplyUniform("paradoxMask", ctx -> {
//            var texture =  ITextureLoader.instance().getTexture("paradox_mask");
//            texture.bind(slot);
//            ctx.uniform().uploadInt(slot);
//        });
//    }
//
////    public static final Pipeline.Builder GALAXY_BASE = new Pipeline.Builder(BASE)
////            .shader(builtin("animated/animated.vs.glsl"), builtin("animated/galaxy.fs.glsl"))
////            .configure(GuiPipelines::baseColors)
////            .configure(GuiPipelines::emissionColors)
////            .supplyUniform("frame", ctx -> {
////                var i = (int) pingpong((RareCandyCanvas.getTime()) % 1d); // Scale time
////                ctx.uniform().uploadInt(i);
////
////            }).supplyUniform("layer", ctx -> {
////                var texture = ctx.getTexture("layer");
////
////                if(texture == null) texture = ITextureLoader.instance().getDarkFallback();
////
////
////                texture.bind(2);
////                ctx.uniform().uploadInt(2);
////            }).supplyUniform("mask", ctx -> {
////                var texture = ctx.getTexture("mask");
////
////                if(texture == null) texture = ITextureLoader.instance().getDarkFallback();
////
////                texture.bind(3);
////                ctx.uniform().uploadInt(3);
////            });
////    public static final Pipeline GALAXY = new Pipeline.Builder(GALAXY_BASE)
////            .supplyUniform("frame", ctx -> {
////                double slowdownFactor = 2;
////                var i = (int) pingpong((RareCandyCanvas.getTime() / slowdownFactor) % 1d); // Scale time
////                ctx.uniform().uploadInt(i);
////
////            }).build();
//
//
//    public static double fract(double a) {
//        return a - floor(a);
//    }
//
    public static double pingpong(double time) {
        return (int) (Math.sin(time * Math.PI * 2) * 7 + 7);
    }
//    private static void emissionColors(Pipeline.Builder builder) {
//        builder.supplyUniform("emiColor1", ctx -> ctx.uniform().uploadVec3f(ctx.getMaterial().values().getEmiColor1()))
//                .supplyUniform("emiColor2", ctx -> ctx.uniform().uploadVec3f(ctx.getMaterial().values().getEmiColor2()))
//                .supplyUniform("emiColor3", ctx -> ctx.uniform().uploadVec3f(ctx.getMaterial().values().getEmiColor3()))
//                .supplyUniform("emiColor4", ctx -> ctx.uniform().uploadVec3f(ctx.getMaterial().values().getEmiColor4()))
//                .supplyUniform("emiColor5", ctx -> ctx.uniform().uploadVec3f(ctx.getMaterial().values().getEmiColor5()))
//                .supplyUniform("emiIntensity1", ctx -> ctx.uniform().uploadFloat(ctx.getMaterial().values().getEmiIntensity1()))
//                .supplyUniform("emiIntensity2", ctx -> ctx.uniform().uploadFloat(ctx.getMaterial().values().getEmiIntensity2()))
//                .supplyUniform("emiIntensity3", ctx -> ctx.uniform().uploadFloat(ctx.getMaterial().values().getEmiIntensity3()))
//                .supplyUniform("emiIntensity4", ctx -> ctx.uniform().uploadFloat(ctx.getMaterial().values().getEmiIntensity4()))
//                .supplyUniform("emiIntensity5", ctx -> ctx.uniform().uploadFloat(ctx.getMaterial().values().getEmiIntensity5()));
//    }
//
//
//
    public static void onInitialize() {
//        shaderMap = new HashMap<>();
//
//        var effects = List.of("pastel", "galaxy", "paradox", "shadow", "sketch", "vintage", "passthrough", "cartoon");
//
//        for (var effect : effects) {
//
//            var solid = createSolid(effect);
//            var masked = createMasked(effect);
//            var layered = createLayered(effect);
//
//            if(effect.equals("paradox") || effect.equals("galaxy")) {
//                addParadox(solid, "solid");
//                addParadox(masked, "masked");
//                addParadox(layered, "layered");
//            }
//
//            var suffix = !effect.equals("passthrough") ? "_" + effect : "";
//
//            shaderMap.put("solid" + suffix, solid.build());
//            shaderMap.put("masked" + suffix, masked.build());
//            shaderMap.put("layered" + suffix, layered.build());
//        }
//
//        shaderMap.put("plane", GuiPipelines.PLANE);
//        shaderMap.put("screen", GuiPipelines.SCREEN_QUAD);

        PipelineRegistry.setFunction(s -> {
            return switch (s) {
                case "plane" -> GuiPipelines.PLANE;
                case "screen" -> GuiPipelines.SCREEN_QUAD;
                default -> GuiPipelines.ANIMATED;
            };
//            return shaderMap.get(key);
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
//
//    private static String builtin(String name, String lib) {
//        // Base path to the shaders folder
//        String basePath = "/shaders/";
//
//        try (
//                var nameStream = Pipeline.class.getResourceAsStream(basePath + name);
//                var libStream = Pipeline.class.getResourceAsStream(basePath + lib)
//        ) {
//            if (nameStream == null) {
//                throw new IllegalArgumentException("Shader resource not found: " + name);
//            }
//            if (libStream == null) {
//                throw new IllegalArgumentException("Library resource not found: " + lib);
//            }
//
//            // Read the shader file content
//            String shaderContent = new String(nameStream.readAllBytes());
//
//            // Read the library file content
//            String libContent = new String(libStream.readAllBytes());
//
//            // Replace all instances of #color in the shader content with the library content
//            return shaderContent.replace("#process", libContent);
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to read built-in shader or library file: " + name + ", " + lib, e);
//        }
//    }
}