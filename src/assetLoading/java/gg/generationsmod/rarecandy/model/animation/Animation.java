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
                if (animationNode.positionKeys.getAtTime((int) animationDuration - 10) == null)
                    animationNode.positionKeys.add(animationDuration, animationNode.positionKeys.get(0).value());
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

    public void readNodeHierarchy(float animTime, BoneNode node, Matrix4f parentTransform, Matrix4f[] boneTransforms) {
        var name = node.name;
        var nodeTransform = new Matrix4f(node.transform);

        var animationNodeId = nodeIdMap.getOrDefault(name, -1);
        if (animationNodeId != -1) {
            var animNode = animationNodes[animationNodeId];

            if (animNode != null) {
                var scale = AnimationMath.calcInterpolatedScaling(animTime, animNode);
                var rotation = AnimationMath.calcInterpolatedRotation(animTime, animNode);
                var translation = AnimationMath.calcInterpolatedPosition(animTime, animNode);
                nodeTransform.identity().translationRotateScale(translation, rotation, scale);
            }
        }

        var globalTransform = parentTransform.mul(nodeTransform, new Matrix4f());
        var bone = skeleton.getBone(name);
        if (bone != null) boneTransforms[skeleton.getId(bone)] = globalTransform.mul(bone.inverseBindMatrix, new Matrix4f());

        for (var child : node.children)
            readNodeHierarchy(animTime, child, globalTransform, boneTransforms);
    }

    private AnimationNode[] fillAnimationNodesTrinity(gg.generationsmod.rarecandy.model.animation.tranm.Animation rawAnimation) {
        var animationNodes = new AnimationNode[skeleton.nodes.length]; // BoneGroup

        for (int i = 0; i < rawAnimation.anim().bonesLength(); i++) {
            var boneAnim = rawAnimation.anim().bones(i);
            nodeIdMap.put(Objects.requireNonNull(boneAnim.name()).replace(".trmdl", ""), i);
            animationNodes[i] = new AnimationNode();

            switch (boneAnim.rotType()) {
                case QuatTrack.DynamicQuatTrack ->
                        TranmUtil.processDynamicQuatTrack((DynamicQuatTrack) Objects.requireNonNull(boneAnim.rot(new DynamicQuatTrack())), animationNodes[i].rotationKeys);
                case QuatTrack.FixedQuatTrack ->
                        TranmUtil.processFixedQuatTrack((FixedQuatTrack) Objects.requireNonNull(boneAnim.rot(new FixedQuatTrack())), animationNodes[i].rotationKeys);
                case QuatTrack.Framed8QuatTrack ->
                        TranmUtil.processFramed8QuatTrack((Framed8QuatTrack) Objects.requireNonNull(boneAnim.rot(new Framed8QuatTrack())), animationNodes[i].rotationKeys);
                case QuatTrack.Framed16QuatTrack ->
                        TranmUtil.processFramed16QuatTrack((Framed16QuatTrack) Objects.requireNonNull(boneAnim.rot(new Framed16QuatTrack())), animationNodes[i].rotationKeys);
            }

            switch (boneAnim.scaleType()) {
                case VectorTrack.DynamicVectorTrack ->
                        TranmUtil.processDynamicVecTrack((DynamicVectorTrack) Objects.requireNonNull(boneAnim.scale(new DynamicVectorTrack())), animationNodes[i].scaleKeys);
                case VectorTrack.FixedVectorTrack ->
                        TranmUtil.processFixedVecTrack((FixedVectorTrack) Objects.requireNonNull(boneAnim.scale(new FixedVectorTrack())), animationNodes[i].scaleKeys);
                case VectorTrack.Framed8VectorTrack ->
                        TranmUtil.processFramed8VecTrack((Framed8VectorTrack) Objects.requireNonNull(boneAnim.scale(new Framed8VectorTrack())), animationNodes[i].scaleKeys);
                case VectorTrack.Framed16VectorTrack ->
                        TranmUtil.processFramed16VecTrack((Framed16VectorTrack) Objects.requireNonNull(boneAnim.scale(new Framed16VectorTrack())), animationNodes[i].scaleKeys);
            }

            switch (boneAnim.transType()) {
                case VectorTrack.DynamicVectorTrack ->
                        TranmUtil.processDynamicVecTrack((DynamicVectorTrack) Objects.requireNonNull(boneAnim.trans(new DynamicVectorTrack())), animationNodes[i].positionKeys);
                case VectorTrack.FixedVectorTrack ->
                        TranmUtil.processFixedVecTrack((FixedVectorTrack) Objects.requireNonNull(boneAnim.trans(new FixedVectorTrack())), animationNodes[i].positionKeys);
                case VectorTrack.Framed8VectorTrack ->
                        TranmUtil.processFramed8VecTrack((Framed8VectorTrack) Objects.requireNonNull(boneAnim.trans(new Framed8VectorTrack())), animationNodes[i].positionKeys);
                case VectorTrack.Framed16VectorTrack ->
                        TranmUtil.processFramed16VecTrack((Framed16VectorTrack) Objects.requireNonNull(boneAnim.trans(new Framed16VectorTrack())), animationNodes[i].positionKeys);
            }
        }

        return animationNodes;
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
