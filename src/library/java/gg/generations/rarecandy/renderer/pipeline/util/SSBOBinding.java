package gg.generations.rarecandy.renderer.pipeline.util;

/**
 * Describes an SSBO binding declaration.
 * Supports both base and range binding.
 */
public record SSBOBinding(
    String name,
    int bindingPoint,
    BufferSupplier bufferSupplier,
    RangeSupplier offsetSupplier,
    RangeSupplier sizeSupplier
) {}
