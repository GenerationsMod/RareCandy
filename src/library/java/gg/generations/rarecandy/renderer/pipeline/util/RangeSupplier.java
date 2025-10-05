package gg.generations.rarecandy.renderer.pipeline.util;

import gg.generations.rarecandy.renderer.components.MultiRenderObject;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;

/**
 * Supplies byte offsets and sizes for SSBO range bindings dynamically at pipeline bind time.
 */
@FunctionalInterface
public interface RangeSupplier {
    long get(UniformUploadContext ctx);
}
