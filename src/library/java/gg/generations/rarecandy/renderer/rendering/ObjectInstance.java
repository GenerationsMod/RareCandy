package gg.generations.rarecandy.renderer.rendering;

import gg.generations.rarecandy.renderer.animation.Transform;
import gg.generations.rarecandy.renderer.components.RenderObject;
import gg.generations.rarecandy.renderer.ubo.UniformBlockUploader;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class ObjectInstance extends UniformBlockUploader {
    private final Matrix4f transformationMatrix;
    private final Matrix4f viewMatrix;
    private String variant;
    private RenderObject object;

    public ObjectInstance(int index, Matrix4f transformationMatrix, Matrix4f viewMatrix, String variant) {
        this(index, 64, transformationMatrix, viewMatrix, variant);
    }

    public ObjectInstance(int index, int size, Matrix4f transformationMatrix, Matrix4f viewMatrix, String variant) {
        super(index, size);
        this.transformationMatrix = transformationMatrix;
        this.viewMatrix = viewMatrix;
        this.variant = variant;
    }

    public void update(String materialId) {
        initalize();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long address = stack.nmalloc(80);
            var transform = getTransform(materialId);
            transform.offset().getToAddress(address);
            transform.offset().getToAddress(address+8);
            transformationMatrix.getToAddress(address+16);
            upload(0, 64, address);
        }
    }

    public Transform getTransform(String materialId) {
        return object.getTransform(variant);
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
