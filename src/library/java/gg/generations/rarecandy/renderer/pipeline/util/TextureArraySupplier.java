package gg.generations.rarecandy.renderer.pipeline.util;

import gg.generations.rarecandy.renderer.textures.ITexture;
import gg.generations.rarecandy.renderer.textures.TextureArray;

public interface TextureArraySupplier {
    TextureArray getTextureArray(UniformUploadContext ctx);
}
