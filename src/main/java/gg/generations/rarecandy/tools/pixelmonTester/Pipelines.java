package gg.generations.rarecandy.tools.pixelmonTester;

import gg.generations.rarecandy.legacy.pipeline.ShaderProgram;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.IOException;

public class Pipelines {

//    public final ShaderProgram animated;

//    public Pipelines(Matrix4f projectionMatrix, Matrix4f viewMatrix) {
//        var base = new ShaderProgram.Builder()
//                .supplyUniform("viewMatrix", ctx -> ctx.uniform().uploadMat4f(viewMatrix))
//                .supplyUniform("modelMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().getTransform()))
//                .supplyUniform("projectionMatrix", (ctx) -> ctx.uniform().uploadMat4f(projectionMatrix))
//                .supplyUniform("lightPosition", ctx -> ctx.uniform().uploadVec3f(new Vector3f(0, 2, 0)))
//                .supplyUniform("reflectivity", ctx -> ctx.uniform().uploadFloat(0.3f))
//                .supplyUniform("shineDamper", ctx -> ctx.uniform().uploadFloat(0.3f))
//                .supplyUniform("intColor", ctx -> ctx.uniform().uploadInt(0xFFFFFF))
//                .supplyUniform("diffuseColorMix", ctx -> ctx.uniform().uploadFloat(0.7f))
//                .supplyUniform("diffuse", ctx -> {
//                    ctx.instance().getMaterial().getDiffuseTexture().bind(0);
//                    ctx.uniform().uploadInt(0);
//                });
//
//        this.animated = new ShaderProgram.Builder(base)
//                .shader(builtin("static/static.vs.glsl"), builtin("static/static.fs.glsl"))
////                .supplyUniform("boneTransforms", ctx -> ctx.uniform().uploadMat4fs(((AnimatedObjectInstance) ctx.instance()).getTransforms()))
//                .build();
//    }

    private static String builtin(String name) {
        try (var is = ShaderProgram.class.getResourceAsStream("/shaders/" + name)) {
            assert is != null;
            return new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read built in shader", e);
        }
    }
}
