package com.pixelmongenerations.rarecandy.components;

import com.pixelmongenerations.pixelmonassetutils.scene.material.Material;
import com.pixelmongenerations.rarecandy.core.VertexLayout;
import com.pixelmongenerations.rarecandy.rendering.InstanceState;
import com.pixelmongenerations.rarecandy.rendering.shader.ShaderProgram;
import org.joml.Matrix4f;

import java.util.List;

public abstract class RenderObject {
    protected ShaderProgram shaderProgram;
    protected Material material;
    protected int indexCount;
    protected VertexLayout layout;

    public abstract void render(Matrix4f projectionMatrix, List<InstanceState> instances);

    public void update() {
    }
}

