package gg.generations.rarecandy.renderer.storage;



import gg.generations.rarecandy.renderer.animation.ITransform;
import gg.generations.rarecandy.renderer.animation.ITransformSet;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class AnimatedObjectInstance extends ObjectInstance {
    public static final int ANIMATED_SIZE = MAT4_SIZE * 222;

    private final Matrix4f[] transforms = new Matrix4f[220];
    private ITransformSet[] materialTransforms;

    public AnimatedObjectInstance(Matrix4f modelMatrix, Matrix3f normalMatrix, int materialId) {
        super(modelMatrix, normalMatrix, materialId);

        for (int i = 0; i < 220; i++) {
            transforms[i] = new Matrix4f();
        }
    }

    @Override
    public void update(SSBOBuffer instanceBuffer) {
        super.update(instanceBuffer);

        var bones = getTransforms();

        for (Matrix4f bone : bones) {
            instanceBuffer.put(bone);
        }
    }


    public Matrix4f[] getTransforms() {
        return transforms;
    }

    public ITransform getTransform(int material, int texture) {
        return ITransform.Companion.getDEFAULT();
    }

    @Override
    protected void delink() {
        super.delink();
    }
}
