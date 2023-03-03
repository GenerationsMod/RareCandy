package com.pokemod.rarecandy.rendering;

import com.pokemod.rarecandy.components.RenderObject;
import com.pokemod.rarecandy.pipeline.UniformBlockUploader;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class ObjectInstance extends UniformBlockUploader {
    private final Matrix4f transformationMatrix;
    private String materialId;
    private RenderObject object;

    public ObjectInstance(Matrix4f transformationMatrix, String materialId) {
        this(MAT4F_SIZE, transformationMatrix, materialId);
    }

    public ObjectInstance(int size, Matrix4f transformationMatrix, String materialId) {
        super(size, 1);
        this.transformationMatrix = transformationMatrix;
        this.materialId = materialId;
    }

    public void update() {
        try (var stack = MemoryStack.stackPush()) {
            var ptr = stack.nmalloc(MAT4F_SIZE);
            transformationMatrix.getToAddress(ptr);
            upload(0, MAT4F_SIZE, ptr);
        }
    }

    public void link(RenderObject object) {
        this.object = object;
        object.applyTransformOffset(transformationMatrix);
    }

    public Matrix4f transformationMatrix() {
        return transformationMatrix;
    }

    public String materialId() {
        return materialId;
    }

    public RenderObject object() {
        return object;
    }

    public void setVariant(String materialId) {
        this.materialId = materialId;
    }
}
