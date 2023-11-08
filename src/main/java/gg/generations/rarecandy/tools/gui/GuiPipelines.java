package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.renderer.animation.AnimationController;
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