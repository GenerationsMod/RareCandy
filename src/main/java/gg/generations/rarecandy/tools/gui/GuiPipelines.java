package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.model.material.TransparentMaterial;
import gg.generations.rarecandy.pipeline.Pipeline;
import gg.generations.rarecandy.storage.AnimatedObjectInstance;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

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
            .supplyUniform("diffuse", ctx -> {
                ctx.object().getVariant(ctx.instance().variant()).material().getDiffuseTexture().bind(0);
                ctx.uniform().uploadInt(0);
            });

    public static final Pipeline STATIC = new Pipeline.Builder(BASE)
            .shader(builtin("static/static.vs.glsl"), builtin("static/static.fs.glsl"))
            .build();

    public static final Pipeline ANIMATED = new Pipeline.Builder(BASE)
            .shader(builtin("animated/animated.vs.glsl"), builtin("animated/animated.fs.glsl"))
            .supplyUniform("boneTransforms", ctx -> ctx.uniform().uploadMat4fs(((AnimatedObjectInstance) ctx.instance()).getTransforms()))
            .build();

    public static final Pipeline TRANSPARENT = new Pipeline.Builder(BASE)
            .shader(builtin("animated/animated.vs.glsl"), builtin("animated/transparent.fs.glsl"))
            .prePostDraw(() -> {
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            }, () -> {
                GL11.glDisable(GL11.GL_BLEND);
            })
            .supplyUniform("boneTransforms", ctx -> ctx.uniform().uploadMat4fs(((AnimatedObjectInstance) ctx.instance()).getTransforms()))
            .supplyUniform("alpha", ctx -> ctx.uniform().uploadFloat(((TransparentMaterial) ctx.object().getVariant(ctx.instance().variant()).material()).alpha))
            .build();

    public static final Pipeline POKEMON_EYES = new Pipeline.Builder(BASE)
            .shader(builtin("animated/animated.vs.glsl"), builtin("animated/animated.fs.glsl"))
            .supplyUniform("boneTransforms", ctx -> ctx.uniform().uploadMat4fs(((AnimatedObjectInstance) ctx.instance()).getTransforms()))
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