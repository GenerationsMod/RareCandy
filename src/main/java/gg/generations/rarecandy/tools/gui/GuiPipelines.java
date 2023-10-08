package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.renderer.animation.AnimationController;
import gg.generations.rarecandy.renderer.pipeline.Pipeline;
import gg.generations.rarecandy.renderer.storage.AnimatedObjectInstance;
import org.joml.Vector3f;

import java.io.IOException;

import static gg.generations.rarecandy.tools.gui.RareCandyCanvas.projectionMatrix;

public class GuiPipelines {
    private static final Pipeline.Builder BASE = new Pipeline.Builder()
            .supplyUniform("viewMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().viewMatrix()))
            .supplyUniform("modelMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().transformationMatrix()))
            .supplyUniform("projectionMatrix", (ctx) -> ctx.uniform().uploadMat4f(projectionMatrix))
            .supplyUniform("lightPosition", ctx -> ctx.uniform().uploadVec3f(new Vector3f(0, 2, 0)))
            .supplyUniform("reflectivity", ctx -> ctx.uniform().uploadFloat(0.3f))
            .supplyUniform("shineDamper", ctx -> ctx.uniform().uploadFloat(0.3f))
            .supplyUniform("intColor", ctx -> ctx.uniform().uploadInt(0xFFFFFF))
            .supplyUniform("diffuseColorMix", ctx -> ctx.uniform().uploadFloat(0.7f))
            .supplyUniform("boneTransforms", ctx -> ctx.uniform().uploadMat4fs(ctx.instance() instanceof AnimatedObjectInstance instance ? instance.getTransforms() : AnimationController.NO_ANIMATION))
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

    public static final Pipeline STATIC = new Pipeline.Builder(BASE)
            .shader(builtin("static/static.vs.glsl"), builtin("static/static.fs.glsl"))
            .build();

    public static final Pipeline ANIMATED = new Pipeline.Builder(BASE)
            .shader(builtin("animated/animated.vs.glsl"), builtin("animated/animated.fs.glsl"))
            .build();

    public static final Pipeline TRANSPARENT = new Pipeline.Builder(BASE)
            .shader(builtin("animated/animated.vs.glsl"), builtin("animated/transparent.fs.glsl"))
            .build();

    public static final Pipeline POKEMON_EYES = new Pipeline.Builder(BASE)
            .shader(builtin("animated/animated.vs.glsl"), builtin("animated/animated.fs.glsl"))
            .supplyUniform("boneTransforms", ctx -> ctx.uniform().uploadMat4fs(ctx.instance() instanceof AnimatedObjectInstance instance ? instance.getTransforms() : AnimationController.NO_ANIMATION))
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