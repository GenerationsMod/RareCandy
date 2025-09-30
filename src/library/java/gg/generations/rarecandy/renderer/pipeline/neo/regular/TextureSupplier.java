package gg.generations.rarecandy.renderer.pipeline.neo.regular;

import gg.generations.rarecandy.renderer.components.RenderObject;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;

/**
 * Supplies a texture object ID dynamically at pipeline bind time.
 */
@FunctionalInterface
public interface TextureSupplier {
    int get(ObjectInstance instance, RenderObject object);
}
