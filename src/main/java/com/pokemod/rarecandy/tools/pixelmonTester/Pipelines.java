package com.pokemod.rarecandy.tools.pixelmonTester;

import com.pokemod.rarecandy.pipeline.Pipeline;
import com.pokemod.rarecandy.storage.AnimatedObjectInstance;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.IOException;

public class Pipelines {

    public final Pipeline animated;

    public Pipelines(Matrix4f projectionMatrix) {
        var base = new Pipeline.Builder()
                .supplyUniform("viewMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().viewMatrix()))
                .supplyUniform("modelMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().transformationMatrix()))
                .supplyUniform("projectionMatrix", (ctx) -> ctx.uniform().uploadMat4f(projectionMatrix))
                .supplyUniform("lightPosition", ctx -> ctx.uniform().uploadVec3f(new Vector3f(0, 2, 0)))
                .supplyUniform("LIGHT_reflectivity", ctx -> ctx.uniform().uploadFloat(0.001f))
                .supplyUniform("LIGHT_shineDamper", ctx -> ctx.uniform().uploadFloat(0.1f))
                .supplyUniform("LIGHT_color", ctx -> ctx.uniform().uploadVec3f(new Vector3f(1, 1, 1)))
                .supplyUniform("diffuse", ctx -> {
                    ctx.object().getMaterial(ctx.instance().materialId()).getDiffuseTexture().bind(0);
                    ctx.uniform().uploadInt(0);
                });

        this.animated = new Pipeline.Builder(base)
                .shader(builtin("animated/animated.vs.glsl"), builtin("animated/animated.fs.glsl"))
                .supplyUniform("boneTransforms", ctx -> ctx.uniform().uploadMat4fs(((AnimatedObjectInstance) ctx.instance()).getTransforms()))
                .build();
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
