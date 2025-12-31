package gg.generations.rarecandy.renderer.rendering;

import gg.generations.rarecandy.renderer.components.MultiRenderObject;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.io.Closeable;
import java.io.IOException;

public class ObjectInstance implements Closeable {
    public static final int MAT4_SIZE = 16 * Float.BYTES;

    private final Matrix4f transformationMatrix;
    public final long pointer;
    private int variant;
    private MultiRenderObject object;
    private boolean used = false;
    private double secondsPassed = 0f;
    public static final float DELINK_THRESHOLD =  0.1f; // Seconds before delinking

    public ObjectInstance(Matrix4f transformationMatrix, int variant) {
        this(MAT4_SIZE, transformationMatrix, variant);
    }

    public ObjectInstance(int size, Matrix4f transformationMatrix, int variant) {
        this.transformationMatrix = transformationMatrix;
        this.variant = variant;

        this.pointer = MemoryUtil.nmemAlloc(size);
        update();
    }

    public void update() {
        transformationMatrix.getToAddress(pointer);
    }

    public void link(MultiRenderObject object) {
        this.object = object;
    }

    public Matrix4f transformationMatrix() {
        return transformationMatrix;
    }

    public int materialId() {
        return variant;
    }

    public MultiRenderObject object() {
        return object;
    }

    public int variant() {
        return variant;
    }

    public void setVariant(int variant) {
        this.variant = variant;
    }

    @Override
    public void close() throws IOException {
        MemoryUtil.nmemFree(pointer);
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
