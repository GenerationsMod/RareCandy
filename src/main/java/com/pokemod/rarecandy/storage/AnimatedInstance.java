package com.pokemod.rarecandy.storage;

import com.pokemod.rarecandy.animation.Animation;
import com.pokemod.rarecandy.components.AnimatedMeshObject;
import com.pokemod.rarecandy.components.MultiRenderObject;
import com.pokemod.rarecandy.rendering.ObjectInstance;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.List;

public class AnimatedInstance extends ObjectInstance {
    public @Nullable Animation currentAnimation;
    public Matrix4f[] transforms = ObjectManager.NO_ANIMATION;

    public AnimatedInstance(Matrix4f transformationMatrix, Matrix4f viewMatrix, String materialId) {
        super(transformationMatrix, viewMatrix, materialId);
    }

    public AnimatedMeshObject getAnimatedMesh() {
        if (object() instanceof MultiRenderObject<?> mro) {
            return ((List<AnimatedMeshObject>) mro.objects).get(0);
        }
        return (AnimatedMeshObject) object();
    }
}
