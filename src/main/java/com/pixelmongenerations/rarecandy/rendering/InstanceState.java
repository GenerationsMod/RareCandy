package com.pixelmongenerations.rarecandy.rendering;

import org.joml.Matrix4f;

@SuppressWarnings("ClassCanBeRecord")
public class InstanceState {

    public final Matrix4f transformationMatrix;
    public final Matrix4f modelMatrix;
    public final String materialId;

    public InstanceState(Matrix4f transformationMatrix, Matrix4f modelViewMatrix, String materialId) {
        this.transformationMatrix = transformationMatrix;
        this.modelMatrix = modelViewMatrix;
        this.materialId = materialId;
    }
}
