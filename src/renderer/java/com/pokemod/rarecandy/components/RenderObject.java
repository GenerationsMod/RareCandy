package com.pokemod.rarecandy.components;

import com.pokemod.rarecandy.model.Material;
import com.pokemod.rarecandy.pipeline.Pipeline;
import com.pokemod.rarecandy.rendering.ObjectInstance;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class RenderObject {
    protected Pipeline pipeline;
    protected List<Material> materials = new ArrayList<>();
    protected Map<String, Material> variants;
    protected boolean ready = false;
    protected Matrix4f matrixOffset = new Matrix4f().identity();

    public abstract void render(List<ObjectInstance> instances);

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
        return variants.getOrDefault(materialId, materials.get(0));
    }
}

