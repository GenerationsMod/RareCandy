package gg.generations.rarecandy.renderer.pipeline.util;

import gg.generations.rarecandy.renderer.components.RenderObject;
import gg.generations.rarecandy.renderer.loading.ITexture;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;

public interface TextureSupplier {
    ITexture getTexture(ObjectInstance instance, RenderObject object);
}
