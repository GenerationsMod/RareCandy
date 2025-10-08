package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.renderer.pipeline.Pipelines;
import gg.generations.rarecandy.renderer.pipeline.compute.ComputePipeline;
import gg.generations.rarecandy.renderer.pipeline.util.*;
import gg.generations.rarecandy.renderer.textures.BlankTexture;
import gg.generations.rarecandy.renderer.textures.ITexture;
import gg.generations.rarecandy.renderer.textures.TextureArray;

import java.util.function.IntSupplier;

public class CriticalPipelines {
    private static ITexture[] textures = new ITexture[2];
    private static TextureArray textureArray = null;
    private static int layer = -1;

    public static ComputePipeline[] colors = new ComputePipeline[3];
    public static ComputePipeline[] effects = new ComputePipeline[7];

    public static void initalize() {
        textures[0] = new BlankTexture(ITexture.Type.RGBA_BYTE, 1024, 1024, ITexture.ComputeAccess.READ_WRITE);
        textures[1] = new BlankTexture(ITexture.Type.RGBA_BYTE, 1024, 1024, ITexture.ComputeAccess.READ_WRITE);

        colors[0] = Pipelines.compute(Pipelines.builtin(""))
                .autoSampler2DArray(Scope.DRAW, "soldidTex", 0, ctx -> ctx.object().images)
                .autoImage2D(Scope.DRAW, "solidTex", 0, ctx -> textures[0])
                .addUBO(Scope.DRAW, "Material", 0, ctx -> ctx.object().getMaterial(ctx.mesh(), ctx.instance().variant()).bindMaterial())
                .build();
    }
}
