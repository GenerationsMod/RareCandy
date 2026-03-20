package gg.generations.rarecandy.renderer.rendering;

import gg.generations.rarecandy.renderer.components.MultiRenderObject;
import gg.generations.rarecandy.renderer.storage.SSBOBuffer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.io.Closeable;
import java.io.IOException;

public class ObjectInstance implements Closeable {
    public static final int MAT4_SIZE = 16 * Float.BYTES;

    private final Matrix4f transformationMatrix;
    private final Matrix3f normalMatrix;
    private int variant;
    private MultiRenderObject object;
    private boolean used = false;
    private double secondsPassed = 0f;
    public static final float DELINK_THRESHOLD =  0.1f; // Seconds before delinking


    public ObjectInstance(Matrix4f transformationMatrix, Matrix3f normalMatrix, int variant) {
        this.transformationMatrix = transformationMatrix;
        this.normalMatrix = normalMatrix;
        this.variant = variant;
    }

    public void update(SSBOBuffer instanceBuffer) {
        instanceBuffer.put(transformationMatrix);
    }

    public void link(MultiRenderObject object) {
        this.object = object;
    }

    public Matrix4f modelMatrix() {
        return transformationMatrix;
    }

    public Matrix3f normalMatrix() {
        return normalMatrix;
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
