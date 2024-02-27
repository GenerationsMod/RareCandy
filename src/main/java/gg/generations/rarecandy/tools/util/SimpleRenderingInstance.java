package gg.generations.rarecandy.tools.util;

import gg.generations.rarecandy.arceus.model.Material;
import gg.generations.rarecandy.arceus.model.Model;
import gg.generations.rarecandy.arceus.model.RenderingInstance;
import org.joml.Matrix4f;

public class SimpleRenderingInstance implements RenderingInstance {

    private Model model;
    private final Material material;
    public final Matrix4f transform = new Matrix4f();

    public SimpleRenderingInstance(Model model, Material material) {
        this.model = model;
        this.material = material;
    }

    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public Material getMaterial() {
        return material;
    }

    @Override
    public Matrix4f getTransform() {
        return transform;
    }
}
