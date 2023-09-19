package gg.generations.rarecandy.tools.pixelmonTester;

import gg.generations.rarecandy.arceus.model.Model;
import gg.generations.rarecandy.arceus.model.RenderingInstance;
import gg.generations.rarecandy.legacy.model.misc.Material;
import org.joml.Matrix4f;

public class BasicInstance implements RenderingInstance {
    private final Model model;
    private final Matrix4f tranform = new Matrix4f();

    public BasicInstance(Model model) {
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
        return tranform;
    }

    @Override
    public boolean visible() {
        return true;
    }
}
