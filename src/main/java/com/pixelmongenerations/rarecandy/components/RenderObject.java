package com.pixelmongenerations.rarecandy.components;

import org.joml.Matrix4f;

public class RenderObject {

    private Matrix4f transformationMatrix = new Matrix4f();

    public void render(Matrix4f projectionMatrix, Matrix4f viewMatrix) {
    }

    public void update() {
    }

    public Matrix4f getTransformationMatrix() {
        return transformationMatrix;
    }

    public void setTransformationMatrix(Matrix4f transformationMatrix) {
        this.transformationMatrix = transformationMatrix;
    }
}

