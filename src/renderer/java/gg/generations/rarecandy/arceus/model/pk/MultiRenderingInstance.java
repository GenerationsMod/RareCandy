package gg.generations.rarecandy.arceus.model.pk;

import gg.generations.rarecandy.arceus.model.Model;
import gg.generations.rarecandy.arceus.model.RenderingInstance;
import org.joml.Matrix4f;

import java.util.function.Supplier;

public final class MultiRenderingInstance<T extends MultiRenderObjectInstance<?>> implements RenderingInstance {
    private final String name;
    private final Model model;
    private final T object;
    private final Supplier<PkMaterial> materialSupplier;
    private PkMaterial material;
    private final Matrix4f transform;
    private boolean changing = false;

    public MultiRenderingInstance(String name, Model model, T object, Supplier<PkMaterial> materialSupplier, Matrix4f transform) {
        this.name = name;
        this.model = model;
        this.object = object;
        this.materialSupplier = materialSupplier;
        this.material = materialSupplier.get();
        this.transform = transform;
    }

    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public PkMaterial getMaterial() {
        return material;
    }


    @Override
    public Matrix4f getTransform() {
        return transform;
    }

    public Model model() {
        return model;
    }

    @Override
    public void postRemove() {
        material = materialSupplier.get();
        changing = false;
    }

    public void setChanging() {
        this.changing = true;
    }

    public boolean isChanging() {
        return changing;
    }

    public String getName() {
        return name;
    }

    public T object() {
        return object;
    }
}
