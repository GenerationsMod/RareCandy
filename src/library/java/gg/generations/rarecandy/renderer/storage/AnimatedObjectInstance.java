package gg.generations.rarecandy.renderer.storage;

import gg.generations.rarecandy.renderer.animation.AnimationController;
import gg.generations.rarecandy.renderer.animation.AnimationInstance;
import gg.generations.rarecandy.renderer.animation.Transform;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.Arrays;

public class AnimatedObjectInstance extends ObjectInstance {
    public static final int ANIMATED_SIZE = MAT4_SIZE * 222;

    @Nullable
    public AnimationInstance currentAnimation;

    public AnimatedObjectInstance(Matrix4f modelMatrix, Matrix3f normalMatrix, int materialId) {
        super(modelMatrix, normalMatrix, materialId);
    }

    @Override
    public void update(SSBOBuffer instanceBuffer) {
        super.update(instanceBuffer);

        var bones = getTransforms();

        for (int i = 0; i < bones.length; i++) {
            Matrix4f bone = bones[i];
            instanceBuffer.put(bone);
        }
    }


    public Matrix4f[] getTransforms() {
        if (currentAnimation == null || currentAnimation.matrixTransforms == null)
            return AnimationController.NO_ANIMATION;
        return currentAnimation.matrixTransforms;
    }

    public void changeAnimation(AnimationInstance newAnimation) {
        if (currentAnimation != null) currentAnimation.destroy();
        this.currentAnimation = newAnimation;
    }

    public Transform getTransform(int material) {
        return currentAnimation != null ? currentAnimation.getOffset(material) : null;
    }

    @Override
    public void update(double absoluteTime) {
        super.update(absoluteTime);

        if(currentAnimation != null) {
            currentAnimation.update(absoluteTime);
        }
    }

    @Override
    protected void delink() {
        super.delink();
        if(currentAnimation != null) currentAnimation.destroy();
    }
}
