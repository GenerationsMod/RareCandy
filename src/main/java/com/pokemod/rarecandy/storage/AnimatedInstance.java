package com.pokemod.rarecandy.storage;

import com.pokemod.rarecandy.animation.Animation;
import com.pokemod.rarecandy.rendering.ObjectInstance;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class AnimatedInstance extends ObjectInstance {
    @Nullable
    public Animation currentAnimation;
    public Matrix4f[] transforms = ObjectManager.NO_ANIMATION;

    public AnimatedInstance(Matrix4f transformationMatrix, Matrix4f viewMatrix, String materialId) {
        this(transformationMatrix, viewMatrix, materialId, 0xFFFFFFFF);
    }

    public AnimatedInstance(Matrix4f transformationMatrix, Matrix4f viewMatrix, String materialId, int lightColor) {
        super(transformationMatrix, viewMatrix, materialId, lightColor);
    }
}
