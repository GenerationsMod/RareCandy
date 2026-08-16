package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.pokeutils.reader.ITextureLoader;
import gg.generations.rarecandy.pokeutils.resource.JarResourceReader;
import gg.generations.rarecandy.renderer.pipeline.ShaderSource;
import gg.generations.rarecandy.renderer.pipeline.SnippetFinder;
import gg.generations.rarecandy.renderer.pipeline.traditional.TraditionalPipeline;
import gg.generations.rarecandy.renderer.pipeline.util.*;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

public class GuiPipelines {
    public static final Vector3f light0 = new Vector3f(0.5f, 0.5f, -0.5f).normalize();
    public static final Vector3f light1 = new Vector3f(-0.3f, 0.4f, 0.7f).normalize();

    public static final Vector4f colorMOdulator = new Vector4f(1f, 1f, 1f, 1f);

    private static final Vector4f ONE_V = new Vector4f(1,1, 1, 1);

    public static SnippetFinder SNIPPETS = SnippetFinder.create(JarResourceReader.create("shaders/snippets", GuiPipelines.class))
            .addSnippet("lib", "fog")
            .addSnippet("lib", "light")
            .addSnippet("lib", "material")
            .addSnippet("lib", "paradox")
            .addSnippet("lib", "structs")
            .addSnippet("lib", "terastal")
            .addSnippet("lib", "utils")
            .addSnippet("lib", "vertex");

    public static TraditionalPipeline G_BUFFER;
    public static TraditionalPipeline LIGHT;
    public static TraditionalPipeline GRID;
    public static TraditionalPipeline FOG;

    public static double pingpong(double time) {
        return (int) (Math.sin(time * Math.PI * 2) * 7 + 7);
    }

    public static void onInitialize(RareCandyCanvas canvas, PokeUtilsGui.Settings settings) {

        var source = ShaderSource.create().finder(SNIPPETS);
        var reader = JarResourceReader.create("shaders/deferred", GuiPipelines.class);

        RenderPasses.init(reader, source, canvas, settings, light0, light1);

        G_BUFFER = TraditionalPipeline.builder(source.compileSet(reader, "model"))
                .apply(builder -> GuiPipelines.common(builder, canvas,settings))
                .build();

        GRID = TraditionalPipeline.builder(source.compileSet(reader, "grid"))
                .autoMat4(Scope.GLOBAL, "inverseProjectionMatrix", ctx -> canvas.camera.getInverseProjectionMatrix())
                .autoMat4(Scope.GLOBAL, "inverseViewMatrix", ctx -> canvas.camera.getInverseViewMatrix())
                .autoMat4(Scope.GLOBAL, "viewMatrix", ctx -> canvas.camera.getViewMatrix())
                .autoMat4(Scope.GLOBAL, "projectionMatrix", ctx -> canvas.camera.getProjectionMatrix())
                .autoVec3(Scope.GLOBAL, "cameraPosition", ctx -> canvas.camera.getPosition())
                .build();
    }

    private static void common(TraditionalPipeline.Builder builder, RareCandyCanvas canvas, PokeUtilsGui.Settings settings) {
        builder.autoMat4(Scope.GLOBAL, "viewMatrix", (ctx) -> canvas.camera.getViewMatrix())
                .autoMat4(Scope.GLOBAL, "projectionMatrix", (ctx) -> canvas.camera.getProjectionMatrix())
                .autoVec4(Scope.GLOBAL, "ColorModulator", (ctx) -> colorMOdulator)
                .autoVec4(Scope.GLOBAL, "tint", (ctx) -> ONE_V)

                .addUBO(Scope.GLOBAL, "Fog", 0, (ctx) -> canvas.getFogUploader().id)
                .addSSBORange(Scope.MODEL, "VertexBuffer", 0, ctx -> ctx.object().modelBuffer, ctx -> ctx.object().vertex)
                .addSSBORange(Scope.MODEL, "IndexBuffer", 1, ctx -> ctx.object().modelBuffer, ctx -> ctx.object().index)
                .addSSBORange(Scope.MODEL, "MeshOffsetBuffer", 2, ctx -> ctx.object().modelBuffer, ctx -> ctx.object().meshOffsets)
                .addSSBO(Scope.MODEL, "VariantBuffer", 3, ctx -> ctx.object().variant.getBufferId())
                .addSSBO(Scope.MODEL, "MaterialBuffer", 4, ctx -> ctx.object().material.getBufferId())
                .addSSBO(Scope.MODEL, "InstanceBuffer", 5, ctx -> ctx.object().instance.getBufferId())
                .addSSBO(Scope.MODEL, "DrawInfoBuffer", 6, ctx -> ctx.object().drawInfo.getBufferId())
                .autoInt(Scope.MODEL, "Selected", ctx -> canvas.selected.getMeshId())

                .autoSampler2DArray(Scope.MODEL, "images", 0, ctx -> ctx.object().images)

                //Terastal
//                .autoVec3(Scope.GLOBAL, "teraTint", (ctx) -> settings.terastalization.tint.getValue())
//                .autoBool(Scope.GLOBAL, "teraActive", (ctx) -> settings.terastalization.enabled.getValue())

                //Paradox
                .autoInt(Scope.GLOBAL, "frame", (ctx) -> 0) //(int) pingpong(RareCandyCanvas.getTime() % 1d)) TODO: Readd time.
                .autoSampler2D(Scope.GLOBAL, "paradoxTexture", 1, ctx -> ITextureLoader.instance().getTexture("paradox_mask").id())

                .prePostDraw(material -> {
                    if (material.disableDepth()) {
                        GL11.glDisable(GL11.GL_DEPTH_TEST);
                    }

                    material.cullType().enable();
                    material.blendType().enable();
                }, material -> {
                    if (material.disableDepth()) {
                        GL11.glEnable(GL11.GL_DEPTH_TEST);
                    }

                    material.cullType().disable();
                    material.blendType().disable();
                });
        ;
    }
}
