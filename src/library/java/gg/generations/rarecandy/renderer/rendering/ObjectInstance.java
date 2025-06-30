package gg.generations.rarecandy.renderer.rendering;

import gg.generations.rarecandy.renderer.components.RenderObject;
import gg.generations.rarecandy.renderer.storage.AnimatedObjectInstance;
import gg.generations.rarecandy.renderer.ubo.UniformBlockUploader;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.io.Closeable;
import java.io.IOException;

public class ObjectInstance implements Closeable {
    public static final int MAT4_SIZE = 16 * Float.BYTES;

    private final Matrix4f transformationMatrix;
    public final long pointer;
    private String variant;
    private RenderObject object;

    public ObjectInstance(Matrix4f transformationMatrix, String variant) {
        this(MAT4_SIZE, transformationMatrix, variant);
    }

    public ObjectInstance(int size, Matrix4f transformationMatrix, String variant) {
        this.transformationMatrix = transformationMatrix;
        this.variant = variant;

        this.pointer = MemoryUtil.nmemAlloc(size);
        update();
    }

    public void update() {
        transformationMatrix.getToAddress(pointer);
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

    @Override
    public void close() throws IOException {
        MemoryUtil.nmemFree(pointer);
    }
}
