package com.pokemod.rarecandy.tools.gui;

import com.pokemod.rarecandy.pipeline.ShaderPipeline;
import com.pokemod.rarecandy.storage.AnimatedObjectInstance;
import org.joml.Vector3f;

import java.io.IOException;

public class GuiPipelines {
    private static final ShaderPipeline.Builder BASE = new ShaderPipeline.Builder()
            .supplyUniform("viewMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().viewMatrix()))
            .supplyUniform("modelMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().transformationMatrix()))
            .supplyUniform("projectionMatrix", (ctx) -> ctx.uniform().uploadMat4f(RareCandyCanvas.projectionMatrix))
            .supplyUniform("lightPosition", ctx -> ctx.uniform().uploadVec3f(new Vector3f(0, 2, 0)))
            .supplyUniform("LIGHT_reflectivity", ctx -> ctx.uniform().uploadFloat(0.001f))
            .supplyUniform("LIGHT_shineDamper", ctx -> ctx.uniform().uploadFloat(0.1f))
            .supplyUniform("LIGHT_color", ctx -> ctx.uniform().uploadVec3f(new Vector3f(1, 1, 1)))
            .supplyUniform("diffuse", ctx -> {
                ctx.object().getMaterial(ctx.instance().materialId()).getDiffuseTexture().bind(0);
                ctx.uniform().uploadInt(0);
            });

    public static final ShaderPipeline STATIC = new ShaderPipeline.Builder(BASE)
            .shader(builtin("static/pbr.vs.glsl"), builtin("static/pbr.fs.glsl"))
            .build();

    public static final ShaderPipeline ANIMATED = new ShaderPipeline.Builder(BASE)
            .shader(builtin("animated/animated.vs.glsl"), builtin("animated/animated.fs.glsl"))
            .supplyUniform("boneTransforms", ctx -> ctx.uniform().uploadMat4fs(((AnimatedObjectInstance) ctx.instance()).getTransforms()))
            .build();

    public static final ShaderPipeline POKEMON_EYES = new ShaderPipeline.Builder(BASE)
            .shader(builtin("animated/animated.vs.glsl"), builtin("animated_pokemon_eyes.fs.glsl"))
            .supplyUniform("boneTransforms", ctx -> ctx.uniform().uploadMat4fs(((AnimatedObjectInstance) ctx.instance()).getTransforms()))
            .build();

    public static void onInitialize() {}

    private static String builtin(String name) {
        try (var is = ShaderPipeline.class.getResourceAsStream("/shaders/" + name)) {
            assert is != null;
            return new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read built in shader", e);
        }
    }
}