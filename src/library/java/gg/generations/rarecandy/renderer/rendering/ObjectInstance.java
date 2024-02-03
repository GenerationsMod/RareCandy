package gg.generations.rarecandy.renderer.rendering;

import gg.generations.rarecandy.renderer.components.RenderObject;
import org.joml.Matrix4f;

public class ObjectInstance {
    private final Matrix4f transformationMatrix;
    private final Matrix4f viewMatrix;
    private String variant;
    private RenderObject object;

    public ObjectInstance(Matrix4f transformationMatrix, Matrix4f viewMatrix, String variant) {
        this.transformationMatrix = transformationMatrix;
        this.viewMatrix = viewMatrix;
        this.variant = variant;
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
        return variant;
    }

    public RenderObject object() {
        return object;
    }

    public String variant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public void render() {
        this.object.render(this);
    }
}
