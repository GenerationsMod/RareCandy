package com.pokemod.rarecandy.storage;

import com.pokemod.rarecandy.animation.Animation;
import com.pokemod.rarecandy.animation.AnimationController;
import com.pokemod.rarecandy.animation.AnimationInstance;
import com.pokemod.rarecandy.components.AnimatedMeshObject;
import com.pokemod.rarecandy.components.MultiRenderObject;
import com.pokemod.rarecandy.rendering.ObjectInstance;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimatedObjectInstance extends ObjectInstance {

    @Nullable
    public AnimationInstance currentAnimation;

    public AnimatedObjectInstance(Matrix4f transformationMatrix, Matrix4f viewMatrix, String materialId) {
        super(transformationMatrix, viewMatrix, materialId);
    }

    public Map<String, Animation> getAnimationsIfAvailable() {
        var animations = new HashMap<String, Animation>();

        try {
            return getAnimatedMesh().animations;
        } catch (Exception ignored) {
        }

        return animations;
    }

    public AnimatedMeshObject getAnimatedMesh() {
        if (object() instanceof MultiRenderObject<?> mro) {
            return ((List<AnimatedMeshObject>) mro.objects).get(0);
        }
        return (AnimatedMeshObject) object();
    }

    public Matrix4f[] getTransforms() {
        if (currentAnimation == null || currentAnimation.matrixTransforms == null) return AnimationController.NO_ANIMATION;
        return currentAnimation.matrixTransforms;
    }

    public void changeAnimation(AnimationInstance newAnimation) {
        if(currentAnimation != null) currentAnimation.destroy();
        this.currentAnimation = newAnimation;
    }
}
