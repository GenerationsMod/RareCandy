package com.pixelmongenerations.test;

import com.pixelmongenerations.rarecandy.components.AnimatedMeshObject;
import com.pixelmongenerations.rarecandy.pipeline.Pipeline;
import com.pixelmongenerations.test.tests.StatUpTest;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11C;

import java.io.IOException;
import java.util.function.Supplier;

public class Pipelines {
    private static final Pipeline.Builder STATIC = new Pipeline.Builder()
            .shader(builtin("static/static.vs.glsl"), builtin("static/static.fs.glsl"))
            .supplyUniform("viewMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().viewMatrix()))
            .supplyUniform("modelMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().transformationMatrix()))
            .supplyUniform("lightPosition", ctx -> ctx.uniform().uploadVec3f(new Vector3f(0, 2, 0)))
            .supplyUniform("LIGHT_reflectivity", ctx -> ctx.uniform().uploadFloat(0.3f))
            .supplyUniform("LIGHT_shineDamper", ctx -> ctx.uniform().uploadFloat(0.3f))
            .supplyUniform("LIGHT_color", ctx -> ctx.uniform().uploadVec3f(new Vector3f(1, 1, 1)))
            .supplyUniform("diffuse", ctx -> {
                ctx.object().getMaterial(ctx.instance().materialId()).getDiffuseTexture().bind(0);
                ctx.uniform().uploadInt(0);
            });

    private static final Pipeline.Builder STAT_UP = new Pipeline.Builder(STATIC)
            .shader(builtin("stat_up/stat_up.vs.glsl"), builtin("stat_up/stat_up.fs.glsl"))
            .supplyUniform("time", ctx -> ctx.uniform().uploadFloat((float) StatUpTest.getTimePassed() * 2500))
            .prePostDraw(() -> GL11C.glEnable(GL11C.GL_BLEND),() -> GL11C.glDisable(GL11C.GL_BLEND));


    private static final Pipeline.Builder ANIMATED = new Pipeline.Builder(STATIC)
            .shader(builtin("animated/animated.vs.glsl"), builtin("animated/animated.fs.glsl"))
            .supplyUniform("boneTransforms", ctx -> ctx.uniform().uploadMat4fs(((AnimatedMeshObject) ctx.object()).boneTransforms));

    public static Pipeline staticPipeline(Supplier<Matrix4f> projectionMatrix) {
        return new Pipeline.Builder(STATIC)
                .supplyUniform("projectionMatrix", (ctx) -> ctx.uniform().uploadMat4f(projectionMatrix.get()))
                .build();
    }

    public static Pipeline statUpPipeline(Supplier<Matrix4f> projectionMatrix) {
        return new Pipeline.Builder(STAT_UP)
                .supplyUniform("projectionMatrix", (ctx) -> ctx.uniform().uploadMat4f(projectionMatrix.get()))
                .build();
    }

    public static Pipeline animatedPipeline(Supplier<Matrix4f> projectionMatrix) {
        return new Pipeline.Builder(ANIMATED)
                .supplyUniform("projectionMatrix", (ctx) -> ctx.uniform().uploadMat4f(projectionMatrix.get()))
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
