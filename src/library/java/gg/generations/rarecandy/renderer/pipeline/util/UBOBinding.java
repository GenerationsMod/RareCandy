package gg.generations.rarecandy.renderer.pipeline.util;

/**
 * Describes a UBO binding declaration.
 */
public record UBOBinding(
    String name,
    int bindingPoint,
    BufferSupplier bufferSupplier
) {}

