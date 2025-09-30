package gg.generations.rarecandy.renderer.pipeline.neo.regular;

import gg.generations.rarecandy.renderer.components.RenderObject;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;

/**
 * Supplies a buffer object ID dynamically at pipeline bind time.
 */
@FunctionalInterface
public interface BufferSupplier {
    int get(ObjectInstance instance, RenderObject object);
}

