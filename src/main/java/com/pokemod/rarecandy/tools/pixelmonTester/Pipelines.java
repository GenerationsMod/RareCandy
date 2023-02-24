package com.pokemod.rarecandy.tools.pixelmonTester;

import com.pokemod.rarecandy.pipeline.ShaderPipeline;
import com.pokemod.rarecandy.storage.AnimatedObjectInstance;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.IOException;

public class Pipelines {

    public final ShaderPipeline animated;
    public final ShaderPipeline pbrLight;
    public final ShaderPipeline pbrEmissive;

    public Pipelines(Matrix4f projectionMatrix) {
        var base = new ShaderPipeline.Builder()
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

        this.animated = new ShaderPipeline.Builder(base)
                .shader(builtin("animated/animated.vs.glsl"), builtin("animated/animated.fs.glsl"))
                .supplyUniform("boneTransforms", ctx -> ctx.uniform().uploadMat4fs(((AnimatedObjectInstance) ctx.instance()).getTransforms()))
                .build();

        var pbrBase = new ShaderPipeline.Builder()
                // Vertex Shader
                .supplyUniform("viewMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().viewMatrix()))
                .supplyUniform("modelMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().transformationMatrix()))
                .supplyUniform("projectionMatrix", (ctx) -> ctx.uniform().uploadMat4f(projectionMatrix))
                // Textures
                .supplyUniform("diffuse", ctx -> {
                    ctx.object().getMaterial(ctx.instance().materialId()).getDiffuseTexture().bind(0);
                    ctx.uniform().uploadInt(0);
                });

        this.pbrEmissive = new ShaderPipeline.Builder(pbrBase)
                .shader(builtin("pbr/pbr.vs.glsl"), builtin("pbr/pbr.glow.fs.glsl"))
                .build();

        this.pbrLight = new ShaderPipeline.Builder(pbrBase)
                .shader(builtin("pbr/pbr.vs.glsl"), builtin("pbr/pbr.fs.glsl"))
                // Fragment Shader
                .supplyUniform("camPos", ctx -> ctx.uniform().uploadVec3f(new Vector3f(0.1f, 0.01f, -2)))
                .supplyUniform("metallic", ctx -> ctx.uniform().uploadFloat(0.5f))
                .supplyUniform("roughness", ctx -> ctx.uniform().uploadFloat(0.4f))
                .supplyUniform("ao", ctx -> ctx.uniform().uploadFloat(1.0f))
                .supplyUniform("lightPositions", ctx -> ctx.uniform().uploadVec3fs(new Vector3f[]{
                        new Vector3f(-10.0f, 10.0f, 10.0f),
                        new Vector3f(10.0f, 10.0f, 10.0f),
                        new Vector3f(-10.0f, -10.0f, 10.0f),
                        new Vector3f(10.0f, -10.0f, 10.0f),
                }))
                .supplyUniform("lightColors", ctx -> ctx.uniform().uploadVec3fs(new Vector3f[]{
                        new Vector3f(300.0f, 300.0f, 300.0f),
                        new Vector3f(300.0f, 300.0f, 300.0f),
                        new Vector3f(300.0f, 300.0f, 300.0f),
                        new Vector3f(300.0f, 300.0f, 300.0f)
                }))
                .build();
    }

    private static String builtin(String name) {
        try (var is = ShaderPipeline.class.getResourceAsStream("/shaders/" + name)) {
            assert is != null;
            return new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read built in shader", e);
        }
    }
}
