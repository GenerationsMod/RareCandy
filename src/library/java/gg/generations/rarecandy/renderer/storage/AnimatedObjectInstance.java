package gg.generations.rarecandy.renderer.storage;

import gg.generations.rarecandy.renderer.animation.Animation;
import gg.generations.rarecandy.renderer.animation.AnimationController;
import gg.generations.rarecandy.renderer.animation.AnimationInstance;
import gg.generations.rarecandy.renderer.animation.Transform;
import gg.generations.rarecandy.renderer.components.AnimatedMeshObject;
import gg.generations.rarecandy.renderer.components.MultiRenderObject;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimatedObjectInstance extends ObjectInstance {

    @Nullable
    public AnimationInstance currentAnimation;

    public AnimatedObjectInstance(int index, Matrix4f transformationMatrix, Matrix4f viewMatrix, String materialId) {
        super(index, 14160, transformationMatrix, viewMatrix, materialId);
    }

    public Map<String, Animation> getAnimationsIfAvailable() {

        try {
            return getAnimatedMesh().animations;
        } catch (Exception ignored) {
        }

        return new HashMap<>();
    }

    public AnimatedMeshObject getAnimatedMesh() {
        if (object() instanceof MultiRenderObject<?> mro) {
            return ((List<AnimatedMeshObject>) mro.objects).get(0);
        }
        return (AnimatedMeshObject) object();
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

    public Transform getTransform(String material) {
        return currentAnimation != null ? currentAnimation.getOffset(material) : super.getTransform(material);
    }

    public void update(String materialId) {
        initalize();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long address = stack.nmalloc(80);
            var transform = getTransform(materialId);
            transform.offset().getToAddress(address);
            transform.offset().getToAddress(address+8);
            transformationMatrix().getToAddress(address+16);

            var skeleton = getTransforms();

            for (int i = 0; i < skeleton.length; i++) {
                skeleton[i].getToAddress(address + 80 + 64L * i);
            }

            upload(0, 14160, address);
        }
    }
}
