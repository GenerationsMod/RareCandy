package gg.generations.rarecandy.renderer.pipeline.util;

import gg.generations.rarecandy.renderer.components.MultiRenderObject;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;

/**
 * Supplies a buffer object ID dynamically at pipeline bind time.
 */
@FunctionalInterface
public interface BufferSupplier {
    int get(UniformUploadContext ctx);
}

