package gg.generations.rarecandy.renderer.pipeline.util;

import gg.generations.rarecandy.renderer.components.MultiRenderObject;
import gg.generations.rarecandy.renderer.textures.ITexture;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;

public interface TextureSupplier {
    ITexture getTexture(UniformUploadContext ctx);
}
