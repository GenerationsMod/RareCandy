package gg.generations.rarecandy.renderer.pipeline;

import gg.generations.rarecandy.renderer.components.RenderObject;
import gg.generations.rarecandy.renderer.loading.ITexture;
import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.model.material.MaterialValues;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import org.joml.Vector3f;

public record UniformUploadContext(RenderObject object, ObjectInstance instance, Uniform uniform) {

    public Material getMaterial() {
        return object().getMaterial(instance.variant());
    }
}
