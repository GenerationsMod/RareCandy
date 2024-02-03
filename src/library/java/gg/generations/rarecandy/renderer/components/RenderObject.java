package gg.generations.rarecandy.renderer.components;

import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;

public abstract class RenderObject implements Closeable {
    protected Map<String, Material> variants = new HashMap<>();

    protected List<String> transparentList = new ArrayList<>();
    protected String defaultVariant = null;

    protected boolean ready = false;
    protected Matrix4f matrixOffset = new Matrix4f().identity();
    protected List<String> shouldRenderList;

    public void render(ObjectInstance instance) {
        render(instance, this);
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

    protected abstract <T extends RenderObject> void render(ObjectInstance instance, T object);

    protected boolean shouldRender(ObjectInstance instance) {
        return shouldRenderList != null && shouldRenderList.contains(instance.variant()); //TODO: check if correct.
    }

    @Override
    public void close() throws IOException {
        for (Material a : variants.values()) {
            if(a != null) {
                a.close();
            }
        }
    }
}

