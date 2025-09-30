package gg.generations.rarecandy.renderer.pipeline.util;

/**
 * Describes a uniform binding declaration.
 * Holds a direct reference to the resolved Uniform object and a callback.
 */
public record UniformBinding(
    Uniform uniform,
    UniformCallback callback
) {}
