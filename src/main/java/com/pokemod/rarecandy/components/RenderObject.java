package com.pokemod.rarecandy.components;

import com.pokemod.rarecandy.model.Material;
import com.pokemod.rarecandy.pipeline.Pipeline;
import com.pokemod.rarecandy.rendering.InstanceState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class RenderObject {
    protected Pipeline pipeline;
    protected List<Material> materials = new ArrayList<>();
    protected Map<String, Material> variants;
    protected boolean ready = false;

    public abstract void render(List<InstanceState> instances);

    public void update() {}

    public boolean isReady() {
        return ready;
    }

    public void applyRootTransformation(InstanceState state) {

    }

    public Set<String> availableVariants() {
        return variants.keySet();
    }

    public Material getMaterial(@Nullable String materialId) {
        return variants.getOrDefault(materialId, materials.get(0));
    }
}

