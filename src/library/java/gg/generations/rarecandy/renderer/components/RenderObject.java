package gg.generations.rarecandy.renderer.components;

import gg.generations.rarecandy.renderer.animation.AnimationController;
import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import gg.generations.rarecandy.renderer.rendering.RenderStage;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class RenderObject implements Closeable {
    protected Map<String, Material> variants = new HashMap<>();
    protected String defaultVariant = null;

    protected boolean ready = false;
    protected Matrix4f matrixOffset = new Matrix4f().identity();
    protected List<String> shouldRenderList;
    protected Map<String, Vector2f> offsets;

    public void render(RenderStage stage, List<ObjectInstance> instances) {
        render(stage, instances, this);
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
        var variant = materialId != null && variants.containsKey(materialId) ? materialId : defaultVariant;
        return variants.get(variant);
    }

    public Vector2f getOffsets(@Nullable String variant) {
        return offsets != null ? offsets.getOrDefault(variant, AnimationController.NO_UV_OFFSET) : AnimationController.NO_UV_OFFSET;
    }

    protected abstract <T extends RenderObject> void render(RenderStage stage, List<ObjectInstance> instances, T object);

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

