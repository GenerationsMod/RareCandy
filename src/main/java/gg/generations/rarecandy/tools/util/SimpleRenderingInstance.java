package gg.generations.rarecandy.tools.util;

import gg.generations.rarecandy.arceus.model.Material;
import gg.generations.rarecandy.arceus.model.Model;
import gg.generations.rarecandy.arceus.model.RenderingInstance;
import gg.generations.rarecandy.arceus.model.lowlevel.RenderData;
import gg.generations.rarecandy.tools.gui.MultiRenderObject;
import org.joml.Matrix4f;

public class SimpleRenderingInstance implements RenderingInstance {

    private Model model;
    public final Matrix4f transform = new Matrix4f();

    public SimpleRenderingInstance(MultiRenderObject<?> multiRenderObject, Model model) {
        this.model = model;
    }

    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public Material getMaterial() {
        return null;
    }

    @Override
    public Matrix4f getTransform() {
        return transform;
    }
}
