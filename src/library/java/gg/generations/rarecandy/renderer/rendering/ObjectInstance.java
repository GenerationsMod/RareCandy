package gg.generations.rarecandy.renderer.rendering;

import gg.generations.rarecandy.renderer.components.RenderObject;
import org.joml.Matrix4f;

public class ObjectInstance {
    private final Matrix4f transformationMatrix;
    private String variant;
    private RenderObject object;
    private boolean used = false;
    private double secondsPassed = 0f;
    private static final float DELINK_THRESHOLD =  1.0f; // Seconds before delinking

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

    public boolean isLinked() {
        return object != null;
    }

    public void use() {
        used = true;
    }

    protected void delink() {
        this.object = null;
    }

    public void update(double absoluteTime) {
        if(object != null) {
            if(used) {
                secondsPassed = absoluteTime; // Store current time as "last used"
                used = false;
            } else {
                if(absoluteTime - secondsPassed >= DELINK_THRESHOLD) {
                    delink();
                }
            }
        }
    }
}