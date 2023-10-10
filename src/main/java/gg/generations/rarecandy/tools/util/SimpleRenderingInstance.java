package gg.generations.rarecandy.tools.util;

import gg.generations.rarecandy.arceus.model.Model;
import gg.generations.rarecandy.arceus.model.RenderingInstance;
import org.joml.Matrix4f;

public class SimpleRenderingInstance implements RenderingInstance {

    private Model model;
    public final Matrix4f transform = new Matrix4f();

    public SimpleRenderingInstance(Model model) {
        this.model = model;
    }

    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public Matrix4f getTransform() {
        return transform;
    }
}
