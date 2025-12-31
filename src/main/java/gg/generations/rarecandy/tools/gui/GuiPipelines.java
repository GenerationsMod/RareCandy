package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.pokeutils.BlendType;
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader;
import gg.generations.rarecandy.renderer.animation.Transform;
import gg.generations.rarecandy.renderer.loading.SbboOffset;
import gg.generations.rarecandy.renderer.model.material.MaterialUploader;
import gg.generations.rarecandy.renderer.model.material.PipelineRegistry;
import gg.generations.rarecandy.renderer.pipeline.compute.ComputePipeline;
import gg.generations.rarecandy.renderer.pipeline.traditional.TraditionalPipeline;
import gg.generations.rarecandy.renderer.pipeline.util.*;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import gg.generations.rarecandy.renderer.storage.AnimatedObjectInstance;
import gg.generations.rarecandy.renderer.storage.InstanceBlockUploader;
import gg.generations.rarecandy.renderer.textures.BlankTexture;
import gg.generations.rarecandy.renderer.textures.ITexture;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL43C;

import java.io.IOException;
import java.util.function.Consumer;

import static gg.generations.rarecandy.renderer.pipeline.Pipelines.builtin;
import static gg.generations.rarecandy.tools.gui.RareCandyCanvas.projectionMatrix;
import static gg.generations.rarecandy.tools.gui.RareCandyCanvas.renderingFrame;
import static org.lwjgl.opengl.GL15C.glBufferData;
import static org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BUFFER;

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

    public static final SbboOffset TRANSFORM_BUFFER_OFFSET =  new SbboOffset(0, 48*5000);
    public static int TRANSFORM_BUFFER_ID;

    private static ITexture[] textures = new ITexture[3];

    public static void onInitialize(RareCandyCanvas canvas, PokeUtilsGui.Settings settings) {
        textures[0] = new BlankTexture(ITexture.Type.RGBA_BYTE, 1024, 1024, ITexture.ComputeAccess.READ_WRITE);
        textures[1] = new BlankTexture(ITexture.Type.RGBA_BYTE, 1024, 1024, ITexture.ComputeAccess.READ_WRITE);
        textures[3] = new BlankTexture(ITexture.Type.RGBA_BYTE, 1024, 1024, ITexture.ComputeAccess.READ_WRITE);

        TRANSFORM_BUFFER_ID = GL43C.glGenBuffers();
        GL43C.glBindBuffer(GL_SHADER_STORAGE_BUFFER, TRANSFORM_BUFFER_ID);
        glBufferData(GL43C.GL_SHADER_STORAGE_BUFFER, TRANSFORM_BUFFER_OFFSET.size(), GL43C.GL_DYNAMIC_DRAW);
        GL43C.glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);

        InstanceBlockUploader.register(ObjectInstance.class, ObjectInstance.MAT4_SIZE);
        InstanceBlockUploader.register(AnimatedObjectInstance.class, ObjectInstance.MAT4_SIZE * 221);
        MaterialUploader.setup();

        TRANSFORM = ComputePipeline.builder(builtin("rewrite/transform.cs.glsl"))
                .addSSBORange(Scope.MODEL, "SrcBuffer", 0, ctx -> ctx.object().buffer, ctx -> ctx.object().vertex)
                .addSSBORange(Scope.MODEL, "IndexBuffer", 1, ctx -> ctx.object().buffer, ctx -> ctx.object().index)
                .addSSBORange(Scope.GLOBAL, "DstBuffer", 2, ctx -> TRANSFORM_BUFFER_ID, ctx -> TRANSFORM_BUFFER_OFFSET)
                .addUBO(Scope.INSTANCE, "Instance", 1, (ctx) -> InstanceBlockUploader.bind(ctx.instance()))
                .addUniform(Scope.GLOBAL, "transform", (uniform, ctx) -> {
                    var variant = ctx.object().getVariant(ctx.mesh(), ctx.instance().variant());

                    Transform transform = variant.offset();

                    if (ctx.instance() instanceof AnimatedObjectInstance animatedInstance) {

                        var material = variant.material();

                        var t = animatedInstance.getTransform(material);

                        if (t != null && !t.isUnit()) {
                            transform = t;
                        }
                    }

                    if(transform == null) {
                        transform = Transform.DEFAULT;
                    }

                    var scale = transform.scale();
                    var offset = transform.offset();

                    uniform.upload4f(scale.x, scale.y, offset.x, offset.y);
                })
                .build();

        PARADOX = ComputePipeline.builder(builtin("rewrite/paradox.cs.glsl"))
                .autoInt(Scope.GLOBAL, "frame", (ctx) -> (int) pingpong(RareCandyCanvas.getTime() % 1d))
                .autoSampler2D(Scope.GLOBAL, "sampler", 0, (ctx) -> ITextureLoader.instance().getTexture("paradox_mask").getId())
                .autoImage2D(Scope.DRAW, "texture", 0, ctx -> textures[0])
                .build();

        MATERIAL = ComputePipeline.builder(builtin("rewrite/material.cs.glsl"))
                .autoSampler2D(Scope.GLOBAL, "diffuse", 0, createMaterialTextureProvider(0))
                .autoSampler2D(Scope.GLOBAL, "emission", 1, createMaterialTextureProvider(1))
                .autoSampler2D(Scope.GLOBAL, "layer", 2, createMaterialTextureProvider(2))
                .autoSampler2D(Scope.GLOBAL, "mask", 3, createMaterialTextureProvider(3))
                .autoSampler2D(Scope.GLOBAL, "paradoxTexture", 4, ctx -> textures[0].getId())
                .addUBO(Scope.DRAW, "Material", 0, (ctx) -> ctx.object().getMaterial(ctx.mesh(), ctx.instance().variant()).bindMaterial())
                .autoImage2D(Scope.DRAW, "solidTex", 0, ctx -> textures[1])
                .autoImage2D(Scope.DRAW, "litTex", 1, ctx -> textures[2])
                .build();

        LIT = TraditionalPipeline.builder(builtin("rewrite/lit.vs.glsl"), builtin("rewrite/lit.fs.glsl"))
                .apply(builder -> GuiPipelines.common(builder, canvas))
                .autoSampler2D(Scope.GLOBAL, "tex", 0, ctx -> textures[2].getId())
                .build();

        SOLID = TraditionalPipeline.builder(builtin("rewrite/solid.vs.glsl"), builtin("rewrite/solid.fs.glsl"))
                .apply(builder -> GuiPipelines.common(builder, canvas))
                .autoVec3(Scope.GLOBAL, "teraTint", (ctx) -> settings.terastalization.tint.getValue())
                .autoSampler2D(Scope.GLOBAL, "tex", 0, ctx -> textures[1].getId())
                .addUniform(Scope.GLOBAL, "light", (uniform, ctx) -> {
                    var light = (int) (RareCandyCanvas.getLightLevel() * 15);

                    uniform.upload2i(0, light);
                })
                .autoSampler2D(Scope.GLOBAL, "lightmap", 1, (ctx) -> ITextureLoader.instance().getTexture("light_map").getId())

                .build();

        TERSTAL = TraditionalPipeline.builder(builtin("rewrite/terastal.vs.glsl"), builtin("rewrite/terastal.fs.glsl"))
                .apply(builder -> GuiPipelines.common(builder, canvas))
                .autoSampler2D(Scope.GLOBAL, "tex", 0, ctx -> textures[1].getId())
                .build();

        PLANE = TraditionalPipeline.builder(builtin("original/animated/plane.vs.glsl"), builtin("original/animated/plane.fs.glsl"))
                .autoMat4(Scope.GLOBAL, "viewMatrix", (ctx) -> RareCandyCanvas.viewMatrix)
                .autoMat4(Scope.GLOBAL, "projectionMatrix", (ctx) -> projectionMatrix)
                .autoFloat(Scope.GLOBAL, "lightLevel", (ctx) -> RareCandyCanvas.getLightLevel())
                .autoFloat(Scope.GLOBAL, "radius", (ctx) -> RareCandyCanvas.radius)
                .autoBool(Scope.GLOBAL, "render", (ctx) -> RareCandyCanvas.renderingFrame)
                .prePostDraw(material -> BlendType.Regular.enable(), material -> BlendType.Regular.disable())
                .build();

        TraditionalPipeline SCREEN_QUAD = TraditionalPipeline.builder(builtin("original/screen/screen_quad.vs.glsl"), builtin("original/screen/screen_quad.fs.glsl"))
                .autoSampler2D(Scope.GLOBAL, "screenTexture", 0, (ctx) -> RareCandyCanvas.framebuffer.getId())
                .build();
    }

    private static void common(TraditionalPipeline.Builder builder, RareCandyCanvas canvas) {
        builder.autoMat4(Scope.GLOBAL, "viewMatrix", (ctx) -> RareCandyCanvas.viewMatrix)
                .autoMat4(Scope.GLOBAL, "projectionMatrix", (ctx) -> projectionMatrix)
                .autoVec3(Scope.GLOBAL, "Light0_Direction", (ctx) -> light0)
                .autoVec3(Scope.GLOBAL, "Light1_Direction", (ctx) -> light1)
                .autoVec4(Scope.GLOBAL, "ColorModulator", (ctx) -> colorMOdulator)
                .autoVec3(Scope.GLOBAL, "tint", (ctx) -> ONE)
                .prePostDraw(material -> {

                    if(material.disableDepth()) {
                        GL11.glDisable(GL11.GL_DEPTH_TEST);
                    }

                    material.cullType().enable();
                    material.blendType().enable();
                }, material -> {
                    if(material.disableDepth()) {
                        GL11.glEnable(GL11.GL_DEPTH_TEST);
                    }

                    material.cullType().disable();
                    material.blendType().disable();
                })
                .addUBO(Scope.GLOBAL, "Fog", 0, (ctx) -> canvas.getFogUploader().id)
                .addUBO(Scope.INSTANCE, "Instance", 1, (ctx) -> InstanceBlockUploader.bind(ctx.instance()))
                .addSSBORange(Scope.MODEL, "VertexBuffer", 0, ctx -> TRANSFORM_BUFFER_ID, ctx -> TRANSFORM_BUFFER_OFFSET)
        ;
    }

    private static TextureIdSupplier createMaterialTextureProvider(int index) {
        return ctx -> {
            var variant = ctx.instance().variant();
            var imageIndex = ctx.object().getMaterial(ctx.mesh(), variant).images()[index];

            var name = ctx.object().images[imageIndex];

            return ITextureLoader.instance().getTexture(name).getId();
        };
    }
}