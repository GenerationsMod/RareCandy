package gg.generations.rarecandy.renderer.pipeline;

import gg.generations.rarecandy.renderer.components.RenderObject;
import gg.generations.rarecandy.renderer.loading.ITexture;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;

public record UniformUploadContext(RenderObject object, ObjectInstance instance, Uniform uniform) {
    public Object getValue(String name) {
        return object().getMaterial(instance.variant()).getValue(name);
    }

    public ITexture getTexture(String name) {
        return object().getMaterial(instance.variant()).getTexture(name);
    }
}
