package com.pokemod.rarecandy.rendering;

import com.pokemod.rarecandy.components.RenderObject;
import org.joml.Matrix4f;

public class ObjectInstance {
    private final Matrix4f transformationMatrix;
    private final Matrix4f viewMatrix;
    private final String materialId;
    private final int lightColor;
    private RenderObject object;

    public ObjectInstance(Matrix4f transformationMatrix, Matrix4f viewMatrix, String materialId, int lightColor) {
        this.transformationMatrix = transformationMatrix;
        this.viewMatrix = viewMatrix;
        this.materialId = materialId;
        this.lightColor = lightColor;
    }

    public ObjectInstance(Matrix4f transformationMatrix, Matrix4f viewMatrix, String materialId) {
        this(transformationMatrix, viewMatrix, materialId, 0xffffff);
    }

    public void link(RenderObject object) {
        this.object = object;
    }

    public Matrix4f transformationMatrix() {
        return transformationMatrix;
    }

    public Matrix4f viewMatrix() {
        return viewMatrix;
    }

    public String materialId() {
        return materialId;
    }

    public int lightColor() {
        return lightColor;
    }

    public RenderObject object() {
        return object;
    }
}
