package com.pokemod.rarecandy.tools.pixelmonTester;

import com.pokemod.rarecandy.components.SkyboxRenderObject;
import com.pokemod.rarecandy.pipeline.ShaderPipeline;
import com.pokemod.rarecandy.pipeline.UniformBlockReference;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL20C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Example Implementation on how to handle shader pipelines
 */
public class Pipelines {
    private static final Logger LOGGER = LoggerFactory.getLogger("Pipeline Builder");
    private static final Map<ShaderSettings, ShaderPipeline> SHADER_CACHE = new HashMap<>();
    public final ShaderPipeline skybox;

    public Pipelines(Supplier<Matrix4f> projectionMatrix) {
        for (var lightingType : LightingType.values()) {
            LOGGER.info("Loading " + lightingType);
            cachePipeline(lightingType, true, projectionMatrix);
        }

        this.skybox = new ShaderPipeline.Builder()
                .shader(builtin("skybox/vert.glsl"), builtin("skybox/frag.glsl"))
                .uniform(new UniformBlockReference("SharedInfo", 0))
                .supplyUniform("cubemap", ctx -> {
                    GL13C.glActiveTexture(GL13C.GL_TEXTURE0);
                    GL11C.glBindTexture(GL20C.GL_TEXTURE_CUBE_MAP, ((SkyboxRenderObject) ctx.object()).texture.id);
                    ctx.uniform().uploadInt(0);
                })
                .build();
    }

    public ShaderPipeline cachePipeline(LightingType lightingType, boolean animated) {
        var settings = new ShaderSettings(lightingType, animated);
        if (!SHADER_CACHE.containsKey(settings)) throw new RuntimeException("Tried getting uncached value");
        return SHADER_CACHE.get(settings);
    }

    public void cachePipeline(LightingType lightingType, boolean animated, Supplier<Matrix4f> projectionMatrix) {
        SHADER_CACHE.computeIfAbsent(new ShaderSettings(lightingType, animated), shaderSettings -> {
            var vertexShader = builtin("pixelmon/" + (animated ? "animated" : "static") + "/vert" + lightingType.suffix + ".glsl");
            var fragmentShader = builtin("pixelmon/shared/frag" + lightingType.suffix + ".glsl");

            var builder = new ShaderPipeline.Builder()
                    .shader(vertexShader, fragmentShader)
                    .uniform(new UniformBlockReference("SharedInfo", 0))
                    .uniform(new UniformBlockReference("InstanceInfo", 1))
                    .supplyUniform("diffuse", ctx -> {
                        ctx.object().getMaterial(ctx.instance().materialId()).getDiffuseTexture().bind(0);
                        ctx.uniform().uploadInt(0);
                    });

            switch (lightingType) {
                case PBR -> builder
                        .supplyUniform("camPos", ctx -> ctx.uniform().uploadVec3f(new Vector3f(0f, 0f, -1)))
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
                        }));

                case BASIC_FAST -> builder
                        .supplyUniform("lightPosition", ctx -> ctx.uniform().uploadVec3f(new Vector3f(0, 2, 0)))
                        .supplyUniform("reflectivity", ctx -> ctx.uniform().uploadFloat(0.3f))
                        .supplyUniform("shineDamper", ctx -> ctx.uniform().uploadFloat(0.3f))
                        .supplyUniform("intColor", ctx -> ctx.uniform().uploadInt(0xFFFFFF))
                        .supplyUniform("diffuseColorMix", ctx -> ctx.uniform().uploadFloat(0.7f));

                case GLOSSY -> builder
                        .supplyUniform("lightPosition", ctx -> ctx.uniform().uploadVec3f(new Vector3f(0, 2, -1)))
                        .supplyUniform("reflectivity", ctx -> ctx.uniform().uploadFloat(3f))
                        .supplyUniform("shineDamper", ctx -> ctx.uniform().uploadFloat(2.5f))
                        .supplyUniform("intColor", ctx -> ctx.uniform().uploadInt(0xFFFFFF))
                        .supplyUniform("diffuseColorMix", ctx -> ctx.uniform().uploadFloat(0.5f));

                case GLOSSY_EXPERIMENTAL -> builder
                        .supplyUniform("underlyingTexCoordMix", ctx -> ctx.uniform().uploadFloat(0.8f))
                        .supplyUniform("stars", ctx -> {
                            PokemonTest.INSTANCE.starsTexture.bind(2);
                            ctx.uniform().uploadInt(2);
                        })
                        .supplyUniform("cubemap", ctx -> {
                            GL13C.glActiveTexture(GL13C.GL_TEXTURE1);
                            GL11C.glBindTexture(GL20C.GL_TEXTURE_CUBE_MAP, PokemonTest.INSTANCE.cubeMap.id);
                            ctx.uniform().uploadInt(1);
                        });

                case TERASTALLIZE -> builder
                        .supplyUniform("underlyingTexCoordMix", ctx -> ctx.uniform().uploadFloat(0.6f))
                        .supplyUniform("stars", ctx -> {
                            PokemonTest.INSTANCE.starsTexture.bind(2);
                            ctx.uniform().uploadInt(2);
                        })
                        .supplyUniform("normalMap", ctx -> {
                            PokemonTest.INSTANCE.normalMap.bind(3);
                            ctx.uniform().uploadInt(3);
                        })
                        .supplyUniform("cubemap", ctx -> {
                            GL13C.glActiveTexture(GL13C.GL_TEXTURE1);
                            GL11C.glBindTexture(GL20C.GL_TEXTURE_CUBE_MAP, PokemonTest.INSTANCE.terastallizeCubeMap.id);
                            ctx.uniform().uploadInt(1);
                        });
            }

            return builder.build();
        });
    }

    public enum LightingType {
        EMISSIVE("Emissive"),
        BASIC_FAST("Fast"),
        PBR("BRDF"),
        GLOSSY("GlassFast"),
        GLOSSY_EXPERIMENTAL("GlassExperimental"),
        TERASTALLIZE("TerastallizeCrystal");

        public final String suffix;

        LightingType(String shaderSuffix) {
            this.suffix = shaderSuffix;
        }
    }

    private static String builtin(String name) {
        try (var is = ShaderPipeline.class.getResourceAsStream("/shaders/" + name)) {
            assert is != null;
            return new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read built in shader", e);
        }
    }

    private record ShaderSettings(LightingType lightingType, boolean animated) {
    }
}
