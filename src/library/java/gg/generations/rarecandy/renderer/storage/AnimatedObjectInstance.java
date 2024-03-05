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
import org.joml.Vector2f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimatedObjectInstance extends ObjectInstance {

    @Nullable
    public AnimationInstance currentAnimation;

    public AnimatedObjectInstance(Matrix4f transformationMatrix, Matrix4f viewMatrix, String materialId) {
        super(transformationMatrix, viewMatrix, materialId);
    }

    public Map<String, Animation<?>> getAnimationsIfAvailable() {

        try {
            return getAnimatedMesh().animations;
        } catch (Exception ignored) {
        }

        return new HashMap<String, Animation<?>>();
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

    public Transform getOffset(String material) {
        return currentAnimation != null ? currentAnimation.getOffset(material) : null;
    }
}
