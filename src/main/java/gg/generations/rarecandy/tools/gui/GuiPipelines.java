package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.pokeutils.BlendType;
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader;
import gg.generations.rarecandy.renderer.components.MultiRenderObject;
import gg.generations.rarecandy.renderer.pipeline.compute.ComputePipeline;
import gg.generations.rarecandy.renderer.pipeline.traditional.TraditionalPipeline;
import gg.generations.rarecandy.renderer.pipeline.util.*;
import gg.generations.rarecandy.renderer.rendering.RenderStage;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import gg.generations.rarecandy.renderer.textures.BlankTexture;
import gg.generations.rarecandy.renderer.textures.ITexture;
import gg.generations.rarecandy.renderer.textures.TextureArray;
import gg.generations.rarecandy.renderer.ubo.UniformBlockUploader;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import static gg.generations.rarecandy.renderer.pipeline.Pipelines.builtin;
import static gg.generations.rarecandy.tools.gui.RareCandyCanvas.projectionMatrix;
import static org.lwjgl.opengl.GL15C.glBufferData;
import static org.lwjgl.opengl.GL42C.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT;
import static org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BARRIER_BIT;

public class GuiPipelines {
    public static final Vector3f light0 = new Vector3f(0.5f, 0.5f, -0.5f).normalize();
    public static final Vector3f light1 = new Vector3f(-0.3f, 0.4f, 0.7f).normalize();

    public static final Vector4f colorMOdulator = new Vector4f(1f, 1f, 1f, 1f);

    private static final Vector3f ONE = new Vector3f(1,1, 1);
    private static final Vector4f ONE_V = new Vector4f(1,1, 1, 1);

    public static ComputePipeline PARADOX;

    public static TraditionalPipeline ANIMATED;
    public static TraditionalPipeline PLANE;


    public static double pingpong(double time) {
        return (int) (Math.sin(time * Math.PI * 2) * 7 + 7);
    }

    public static int instanceId;

    private static final ITexture[] textures = new ITexture[3];

    public static void onInitialize(RareCandyCanvas canvas, PokeUtilsGui.Settings settings) {

//        PARADOX = ComputePipeline.builder(builtin("rewrite/paradox.cs.glsl"))
//                .autoInt(Scope.GLOBAL, "frame", (ctx) -> (int) pingpong(RareCandyCanvas.getTime() % 1d))
//                .autoSampler2D(Scope.GLOBAL, "sampler", 0, (ctx) -> ITextureLoader.instance().getTexture("paradox_mask").id())
//                .autoImage2D(Scope.GLOBAL, "outputTexture", 0, ctx -> textures[0])
//                .build();

        ANIMATED = TraditionalPipeline.builder(builtin("true/generic.vs.glsl", "true/libs"), builtin("true/generic.fs.glsl", "true/libs"))
                .apply(builder -> GuiPipelines.common(builder, canvas,settings))
                .build();

//        PLANE = TraditionalPipeline.builder(builtin("original/animated/plane.vs.glsl"), builtin("original/animated/plane.fs.glsl"))
//                .autoMat4(Scope.GLOBAL, "viewMatrix", (ctx) -> RareCandyCanvas.viewMatrix)
//                .autoMat4(Scope.GLOBAL, "projectionMatrix", (ctx) -> projectionMatrix)
//                .autoFloat(Scope.GLOBAL, "lightLevel", (ctx) -> RareCandyCanvas.getLightLevel())
//                .autoFloat(Scope.GLOBAL, "radius", (ctx) -> RareCandyCanvas.radius)
//                .autoBool(Scope.GLOBAL, "render", (ctx) -> RareCandyCanvas.renderingFrame)
//                .prePostDraw(material -> BlendType.Regular.enable(), material -> BlendType.Regular.disable())
//                .build();
    }

    private static void common(TraditionalPipeline.Builder builder, RareCandyCanvas canvas, PokeUtilsGui.Settings settings) {
        builder.autoMat4(Scope.GLOBAL, "viewMatrix", (ctx) -> RareCandyCanvas.viewMatrix)
                .autoMat4(Scope.GLOBAL, "projectionMatrix", (ctx) -> projectionMatrix)
                .autoVec4(Scope.GLOBAL, "ColorModulator", (ctx) -> colorMOdulator)
                .autoVec4(Scope.GLOBAL, "tint", (ctx) -> ONE_V)
                .autoBool(Scope.MODEL, "renderTranslucent", ctx -> ctx.stage() == RenderStage.TRANSPARENT)

                .addUBO(Scope.GLOBAL, "Fog", 0, (ctx) -> canvas.getFogUploader().id)
                .addSSBORange(Scope.MODEL, "VertexBuffer", 0, ctx -> ctx.object().modelBuffer, ctx -> ctx.object().vertex)
                .addSSBORange(Scope.MODEL, "IndexBuffer", 1, ctx -> ctx.object().modelBuffer, ctx -> ctx.object().index)
                .addSSBORange(Scope.MODEL, "MeshOffsetBuffer", 2, ctx -> ctx.object().modelBuffer, ctx -> ctx.object().meshOffsets)
                .addSSBORange(Scope.MODEL, "VariantBuffer", 3, ctx -> ctx.object().modelBuffer, ctx -> ctx.object().variant)
                .addSSBORange(Scope.MODEL, "MaterialBuffer", 4, ctx -> ctx.object().modelBuffer, ctx -> ctx.object().material)
                .addSSBO(Scope.MODEL, "InstanceBuffer", 5, ctx -> ctx.object().instanceBuffer.getBufferId())
                .addSSBO(Scope.MODEL, "TransformBuffer", 6, ctx -> ctx.object().uvTransformBuffer.getBufferId())

                .autoSampler2DArray(Scope.MODEL, "images", 0, ctx -> ctx.object().images)

                //Terastal
                .autoVec3(Scope.GLOBAL, "teraTint", (ctx) -> settings.terastalization.tint.getValue())
                .autoBool(Scope.GLOBAL, "teraActive", (ctx) -> settings.terastalization.enabled.getValue())

                //Paradox
                .autoInt(Scope.GLOBAL, "frame", (ctx) -> (int) pingpong(RareCandyCanvas.getTime() % 1d))
                .autoSampler2D(Scope.GLOBAL, "paradoxTexture", 1, ctx -> ITextureLoader.instance().getTexture("paradox_mask").id())

                //Light
                .addUniform(Scope.GLOBAL, "light", (uniform, ctx) -> uniform.upload2i(0, settings.light.lightLevel.get()))
                .autoSampler2D(Scope.GLOBAL, "lightmap", 2, (ctx) -> ITextureLoader.instance().getTexture("light_map").id())
                .autoVec3(Scope.GLOBAL, "Light0_Direction", (ctx) -> light0)
                .autoVec3(Scope.GLOBAL, "Light1_Direction", (ctx) -> light1)

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
