package gg.generationsmod.rarecandy.model.animation;

import gg.generationsmod.rarecandy.model.animation.tranm.*;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Animation<T> {
    public final String name;
    public final double animationDuration;
    public Map<String, Integer> nodeIdMap = new HashMap<>();
    public final AnimationNode[] animationNodes;

    public  Map<String, Offset> offsets;
    public float ticksPerSecond;
    public final Skeleton skeleton;
    public boolean ignoreInstancedTime = false;

    public Animation(String name, int ticksPerSecond, Skeleton skeleton, T value, BiFunction<Animation<T>, T, AnimationNode[]> animationNodes, Function<T, Map<String, Offset>> offsets) {
        this.name = name;
        this.ticksPerSecond = ticksPerSecond;
        this.skeleton = skeleton;
        this.animationNodes = animationNodes.apply(this, value);
        this.offsets = offsets.apply(value);
        this.animationDuration = findLastKeyTime();

        for (var animationNode : this.animationNodes) {
            if (animationNode != null) {
                if (animationNode.positionKeys.getAtTime((int) animationDuration - 10) == null) {
                    if (animationNode.positionKeys.size() == 0) animationNode.positionKeys.add(0, new Vector3f());

                    animationNode.positionKeys.add(animationDuration, animationNode.positionKeys.get(0).value());
                }
                if (animationNode.rotationKeys.getAtTime((int) animationDuration - 10) == null)
                    animationNode.rotationKeys.add(animationDuration, animationNode.rotationKeys.get(0).value());
                if (animationNode.scaleKeys.getAtTime((int) animationDuration - 10) == null)
                    animationNode.scaleKeys.add(animationDuration, animationNode.scaleKeys.get(0).value());
            }
        }
    }

    private double findLastKeyTime() {
        var duration = 0d;

        for (var value : this.animationNodes) {
            if (value != null) {
                for (var key : value.positionKeys) duration = Math.max(key.time(), duration);
                for (var key : value.rotationKeys) duration = Math.max(key.time(), duration);
                for (var key : value.scaleKeys) duration = Math.max(key.time(), duration);
            }
        }

        return duration;
    }

    public float getAnimationTime(double secondsPassed) {
        var ticksPassed = (float) secondsPassed * ticksPerSecond;
        return (float) (ticksPassed % animationDuration);
    }

    public Matrix4f[] getFrameTransform(double secondsPassed) {
        var boneTransforms = new Matrix4f[this.skeleton.bones.length];
        readNodeHierarchy(getAnimationTime(secondsPassed), skeleton.rootNode, new Matrix4f().identity(), boneTransforms);
        return boneTransforms;
    }

    public void getFrameOffset(double secondsPassed, Map<String, Transform> offsets) {

        this.offsets.forEach((k, v) -> {
            var offsetInstance = offsets.get(k);
            this.offsets.get(k).calcOffset((float) secondsPassed, offsetInstance);
        });
    }

    public void readNodeHierarchy(float animTime, BoneNode node, Matrix4f parentTransform, Matrix4f[] boneTransforms) {
        var name = node.name;
        var nodeTransform = node.transform;

        var animationNodeId = nodeIdMap.getOrDefault(name, -1);
        var bone = skeleton.getBone(name);

        if (animationNodeId != -1) {
            var animNode = animationNodes[animationNodeId];

            if (animNode != null) {
                var scale = AnimationMath.calcInterpolatedScaling(animTime, animNode);
                var rotation = AnimationMath.calcInterpolatedRotation(animTime, animNode);
                var translation = AnimationMath.calcInterpolatedPosition(animTime, animNode);
                nodeTransform.identity().translationRotateScale(translation, rotation, scale);

                if (bone != null && this.isNaN(nodeTransform)) {
                    bone.lastSuccessfulTransform = new Matrix4f(nodeTransform);
                }
            }
        }

        var globalTransform = parentTransform.mul(nodeTransform, new Matrix4f());

        if (bone != null) {

            if (isNaN(globalTransform)) {
                globalTransform = parentTransform.mul(bone.lastSuccessfulTransform, new Matrix4f());
            }

            boneTransforms[skeleton.getId(bone)] = globalTransform.mul(bone.inverseBindMatrix, new Matrix4f());
        }

        for (var child : node.children)
            readNodeHierarchy(animTime, child, globalTransform, boneTransforms);
    }

    private boolean isNaN(Matrix4f nodeTransform) {
        return Float.isNaN(nodeTransform.m00());
    }

    public static class AnimationNode {
        public final TransformStorage<Vector3f> positionKeys = new TransformStorage<>();
        public final TransformStorage<Quaternionf> rotationKeys = new TransformStorage<>();
        public final TransformStorage<Vector3f> scaleKeys = new TransformStorage<>();

        public AnimationNode() {
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

    public interface Offset {
        void calcOffset(float animTime, Transform instance);
    }

        @Override
    public String toString() {
        return this.name;
    }
}
