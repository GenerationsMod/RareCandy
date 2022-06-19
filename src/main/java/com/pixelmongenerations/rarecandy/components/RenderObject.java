package com.pixelmongenerations.rarecandy.components;

import com.pixelmongenerations.pixelmonassetutils.scene.material.Material;
import com.pixelmongenerations.pixelmonassetutils.scene.material.Texture;
import com.pixelmongenerations.rarecandy.core.VertexLayout;
import com.pixelmongenerations.rarecandy.rendering.InstanceState;
import com.pixelmongenerations.rarecandy.rendering.shader.ShaderProgram;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL30C;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;

public abstract class RenderObject {
    protected ShaderProgram shaderProgram;
    protected Material material;
    protected int indexCount;
    protected VertexLayout layout;

    public void render(Matrix4f projectionMatrix, List<InstanceState> instances) {
    }

    public void update() {
    }
}

