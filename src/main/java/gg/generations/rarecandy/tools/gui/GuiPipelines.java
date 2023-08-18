package gg.generations.rarecandy.tools.gui;

import gg.generations.pokeutils.reader.TextureReference;
import gg.generations.rarecandy.legacy.model.misc.Texture;
import gg.generations.rarecandy.legacy.pipeline.ShaderProgram;
import org.joml.Matrix4f;

import java.io.IOException;


public class GuiPipelines {

//    private static final ShaderProgram.Builder BASE = new ShaderProgram.Builder()
//            .supplyUniform("viewMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().viewMatrix()))
//            .supplyUniform("modelMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().transformationMatrix()))
//            .supplyUniform("projectionMatrix", (ctx) -> ctx.uniform().uploadMat4f(projectionMatrix))
//            .supplyUniform("lightPosition", ctx -> ctx.uniform().uploadVec3f(new Vector3f(0, 2, 0)))
//            .supplyUniform("reflectivity", ctx -> ctx.uniform().uploadFloat(0.3f))
//            .supplyUniform("shineDamper", ctx -> ctx.uniform().uploadFloat(0.3f))
//            .supplyUniform("intColor", ctx -> ctx.uniform().uploadInt(0xFFFFFF))
//            .supplyUniform("diffuseColorMix", ctx -> ctx.uniform().uploadFloat(0.7f))
//            .supplyUniform("diffuse", ctx -> {
//                ctx.object().getVariant(ctx.instance().variant()).getDiffuseTexture().bind(0);
//                ctx.uniform().uploadInt(0);
//            });
//
//    public static final ShaderProgram STATIC = new ShaderProgram.Builder(BASE)
//            .shader(builtin("static/static.vs.glsl"), builtin("static/static.fs.glsl"))
//            .build();
//
//    public static final ShaderProgram ANIMATED = new ShaderProgram.Builder(BASE)
//            .shader(builtin("animated/animated.vs.glsl"), builtin("animated/animated.fs.glsl"))
//            .supplyUniform("boneTransforms", ctx -> ctx.uniform().uploadMat4fs(((AnimatedObjectInstance) ctx.instance()).getTransforms()))
//            .build();
//
//    public static final ShaderProgram TRANSPARENT = new ShaderProgram.Builder(BASE)
//            .shader(builtin("animated/animated.vs.glsl"), builtin("animated/transparent.fs.glsl"))
//            .prePostDraw(() -> {
//                GL11.glEnable(GL11.GL_BLEND);
//                GL11.glEnable(GL11.GL_DEPTH_TEST);
//                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//            }, () -> {
//                GL11.glDisable(GL11.GL_DEPTH_TEST);
//                GL11.glDisable(GL11.GL_BLEND);
//            })
//            .supplyUniform("boneTransforms", ctx -> ctx.uniform().uploadMat4fs(((AnimatedObjectInstance) ctx.instance()).getTransforms()))
//            .build();
//
//    public static final ShaderProgram POKEMON_EYES = new ShaderProgram.Builder(BASE)
//            .shader(builtin("animated/animated.vs.glsl"), builtin("animated/animated.fs.glsl"))
//            .supplyUniform("boneTransforms", ctx -> ctx.uniform().uploadMat4fs(((AnimatedObjectInstance) ctx.instance()).getTransforms()))
//            .build();

    public static void onInitialize() {
    }

    public static String builtin(String name) {
        try (var is = ShaderProgram.class.getResourceAsStream("/shaders/" + name)) {
            assert is != null;
            return new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read built in shader", e);
        }
    }
}