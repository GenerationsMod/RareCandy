package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.pokeutils.BlendType;
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader;
import gg.generations.rarecandy.renderer.animation.Transform;
import gg.generations.rarecandy.renderer.model.material.MaterialUploader;
import gg.generations.rarecandy.renderer.model.material.PipelineRegistry;
import gg.generations.rarecandy.renderer.pipeline.traditional.TraditionalPipeline;
import gg.generations.rarecandy.renderer.pipeline.util.Scope;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import gg.generations.rarecandy.renderer.storage.AnimatedObjectInstance;
import gg.generations.rarecandy.renderer.storage.InstanceBlockUploader;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

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

        TraditionalPipeline ANIMATED = TraditionalPipeline.builder(builtin("experimental/animated.vs.glsl"), builtin("experimental/animated.fs.glsl"))
                .autoMat4(Scope.GLOBAL, "viewMatrix", (instance, object) -> RareCandyCanvas.viewMatrix)
                .autoMat4(Scope.GLOBAL, "projectionMatrix", (instance, object) -> projectionMatrix)
                .autoVec2(Scope.INSTANCE, "uvOffset", (instance, object) -> {
                    Transform transform = object.getTransform(instance.variant());

                    if (instance instanceof AnimatedObjectInstance animatedInstance) {
                        var t = animatedInstance.getTransform(object.getMaterial(instance.variant()).getMaterialName());

                        if (t != null && !t.isUnit()) {
                            transform = t;
                        }
                    }

                    var offset = transform.offset();

                    if(offset == null) offset = Transform.DEFAULT_OFFSET;

                    return offset;
                })
                .autoVec2(Scope.INSTANCE, "uvScale", (instance, object) -> {
                    Transform transform = object.getTransform(instance.variant());

                    if (instance instanceof AnimatedObjectInstance animatedObjectInstance) {
                        var t = animatedObjectInstance.getTransform(object.getMaterial(instance.variant()).getMaterialName());

                        if (t != null && !t.isUnit()) {
                            transform = t;
                        }
                    }

                    var scale = transform.scale();

                    if(scale == null) scale = Transform.DEFAULT_SCALE;

                    return scale;
                })
                .autoVec3(Scope.GLOBAL, "Light0_Direction", (instance, object) -> light0)
                .autoVec3(Scope.GLOBAL, "Light1_Direction", (instance, object) -> light1)
                .autoBool(Scope.GLOBAL, "tera", (instance, object) -> settings.terastalization.enabled.getValue())
                .autoVec3(Scope.GLOBAL, "teraTint", (instance, object) -> settings.terastalization.tint.getValue())
                .addUniform(Scope.GLOBAL, "light", (uniform, context) -> {
                    var light = (int) (RareCandyCanvas.getLightLevel() * 15);

                    uniform.upload2i(0, light);
                })
                .autoVec3(Scope.GLOBAL, "tint", (instance, object) -> ONE)
                .autoVec4(Scope.GLOBAL, "ColorModulator", (instance, object) -> colorMOdulator)
                .autoInt(Scope.GLOBAL, "frame", (instance, object) -> {
                    return (int) pingpong(RareCandyCanvas.getTime() % 1d);
                })
                .autoSampler2D(Scope.INSTANCE, "diffuse", 0, (instance, object) -> {
                    return ITextureLoader.instance().getTexture(object.getMaterial(instance.variant()).images().getDiffuse()).getId();
                })
                .autoSampler2D(Scope.INSTANCE, "emission", 2, (instance, object) -> ITextureLoader.instance().getTexture(object.getMaterial(instance.variant()).images().getEmission()).getId())
                .autoSampler2D(Scope.INSTANCE, "layer", 3, (instance, object) -> ITextureLoader.instance().getTexture(object.getMaterial(instance.variant()).images().getLayer()).getId())
                .autoSampler2D(Scope.INSTANCE, "mask", 4, (instance, object) -> ITextureLoader.instance().getTexture(object.getMaterial(instance.variant()).images().getMask()).getId())
                .autoSampler2D(Scope.INSTANCE, "lightmap", 5, (instance, object) -> ITextureLoader.instance().getTexture("light_map").getId())
                .autoSampler2D(Scope.INSTANCE, "paradoxMask", 6, (instance, object) -> ITextureLoader.instance().getTexture("paradox_mask").getId())
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
                .addUBO(Scope.GLOBAL, "Fog", 0, (instance, object) -> canvas.getFogUploader().id)
                .addUBO(Scope.INSTANCE, "Material", 2, (instance, object) -> object.getMaterial(instance.variant()).bindMaterial())
                .addUBO(Scope.INSTANCE, "Instance", 1, (instance, object) -> InstanceBlockUploader.bind(instance))
                .build();

        TraditionalPipeline PLANE = TraditionalPipeline.builder(builtin("original/animated/plane.vs.glsl"), builtin("original/animated/plane.fs.glsl"))
                .autoMat4(Scope.GLOBAL, "viewMatrix", (instance, object) -> RareCandyCanvas.viewMatrix)
                .autoMat4(Scope.GLOBAL, "projectionMatrix", (instance, object) -> projectionMatrix)
                .autoFloat(Scope.GLOBAL, "lightLevel", (instance, object) -> RareCandyCanvas.getLightLevel())
                .autoFloat(Scope.GLOBAL, "radius", (instance, object) -> RareCandyCanvas.radius)
                .autoBool(Scope.GLOBAL, "render", (instance, object) -> RareCandyCanvas.renderingFrame)
                .prePostDraw(material -> BlendType.Regular.enable(), material -> BlendType.Regular.disable())
                .build();

        TraditionalPipeline SCREEN_QUAD = TraditionalPipeline.builder(builtin("original/screen/screen_quad.vs.glsl"), builtin("original/screen/screen_quad.fs.glsl"))
                .autoSampler2D(Scope.GLOBAL, "screenTexture", 0, (instance, object) -> RareCandyCanvas.framebuffer.getId())
                .build();

        PipelineRegistry.setFunction(s -> {
            return switch (s) {
                case "plane" -> PLANE;
                case "screen" -> SCREEN_QUAD;
                default -> ANIMATED;
            };
        });
    }

    private static String builtin(String name) {
        try (var is = TraditionalPipeline.class.getResourceAsStream("/shaders/" + name)) {
            assert is != null;
            return new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read built in shader", e);
        }
    }
}