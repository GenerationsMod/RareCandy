package com.pixelmongenerations.rarecandy.components;

import com.pixelmongenerations.pixelmonassetutils.scene.material.Material;
import com.pixelmongenerations.rarecandy.core.VertexLayout;
import com.pixelmongenerations.rarecandy.rendering.InstanceState;
import com.pixelmongenerations.rarecandy.rendering.shader.ShaderProgram;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class RenderObject {
    protected ShaderProgram shaderProgram;
    protected List<Material> material = new ArrayList<>();
    protected int indexCount;
    protected VertexLayout layout;
    protected Map<String, Material> variants;

    public abstract void render(Matrix4f projectionMatrix, List<InstanceState> instances);

    public void update() {
    }

    public int getVariantCount() {
        return material.size();
    }
}

