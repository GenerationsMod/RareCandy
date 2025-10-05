package gg.generations.rarecandy.renderer.storage;

import gg.generations.rarecandy.renderer.animation.AnimationController;
import gg.generations.rarecandy.renderer.animation.AnimationInstance;
import gg.generations.rarecandy.renderer.animation.Transform;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class AnimatedObjectInstance extends ObjectInstance {
    @Nullable
    public AnimationInstance currentAnimation;

    public AnimatedObjectInstance(int size, Matrix4f transformationMatrix, int materialId) {
        super(size, transformationMatrix, materialId);
    }

    public AnimatedObjectInstance(Matrix4f transformationMatrix, int materialId) {
        this(MAT4_SIZE * 221, transformationMatrix, materialId);
    }

    @Override
    public void update() {
        super.update();

        var bones = getTransforms();

        for (int i = 0; i < bones.length; i++) {
            var bone = bones[i];
            bone.getToAddress(pointer + (long) (1 + i) * MAT4_SIZE);
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
}
