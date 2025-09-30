package gg.generations.rarecandy.renderer.pipeline.util;

/**
 * Functional interface for uniform upload callbacks.
 * The callback receives the resolved Uniform object and the upload context.
 */
@FunctionalInterface
public interface UniformCallback {
    void apply(Uniform uniform, UniformUploadContext ctx);
}
