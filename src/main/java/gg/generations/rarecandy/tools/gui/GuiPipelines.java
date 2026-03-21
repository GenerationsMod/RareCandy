package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.pokeutils.BlendType;
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader;
import gg.generations.rarecandy.renderer.components.MultiRenderObject;
import gg.generations.rarecandy.renderer.pipeline.compute.ComputePipeline;
import gg.generations.rarecandy.renderer.pipeline.traditional.TraditionalPipeline;
import gg.generations.rarecandy.renderer.pipeline.util.*;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import gg.generations.rarecandy.renderer.textures.BlankTexture;
import gg.generations.rarecandy.renderer.textures.ITexture;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import static gg.generations.rarecandy.renderer.pipeline.Pipelines.builtin;
import static gg.generations.rarecandy.tools.gui.RareCandyCanvas.projectionMatrix;
import static org.lwjgl.opengl.GL15C.glBufferData;
import static org.lwjgl.opengl.GL42C.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT;
import static org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BARRIER_BIT;

public class GuiPipelines {
    public static final Vector3f light0 = new Vector3f(0.5f, 0.5f, -0.5f).normalize();
    public static final Vector3f light1 = new Vector3f(-0.3f, 0.4f, 0.7f).normalize();

    public static final Vector4f colorMOdulator = new Vector4f(1f, 1f, 1f, 1f);

    public static final Vector4f fogColor = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);

    private static final Vector3f ONE = new Vector3f(1,1, 1);
    public static ComputePipeline MATERIAL;
    public static ComputePipeline TRANSFORM;
    public static ComputePipeline PARADOX;

    public static TraditionalPipeline LIT;
    public static TraditionalPipeline SOLID;
    public static TraditionalPipeline TERSTAL;
    public static TraditionalPipeline PLANE;


    public static double pingpong(double time) {
        return (int) (Math.sin(time * Math.PI * 2) * 7 + 7);
    }

    private static int instanceId;

    private static final ITexture[] textures = new ITexture[3];

    public static void transformVertices(MultiRenderObject object, int instanceId) {
        GuiPipelines.instanceId = instanceId;
        GuiPipelines.TRANSFORM.useProgram();
        GuiPipelines.TRANSFORM.bindGlobal();
        GuiPipelines.TRANSFORM.bindModel(object);
        GuiPipelines.TRANSFORM.dispatch(GL_SHADER_STORAGE_BARRIER_BIT, (object.maxVertex + 255) / 256, object.meshes.length, 1);
    }

    public static void onInitialize(RareCandyCanvas canvas, PokeUtilsGui.Settings settings) {
        textures[0] = new BlankTexture(ITexture.Type.RGBA8, 1024, 1024, ITexture.ComputeAccess.READ_WRITE);
        textures[1] = new BlankTexture(ITexture.Type.RGBA8, 1024, 1024, ITexture.ComputeAccess.READ_WRITE);
        textures[2] = new BlankTexture(ITexture.Type.RGBA8, 1024, 1024, ITexture.ComputeAccess.READ_WRITE);

        TRANSFORM = ComputePipeline.builder(builtin("rewrite/transform.cs.glsl"))
                .addSSBORange(Scope.MODEL, "SrcBuffer", 0, ctx -> ctx.object().modelBuffer, ctx -> ctx.object().vertex)
                .addSSBORange(Scope.MODEL, "IndexBuffer", 1, ctx -> ctx.object().modelBuffer, ctx -> ctx.object().index)
                .addSSBORange(Scope.MODEL, "DrawCommands", 2, ctx -> ctx.object().modelBuffer, ctx -> ctx.object().draw)
                .addSSBO(Scope.MODEL, "InstanceBuffer", 3, ctx -> ctx.object().instanceBuffer.getBufferId())
                .addSSBO(Scope.MODEL, "TransformBuffer", 4, ctx -> ctx.object().uvTransformBuffer.getBufferId())
                .addSSBO(Scope.MODEL, "DstBuffer", 5, ctx -> ctx.object().destBuffer)
                .addUniform(Scope.MODEL, "variantSize", (uniform, ctx) -> uniform.uploadInt(ctx.object().meshes.length))
                .addUniform(Scope.GLOBAL, "instanceId", (uniform, ctx) -> uniform.uploadInt(instanceId))
                .build();

        PARADOX = ComputePipeline.builder(builtin("rewrite/paradox.cs.glsl"))
                .autoInt(Scope.GLOBAL, "frame", (ctx) -> (int) pingpong(RareCandyCanvas.getTime() % 1d))
                .autoSampler2D(Scope.GLOBAL, "sampler", 0, (ctx) -> ITextureLoader.instance().getTexture("paradox_mask").id())
                .autoImage2D(Scope.GLOBAL, "outputTexture", 0, ctx -> textures[0])
                .build();

        MATERIAL = ComputePipeline.builder(builtin("rewrite/material.cs.glsl"))
                .autoSampler2D(Scope.DRAW, "diffuse", 0, createMaterialTextureProvider(0, "neutral"))
                .autoSampler2D(Scope.DRAW, "layer", 1, createMaterialTextureProvider(1, "dark"))
                .autoSampler2D(Scope.DRAW, "mask", 2, createMaterialTextureProvider(2, "dark"))
                .autoSampler2D(Scope.DRAW, "emission", 3, createMaterialTextureProvider(3, "dark"))
                .autoSampler2D(Scope.DRAW, "paradoxTexture", 4, ctx -> textures[0].id())
                .addUniform(Scope.DRAW, "instanceId", (uniform, ctx) -> uniform.uploadInt(instanceId))
                .addUniform(Scope.DRAW, "meshId", (uniform, ctx) -> uniform.uploadInt(ctx.mesh()))
                .addUniform(Scope.DRAW, "variantSize", (uniform, ctx) -> uniform.uploadInt(ctx.object().meshes.length))
                .addSSBORange(Scope.DRAW, "MaterialBuffer", 0, ctx -> ctx.object().modelBuffer, ctx -> ctx.object().material)
                .addSSBORange(Scope.DRAW, "VariantBuffer", 1, ctx -> ctx.object().modelBuffer, ctx -> ctx.object().variant)
                .addSSBO(Scope.DRAW, "TransformBuffer", 2, ctx -> ctx.object().uvTransformBuffer.getBufferId())
                .autoImage2D(Scope.DRAW, "solidTex", 0, ctx -> textures[1])
                .autoImage2D(Scope.DRAW, "litTex", 1, ctx -> textures[2])
                .build();

        LIT = TraditionalPipeline.builder(builtin("rewrite/lit.vs.glsl"), builtin("rewrite/lit.fs.glsl"))
                .apply(builder -> GuiPipelines.common(builder, canvas))
                .autoSampler2D(Scope.GLOBAL, "tex", 0, ctx -> textures[2].id())
                .build();

        SOLID = TraditionalPipeline.builder(builtin("rewrite/solid.vs.glsl"), builtin("rewrite/solid.fs.glsl"))
                .apply(builder -> GuiPipelines.common(builder, canvas))
                .autoSampler2D(Scope.GLOBAL, "tex", 0, ctx -> textures[1].id())
                .addUniform(Scope.GLOBAL, "light", (uniform, ctx) -> {
                    var light = (int) (RareCandyCanvas.getLightLevel() * 15);

                    uniform.upload2i(0, light);
                })
                .autoSampler2D(Scope.GLOBAL, "lightmap", 1, (ctx) -> ITextureLoader.instance().getTexture("light_map").id())
                .autoVec3(Scope.GLOBAL, "Light0_Direction", (ctx) -> light0)
                .autoVec3(Scope.GLOBAL, "Light1_Direction", (ctx) -> light1)
                .build();

        TERSTAL = TraditionalPipeline.builder(builtin("rewrite/terastal.vs.glsl"), builtin("rewrite/terastal.fs.glsl"))
                .apply(builder -> GuiPipelines.common(builder, canvas))
                .autoSampler2D(Scope.GLOBAL, "tex", 0, ctx -> textures[1].id())
                .autoVec3(Scope.GLOBAL, "teraTint", (ctx) -> settings.terastalization.tint.getValue())
                .build();

        PLANE = TraditionalPipeline.builder(builtin("original/animated/plane.vs.glsl"), builtin("original/animated/plane.fs.glsl"))
                .autoMat4(Scope.GLOBAL, "viewMatrix", (ctx) -> RareCandyCanvas.viewMatrix)
                .autoMat4(Scope.GLOBAL, "projectionMatrix", (ctx) -> projectionMatrix)
                .autoFloat(Scope.GLOBAL, "lightLevel", (ctx) -> RareCandyCanvas.getLightLevel())
                .autoFloat(Scope.GLOBAL, "radius", (ctx) -> RareCandyCanvas.radius)
                .autoBool(Scope.GLOBAL, "render", (ctx) -> RareCandyCanvas.renderingFrame)
                .prePostDraw(material -> BlendType.Regular.enable(), material -> BlendType.Regular.disable())
                .build();

//        TraditionalPipeline SCREEN_QUAD = TraditionalPipeline.builder(builtin("original/screen/screen_quad.vs.glsl"), builtin("original/screen/screen_quad.fs.glsl"))
//                .autoSampler2D(Scope.GLOBAL, "screenTexture", 0, (ctx) -> RareCandyCanvas.framebuffer.())
//                .build();
    }

    private static void common(TraditionalPipeline.Builder builder, RareCandyCanvas canvas) {
        builder.autoMat4(Scope.GLOBAL, "viewMatrix", (ctx) -> RareCandyCanvas.viewMatrix)
                .autoMat4(Scope.GLOBAL, "projectionMatrix", (ctx) -> projectionMatrix)
                .autoVec4(Scope.GLOBAL, "ColorModulator", (ctx) -> colorMOdulator)
                .autoVec3(Scope.GLOBAL, "tint", (ctx) -> ONE)
                .addUBO(Scope.GLOBAL, "Fog", 0, (ctx) -> canvas.getFogUploader().id)
                .addSSBORange(Scope.MODEL, "VertexBuffer", 0, ctx -> ctx.object().destBuffer, ctx -> ctx.object().target)
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

    private static TextureIdSupplier createMaterialTextureProvider(int index, String defaultName) {
        return ctx -> {
            var variant = ctx.instance().variant();
            var imageIndex = ctx.object().getMaterial(ctx.mesh(), variant).images()[index];

            var name = ctx.object().images[imageIndex];

            return ITextureLoader.instance().getTexture(name, defaultName).id();
        };
    }

    public static void processMaterial(ObjectInstance instance, MultiRenderObject object, int mesh) {
        GuiPipelines.MATERIAL.useProgram();
        GuiPipelines.MATERIAL.bindDraw(instance, object, mesh);
        GuiPipelines.MATERIAL.dispatch(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT, 64,64,1);
    }
}