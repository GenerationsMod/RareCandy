package com.pixelmongenerations.rarecandy.rendering;

import org.joml.Matrix4f;

import java.util.Objects;

public final class InstanceState {
    private final Matrix4f transformationMatrix;
    private final Matrix4f viewMatrix;
    private final String materialId;

    public InstanceState(Matrix4f transformationMatrix, Matrix4f viewMatrix, String materialId) {
        this.transformationMatrix = transformationMatrix;
        this.viewMatrix = viewMatrix;
        this.materialId = materialId;
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
}
