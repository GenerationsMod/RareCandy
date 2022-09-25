package com.pixelmongenerations.rarecandy.components;

import com.pixelmongenerations.pkl.scene.material.Material;
import com.pixelmongenerations.rarecandy.pipeline.Pipeline;
import com.pixelmongenerations.rarecandy.rendering.InstanceState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class RenderObject {
    protected Pipeline pipeline;
    protected List<Material> materials = new ArrayList<>();
    protected int indexCount;
    protected Map<String, Material> variants;

    public abstract void render(List<InstanceState> instances);

    public void update() {}

    public Set<String> availableVariants() {
        return variants.keySet();
    }

    public Material getMaterial(@Nullable String materialId) {
        if (!variants.containsKey(materialId)) return materials.get(0);
        else return variants.get(materialId);
    }
}

