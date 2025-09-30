package gg.generations.rarecandy.renderer.pipeline.neo.regular;

import gg.generations.rarecandy.renderer.pipeline.Uniform;

/**
 * Describes a UBO binding declaration.
 */
public record UBOBinding(
    String name,
    int bindingPoint,
    BufferSupplier bufferSupplier
) {}

