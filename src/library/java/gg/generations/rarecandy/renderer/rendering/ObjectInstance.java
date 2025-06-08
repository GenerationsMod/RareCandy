package gg.generations.rarecandy.renderer.rendering;

import gg.generations.rarecandy.renderer.components.RenderObject;
import org.joml.Matrix4f;

public class ObjectInstance {
    private final Matrix4f transformationMatrix;
    private String variant;
    private RenderObject object;

    public ObjectInstance(Matrix4f transformationMatrix, String variant) {
        this.transformationMatrix = transformationMatrix;
        this.variant = variant;
    }

    public void link(RenderObject object) {
        this.object = object;
    }

    public Matrix4f transformationMatrix() {
        return transformationMatrix;
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
}
