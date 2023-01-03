package com.pokemod.rarecandy.rendering;

import com.pokemod.rarecandy.components.RenderObject;
import org.joml.Matrix4f;

public class ObjectInstance {
    private final Matrix4f transformationMatrix;
    private final Matrix4f viewMatrix;
    private final String materialId;
    private RenderObject object;

    public ObjectInstance(Matrix4f transformationMatrix, Matrix4f viewMatrix, String materialId) {
        this.transformationMatrix = transformationMatrix;
        this.viewMatrix = viewMatrix;
        this.materialId = materialId;
    }

    public void link(RenderObject object) {
        this.object = object;
        object.applyTransformOffset(transformationMatrix);
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

    public RenderObject object() {
        return object;
    }
}
