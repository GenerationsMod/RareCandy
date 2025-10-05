package gg.generations.rarecandy.renderer.pipeline.util;

/**
 * Supplies a texture object ID dynamically at pipeline bind time.
 */
@FunctionalInterface
public interface TextureIdSupplier {
    int get(UniformUploadContext ctx);
}
