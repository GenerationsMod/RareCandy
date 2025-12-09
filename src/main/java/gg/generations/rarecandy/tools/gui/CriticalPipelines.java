package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.pokeutils.reader.ITextureLoader;
import gg.generations.rarecandy.renderer.pipeline.Pipelines;
import gg.generations.rarecandy.renderer.pipeline.compute.ComputePipeline;
import gg.generations.rarecandy.renderer.pipeline.util.*;
import gg.generations.rarecandy.renderer.textures.BlankTexture;
import gg.generations.rarecandy.renderer.textures.ITexture;
import gg.generations.rarecandy.renderer.textures.TextureArray;

import java.util.function.Consumer;
import java.util.function.IntSupplier;

import static gg.generations.rarecandy.tools.gui.GuiPipelines.pingpong;

public class CriticalPipelines {
    private static ITexture[] textures = new ITexture[2];
    private static TextureArray textureArray = null;
    private static int layer = -1;

    public static ComputePipeline[] colors = new ComputePipeline[3];
    public static ComputePipeline[] effects = new ComputePipeline[7];
    private static ComputePipeline emissive;

    public static void init() {
        textures[0] = new BlankTexture(ITexture.Type.RGBA_BYTE, 1024, 1024, ITexture.ComputeAccess.READ_WRITE);
        textures[1] = new BlankTexture(ITexture.Type.RGBA_BYTE, 1024, 1024, ITexture.ComputeAccess.READ_WRITE);

        colors[0] = color("layered").build();
        colors[1] = color("masked").build();
        colors[2] = color("solid").build();

        emissive = ComputePipeline.builder(material("", "emissive")).apply(CriticalPipelines::addCommon).build();
        effects[0] = effect("cartoon").build();
        effects[1] = effect("galaxy").build();
        effects[2] = effect("paradox")
                .autoInt(Scope.GLOBAL, "frame", (ctx) -> (int) pingpong(RareCandyCanvas.getTime() % 1d))
                .autoSampler2D(Scope.GLOBAL, "paradoxMask", 2, (ctx) -> ITextureLoader.instance().getTexture("paradox_mask").getId()).build();
        effects[3] = effect("pastel").build();
        effects[4] = effect("shadow").build();
        effects[5] = effect("sketch").build();
        effects[6] = effect("vintage").build();
    }

    private static String material(String group, String name) {
        return shader("material/" + group, name);
    }

    private static String shader(String group, String name) {
        return Pipelines.builtin("rewrite/" + group + "/" + name + ".cs.glsl");
    }


    private static ComputePipeline.Builder effect(String name) {
        return ComputePipeline.builder(material("effect", name)).apply(CriticalPipelines::addCommon);
    }

    private static ComputePipeline.Builder color(String name) {
        return ComputePipeline.builder(material("color", name)).apply(CriticalPipelines::addCommon);
    }

    private static void addCommon(ComputePipeline.Builder builder) {
        builder.autoImage2D(Scope.DRAW, "solidTex", 0, ctx -> textures[1]);
        builder.addUBO(Scope.DRAW, "material", 0, ctx -> ctx.object().getMaterial(ctx.mesh(), ctx.instance().variant()).bindMaterial());
    }


}
