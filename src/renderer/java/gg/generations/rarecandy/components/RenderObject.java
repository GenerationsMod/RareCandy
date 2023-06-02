package gg.generations.rarecandy.components;

import gg.generations.rarecandy.model.Material;
import gg.generations.rarecandy.model.Variant;
import gg.generations.rarecandy.pipeline.Pipeline;
import gg.generations.rarecandy.rendering.ObjectInstance;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.*;
import java.util.function.Function;

public abstract class RenderObject {
    protected Function<String, Pipeline> pipeline;
    protected Map<String, Variant> variants = new HashMap<>();

    protected Variant defaultVariant;
    protected boolean ready = false;
    protected Matrix4f matrixOffset = new Matrix4f().identity();

    public void render(List<ObjectInstance> instances) {
        render(instances, this);
    }

    public void update() {}

    public boolean isReady() {
        return ready;
    }

    public void setMatrixOffset(Matrix4f mat4f) {
        matrixOffset.set(mat4f);
    }

    public void applyTransformOffset(Matrix4f currentTransform) {
        currentTransform.mul(matrixOffset);
    }

    public Set<String> availableVariants() {
        return variants.keySet();
    }

    public Material getMaterial(@Nullable String materialId) {
        return getVariant(materialId).material();
    }

    public Variant getVariant(@Nullable String materialId) {
        return variants.getOrDefault(materialId, defaultVariant);
    }

    protected abstract <T extends RenderObject> void render(List<ObjectInstance> instances, T object);
}

