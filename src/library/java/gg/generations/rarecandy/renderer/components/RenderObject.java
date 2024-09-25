package gg.generations.rarecandy.renderer.components;

import gg.generations.rarecandy.renderer.animation.AnimationController;
import gg.generations.rarecandy.renderer.animation.Transform;
import gg.generations.rarecandy.renderer.model.Variant;
import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class RenderObject implements Closeable {
    protected Map<String, Variant> variants = new HashMap<>();
    protected String defaultVariant = null;

    protected boolean ready = false;
    protected Matrix4f matrixOffset = new Matrix4f().identity();

    public void render(List<ObjectInstance> instances) {
        render(instances, this);
    }

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
        var variant = materialId != null && variants.containsKey(materialId) ? materialId : defaultVariant;
        return getVariant(variant).material();
    }

    public Transform getTransform(@Nullable String variantId) {
        var variant = getVariant(variantId);

        return variant != null && variant.offset() != null ? variant.offset() : AnimationController.NO_OFFSET;
    }

    protected abstract <T extends RenderObject> void render(List<ObjectInstance> instances, T object);

    protected abstract <T extends RenderObject> void render(ObjectInstance instance, T object);

    protected boolean shouldRender(ObjectInstance instance) {
        var variant = getVariant(instance);
        return variant == null || variant.hide();
    }

    protected Variant getVariant(ObjectInstance instance) {
        var id = instance.variant() == null ? defaultVariant : instance.variant();

        return variants.getOrDefault(id, null);
    }

    protected Variant getVariant(String variant) {
        return variants.getOrDefault(variant, null);
    }

    @Override
    public void close() throws IOException {
        for (var a : variants.values()) {
            if(a != null) {
                a.material().close();
            }
        }
    }
}

