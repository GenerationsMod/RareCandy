package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.renderer.pipeline.Pipelines;
import gg.generations.rarecandy.renderer.pipeline.compute.ComputePipeline;
import gg.generations.rarecandy.renderer.pipeline.util.Scope;
import gg.generations.rarecandy.renderer.pipeline.util.TextureSupplier;
import gg.generations.rarecandy.renderer.pipeline.util.UniformUploadContext;
import gg.generations.rarecandy.renderer.textures.ITexture;
import gg.generations.rarecandy.renderer.textures.TextureArray;

import java.util.function.IntSupplier;

public class CriticalPipelines {
    public static ComputePipeline SCALE;
    private static ITexture[] textures = new ITexture[2];
    private static TextureArray textureArray = null;
    private static int layer = -1;

    public static void initalize() {
        SCALE = Pipelines.compute(Pipelines.builtin("experimental/scale.cs.glsl"))
                .autoImage2DArray(Scope.GLOBAL, "output", 0, () -> layer, ITexture.ComputeAccess.WRITE_ONLY, ctx -> textureArray)
//                .autoSampler2D()
                .build();
    }
}
