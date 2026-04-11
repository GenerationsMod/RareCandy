package gg.generations.rarecandy.renderer.pipeline.util;

import gg.generations.rarecandy.renderer.components.MultiRenderObject;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import gg.generations.rarecandy.renderer.rendering.RenderStage;

/**
 * Context object passed to uniform upload callbacks.
 * Provides access to the current GL program, instance, and render object.
 */
public record UniformUploadContext(ObjectInstance instance, MultiRenderObject object, int mesh, RenderStage stage) {
}
