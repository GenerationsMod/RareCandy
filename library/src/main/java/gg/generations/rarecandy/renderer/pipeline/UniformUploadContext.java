package gg.generations.rarecandy.renderer.pipeline;

import gg.generations.rarecandy.renderer.components.RenderObject;
import gg.generations.rarecandy.renderer.loading.ITexture;
import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;

public record UniformUploadContext(RenderObject object, ObjectInstance instance, Uniform uniform) {
    public Object getValue(String name) {
        return getMaterial().getValue(name);
    }

    public Material getMaterial() {
        return object().getMaterial(instance.variant());
    }

    public ITexture getTexture(String name) {
        return getMaterial().getTexture(name);
    }
}
