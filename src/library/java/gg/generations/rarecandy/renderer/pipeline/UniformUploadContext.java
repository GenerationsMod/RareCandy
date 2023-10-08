package gg.generations.rarecandy.renderer.pipeline;

import gg.generations.rarecandy.renderer.components.RenderObject;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;

public record UniformUploadContext(RenderObject object, ObjectInstance instance, Uniform uniform) {
}
