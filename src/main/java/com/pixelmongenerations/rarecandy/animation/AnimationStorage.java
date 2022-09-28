package com.pixelmongenerations.rarecandy.animation;

import com.pixelmongenerations.pkl.assimp.AssimpUtils;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.assimp.AINodeAnim;

import java.util.*;

public class AnimationStorage {

    public final String name;
    public final float ticksPerSecond;
    public final double animationDuration;
    public final Map<String, AnimationNode> animationNodes;
    private final Bones bones;

    public AnimationStorage(RawAnimation rawAnimation) {
        this.name = rawAnimation.name;
        this.ticksPerSecond = (float) (rawAnimation.aiAnim.mTicksPerSecond() != 0 ? rawAnimation.aiAnim.mTicksPerSecond() : 25.0f);
        this.animationDuration = rawAnimation.aiAnim.mDuration();
        this.animationNodes = new HashMap<>();
        this.bones = new Bones(rawAnimation.bones);

        fillAnimationNodes(rawAnimation);
    }

    public double getAnimationTime(double secondsPassed) {
        var tps = ticksPerSecond != 0 ? ticksPerSecond : 25.0f;
        var ticksPassed = (float) secondsPassed * tps;
        return ticksPassed % animationDuration;
    }

    public Matrix4f[] getFrameTransform(double animTime, ModelNode rootModelNode) {
        var boneTransforms = new Matrix4f[this.bones.boneArray.length];
        readNodeHierarchy((float) animTime, rootModelNode, new Matrix4f().identity(), boneTransforms);
        return boneTransforms;
    }

    protected void readNodeHierarchy(float animTime, ModelNode node, Matrix4f parentTransform, Matrix4f[] boneTransforms) {
        var name = node.name;
        var transform = node.transform;
        var animNode = animationNodes.get(name);

        if (animNode != null) {
            var scale = AnimationMath.calcInterpolatedScaling(animTime, animNode);
            var scalingMat = new Matrix4f().identity().scale(scale.x(), scale.y(), scale.z());

            var rotation = AnimationMath.calcInterpolatedRotation(animTime, animNode);
            var rotationMat = rotation.get(new Matrix4f().identity());

            var translation = AnimationMath.calcInterpolatedPosition(animTime, animNode);
            var translationMat = new Matrix4f().identity().translate(translation.x(), translation.y(), translation.z());

            transform = new Matrix4f(translationMat).mul(rotationMat).mul(scalingMat);
        }

        transform = new Matrix4f(parentTransform).mul(transform);
        var bone = bones.get(name);
        if(bone != null) boneTransforms[bones.getId(bone)] = new Matrix4f().identity().mul(new Matrix4f(transform)).mul(bone.offsetMatrix);

        for (var child : node.children) {
            readNodeHierarchy(animTime, child, transform, boneTransforms);
        }
    }

    private void fillAnimationNodes(RawAnimation rawAnimation) {
        for (var i = 0; i < rawAnimation.aiAnim.mNumChannels(); i++) {
            AINodeAnim nodeAnim = AINodeAnim.create(rawAnimation.aiAnim.mChannels().get(i));
            animationNodes.put(nodeAnim.mNodeName().dataString(), new AnimationNode(nodeAnim));
        }
    }

    public static class AnimationNode {
        public final TransformStorage<Vector3f> positionKeys = new TransformStorage<>();
        public final TransformStorage<Quaternionf> rotationKeys = new TransformStorage<>();
        public final TransformStorage<Vector3f> scaleKeys = new TransformStorage<>();

        public AnimationNode(AINodeAnim animNode) {
            if (animNode.mNumPositionKeys() > 0) {
                for (var positionKey : Objects.requireNonNull(animNode.mPositionKeys(), "Position keys were null")) {
                    positionKeys.add(positionKey.mTime(), AssimpUtils.from(positionKey.mValue()));
                }
            }

            if (animNode.mNumRotationKeys() > 0) {
                for (var rotationKey : Objects.requireNonNull(animNode.mRotationKeys(), "Rotation keys were null")) {
                    rotationKeys.add(rotationKey.mTime(), AssimpUtils.from(rotationKey.mValue()));
                }
            }

            if (animNode.mNumScalingKeys() > 0) {
                for (var scaleKey : Objects.requireNonNull(animNode.mScalingKeys(), "Scaling keys were null")) {
                    scaleKeys.add(scaleKey.mTime(), AssimpUtils.from(scaleKey.mValue()));
                }
            }
        }

        public TransformStorage.TimeKey<Vector3f> getDefaultPosition() {
            return positionKeys.get(0);
        }

        public TransformStorage.TimeKey<Quaternionf> getDefaultRotation() {
            return rotationKeys.get(0);
        }

        public TransformStorage.TimeKey<Vector3f> getDefaultScale() {
            return scaleKeys.get(0);
        }
    }

    @Override
    public String toString() {
        return this.name;
    }
}
