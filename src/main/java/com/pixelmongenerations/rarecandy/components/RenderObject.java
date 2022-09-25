package com.pixelmongenerations.rarecandy.components;

import com.pixelmongenerations.pkl.scene.material.Material;
import com.pixelmongenerations.rarecandy.core.VertexLayout;
import com.pixelmongenerations.rarecandy.rendering.InstanceState;
import com.pixelmongenerations.rarecandy.rendering.shader.ShaderProgram;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.*;

public abstract class RenderObject {
    protected ShaderProgram shaderProgram;
    protected List<Material> materials = new ArrayList<>();
    protected int indexCount;
    protected VertexLayout layout;
    protected Map<String, Material> variants;
    public boolean loaded;

    public abstract void render(Matrix4f projectionMatrix, List<InstanceState> instances);

    public void update() {}

    public Set<String> availableVariants() {
        return variants.keySet();
    }

    protected Material getMaterial(@Nullable String materialId) {
        if(!variants.containsKey(materialId)) return materials.get(0);
        else return variants.get(materialId);
    }
}

