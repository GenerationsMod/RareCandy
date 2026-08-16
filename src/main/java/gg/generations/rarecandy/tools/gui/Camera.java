package gg.generations.rarecandy.tools.gui;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.function.Consumer;

public class Camera {
    private final Matrix4f projectionMatrix = new Matrix4f();
    private final Matrix4f viewMatrix = new Matrix4f();
    private final Matrix4f inverseProjectionMatrix = new Matrix4f();
    private final Matrix4f inverseViewMatrix = new Matrix4f();
    private final Vector3f position = new Vector3f();

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public void setProjectionMatrix(Consumer<Matrix4f> consumer) {
        projectionMatrix.identity();
        consumer.accept(projectionMatrix);
        projectionMatrix.invert(inverseProjectionMatrix);
    }
    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    public void setViewMatrix(Consumer<Matrix4f> consumer) {
        consumer.accept(viewMatrix);
        viewMatrix.invert(inverseViewMatrix);

        position.set(inverseViewMatrix.m30(), inverseViewMatrix.m31(), inverseViewMatrix.m32());
    }
    public Matrix4f getInverseProjectionMatrix() {
        return inverseProjectionMatrix;
    }

    public Matrix4f getInverseViewMatrix() {
        return inverseViewMatrix;
    }

    public Vector3f getPosition() {
        return position;
    }
}
