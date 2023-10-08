package gg.generations.rarecandy.tools.pixelmonTester;

import gg.generations.rarecandy.renderer.pipeline.Pipeline;
import gg.generations.rarecandy.renderer.storage.AnimatedObjectInstance;
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
                .supplyUniform("reflectivity", ctx -> ctx.uniform().uploadFloat(0.3f))
                .supplyUniform("shineDamper", ctx -> ctx.uniform().uploadFloat(0.3f))
                .supplyUniform("intColor", ctx -> ctx.uniform().uploadInt(0xFFFFFF))
                .supplyUniform("diffuseColorMix", ctx -> ctx.uniform().uploadFloat(0.7f))
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
