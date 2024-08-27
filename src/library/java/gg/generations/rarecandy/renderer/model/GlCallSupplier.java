package gg.generations.rarecandy.renderer.model;

import gg.generations.rarecandy.pokeutils.ModelConfig;
import gg.generations.rarecandy.pokeutils.PixelAsset;
import gg.generations.rarecandy.renderer.components.MultiRenderObject;
import gg.generations.rarecandy.renderer.components.RenderObject;
import gg.generations.rarecandy.renderer.loading.AnimResource;

import java.util.List;
import java.util.Map;

@FunctionalInterface
public interface GlCallSupplier<T extends RenderObject, V extends MultiRenderObject<T>> {

    List<Runnable> getCalls(PixelAsset model, Map<String, AnimResource> animResources, Map<String, String> imageFiles, ModelConfig config, V mro);
}
