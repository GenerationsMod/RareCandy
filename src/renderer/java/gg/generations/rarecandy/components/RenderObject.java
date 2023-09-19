package gg.generations.rarecandy.components;

import gg.generations.rarecandy.model.material.Material;
import gg.generations.rarecandy.pipeline.Pipeline;
import gg.generations.rarecandy.rendering.ObjectInstance;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public abstract class RenderObject {
    protected Function<String, Pipeline> pipeline;
    protected Map<String, Material> variants = new HashMap<>();
    protected String defaultVariant = null;

    protected boolean ready = false;
    protected Matrix4f matrixOffset = new Matrix4f().identity();
    protected List<String> shouldRenderList;

    public void render(List<ObjectInstance> instances) {
        render(instances, this);
    }

    public void update() {
    }

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
        return getVariant(materialId);
    }

    public Material getVariant(@Nullable String materialId) {
        var variant = variants.containsKey(materialId) ? materialId : defaultVariant;
        return variants.get(variant);
    }

    protected abstract <T extends RenderObject> void render(List<ObjectInstance> instances, T object);

    protected boolean shouldRender(String variant) {
        return shouldRenderList != null && shouldRenderList.contains(variant); //TODO: check if correct.
    }
}

