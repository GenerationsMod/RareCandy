package com.pixelmongenerations.rarecandy.rendering;

import org.joml.Matrix4f;

@SuppressWarnings("ClassCanBeRecord")
public class InstanceState {

    public final Matrix4f transformationMatrix;
    public final Matrix4f modelViewMatrix;

    public InstanceState(Matrix4f transformationMatrix, Matrix4f modelViewMatrix) {
        this.transformationMatrix = transformationMatrix;
        this.modelViewMatrix = modelViewMatrix;
    }
}
