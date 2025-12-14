package gg.generations.rarecandy.renderer.pipeline;

import gg.generations.rarecandy.renderer.components.RenderObject;
import gg.generations.rarecandy.renderer.loading.ITexture;
import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.model.material.MaterialValues;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import org.joml.Vector3f;

import java.util.Objects;

public final class UniformUploadContext {
    public static UniformUploadContext INSTANCE = new UniformUploadContext(null, null, null);

    private RenderObject object;
    private ObjectInstance instance;
    private Uniform uniform;

    private UniformUploadContext(RenderObject object, ObjectInstance instance, Uniform uniform) {
        this.object = object;
        this.instance = instance;
        this.uniform = uniform;
    }

    public Material getMaterial() {
        return object().getMaterial(instance.variant());
    }

    public RenderObject object() {
        return object;
    }

    public ObjectInstance instance() {
        return instance;
    }

    public Uniform uniform() {
        return uniform;
    }

    public UniformUploadContext with(Uniform uniform) {
        this.uniform = uniform;
        return this;
    }

    public UniformUploadContext with(RenderObject renderObject, ObjectInstance instance) {
        this.object = renderObject;
        this.instance = instance;
        return this;
    }
}
