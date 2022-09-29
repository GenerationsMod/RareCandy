package com.pixelmongenerations.rarecandy.animation;

import com.pixelmongenerations.pkl.ModelNode;
import com.pixelmongenerations.rarecandy.rendering.Bone;
import de.javagl.jgltf.model.AnimationModel;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

/**
 * Cheaper on CPU After Startup, More Expensive on Memory
 */
public class CachedAnimation extends Animation {
    private final Map<Double, Matrix4f[]> cachedBoneTransforms = new HashMap<>();
    private boolean cached = false;

    public CachedAnimation(AnimationModel animation, Bone[] bones) {
        super(animation, bones);
    }

    public void cacheAllPossibleFrames(ModelNode rootModelNode) {
        var duration = Math.round(animationDuration);

        for (double i = 0; i < duration; i++) {
            var boneTransforms = new Matrix4f[this.bones.boneArray.length];
            readNodeHierarchy((float) i, rootModelNode, new Matrix4f().identity(), boneTransforms);
            cachedBoneTransforms.put(i, boneTransforms);
        }

        this.cached = true;
    }

    @Override
    public Matrix4f[] getFrameTransform(double animTime, ModelNode rootModelNode) {
        if(!cached) cacheAllPossibleFrames(rootModelNode);

        animTime = Math.round(animTime);
        return cachedBoneTransforms.getOrDefault(animTime, cachedBoneTransforms.get(animTime - 1));
    }
}
