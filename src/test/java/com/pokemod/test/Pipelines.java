package com.pokemod.test;

import com.pokemod.rarecandy.components.AnimatedMeshObject;
import com.pokemod.rarecandy.pipeline.Pipeline;
import com.pokemod.rarecandy.storage.AnimatedInstance;
import com.pokemod.test.tests.StatUpTest;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11C;

import java.io.IOException;

public class Pipelines {
    private static final Pipeline.Builder BASE = new Pipeline.Builder()
            .supplyUniform("viewMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().viewMatrix()))
            .supplyUniform("modelMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().transformationMatrix()))
            .supplyUniform("projectionMatrix", (ctx) -> ctx.uniform().uploadMat4f(FeatureTester.PROJECTION_MATRIX))
            .supplyUniform("lightPosition", ctx -> ctx.uniform().uploadVec3f(new Vector3f(0, 2, 0)))
            .supplyUniform("LIGHT_reflectivity", ctx -> ctx.uniform().uploadFloat(0.001f))
            .supplyUniform("LIGHT_shineDamper", ctx -> ctx.uniform().uploadFloat(0.1f))
            .supplyUniform("LIGHT_color", ctx -> ctx.uniform().uploadVec3f(new Vector3f(1, 1, 1)))
            .supplyUniform("diffuse", ctx -> {
                ctx.object().getMaterial(ctx.instance().materialId()).getDiffuseTexture().bind(0);
                ctx.uniform().uploadInt(0);
            });

    public static final Pipeline STATIC = new Pipeline.Builder(BASE)
            .shader(builtin("static/static.vs.glsl"), builtin("static/static.fs.glsl"))
            .build();


    public static final Pipeline STAT_UP = new Pipeline.Builder(BASE)
            .shader(builtin("stat_up/stat_up.vs.glsl"), builtin("stat_up/stat_up.fs.glsl"))
            .supplyUniform("time", ctx -> ctx.uniform().uploadFloat((float) StatUpTest.getTimePassed() * 2500))
            .prePostDraw(() -> GL11C.glEnable(GL11C.GL_BLEND),() -> GL11C.glDisable(GL11C.GL_BLEND))
            .build();


    public static final Pipeline ANIMATED = new Pipeline.Builder(BASE)
            .shader(builtin("animated/animated.vs.glsl"), builtin("animated/animated.fs.glsl"))
            .supplyUniform("boneTransforms", ctx -> ctx.uniform().uploadMat4fs(((AnimatedInstance) ctx.instance()).transforms))
            .build();

    public static final Pipeline POKEMON_EYES = new Pipeline.Builder(BASE)
            .shader(builtin("animated/animated.vs.glsl"), builtin("animated_pokemon_eyes.fs.glsl"))
            .supplyUniform("boneTransforms", ctx -> ctx.uniform().uploadMat4fs(((AnimatedInstance) ctx.instance()).transforms))
            .build();

    public static void onInitialize() {}

    private static String builtin(String name) {
        try (var is = Pipeline.class.getResourceAsStream("/shaders/" + name)) {
            assert is != null;
            return new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read built in shader", e);
        }
    }
}
