package gg.generations.rarecandy.renderer.animation;

import gg.generations.rarecandy.pokeutils.ModelNode;
import gg.generations.rarecandy.renderer.loading.ModelLoader;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class Animation {
    public static final int FPS_60 = 1000;
    public static final int FPS_24 = 400;
    public static final int GLB_SPEED = 30;
    public static BiConsumer<Animation, String> animationModifier = (animation, s) -> {};

    protected static Vector3f TRANSLATE = new Vector3f();
    protected static Vector3f SCALE = new Vector3f();

    public final String name;
    public final double animationDuration;
    protected final Skeleton skeleton;
    public Map<String, Integer> nodeIdMap = new HashMap<>();

    private final AnimationNode[] animationNodes;
    public Map<String, Offset> offsets;

    public float ticksPerSecond;
    public boolean ignoreInstancedTime = false;

    private boolean ignoreScaling;

    public <T> Animation(String name, int ticksPerSecond, Skeleton skeleton, ModelLoader.NodeProvider nodeProvider, Map<String, Offset> offsets, boolean ignoreScaling) {
        this.name = name;
        this.ticksPerSecond = ticksPerSecond;
        this.skeleton = skeleton;
        this.animationNodes = nodeProvider.getNode(this, skeleton);
        this.offsets = offsets;
        this.animationDuration = findLastKeyTime();

        this.ignoreScaling = ignoreScaling;

        if(this.animationNodes != null) {
            for (var animationNode : getAnimationNodes()) {
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

        if (animationNodes != null) {
            for (var value : this.getAnimationNodes()) {
                if (value != null) {
                    for (var key : value.positionKeys) duration = Math.max(key.time(), duration);
                    for (var key : value.rotationKeys) duration = Math.max(key.time(), duration);
                    for (var key : value.scaleKeys) duration = Math.max(key.time(), duration);
                }
            }
        }

        if (duration == 0) {
            for (var value : this.offsets.values()) {
                duration = Math.max(value.duration(), duration);
            }
        }

        return duration;
    }

    public float getAnimationTime(double secondsPassed) {
        var ticksPassed = (float) secondsPassed * ticksPerSecond;
        return (float) (ticksPassed % animationDuration);
    }

    public Matrix4f[] getFrameTransform(AnimationInstance instance) {
        var boneTransforms = new Matrix4f[this.skeleton.jointSize];
        readNodeHierarchy(instance.getCurrentTime(), skeleton.rootNode, new Matrix4f().identity(), boneTransforms);
        return boneTransforms;
    }

    public void getFrameOffset(AnimationInstance<?> instance) {
        this.offsets.forEach((k, v) -> {
            var offsetInstance = instance.offsets.computeIfAbsent(k, a -> new Transform());
            offsetInstance.offset().zero();
            offsetInstance.scale().set(1, 1);

            offsets.get(k).calcOffset(instance.getCurrentTime(), offsetInstance);
        });
    }

    public Matrix4f[] getFrameTransform(double secondsPassed) {
        var boneTransforms = new Matrix4f[this.skeleton.jointSize];
        readNodeHierarchy(getAnimationTime(secondsPassed), skeleton.rootNode, new Matrix4f().identity(), boneTransforms);
        return boneTransforms;
    }

    private static final Matrix4f matrix = new Matrix4f();

    protected void readNodeHierarchy(float animTime, ModelNode node, Matrix4f parentTransform, Matrix4f[] boneTransforms) {
        var name = node.name;
        var nodeTransform = matrix.set(node.transform);
        if (node.id == -1) node.id = nodeIdMap.getOrDefault(name, -1);
        var bone = skeleton.get(name);

        if (node.id != -1) {
            var animNode = getAnimationNodes()[node.id];

            if (animNode != null) {
                var scale = ignoreScaling ? SCALE : AnimationMath.calcInterpolatedScaling(animTime, animNode);
                var rotation = AnimationMath.calcInterpolatedRotation(animTime, animNode);

                var translation = name.equalsIgnoreCase("origin") ? AnimationMath.calcInterpolatedPosition(animTime, animNode) : TRANSLATE;
                nodeTransform.identity().translationRotateScale(translation, rotation, scale);

//                if (bone != null && this.isNaN(nodeTransform)) {
//                    bone.lastSuccessfulTransform = nodeTransform.set(nodeTransform);
//                }
            }
        } else {
            if (bone != null) {
                var scale = bone.poseScale;
                var rotation = bone.poseRotation;
                var translation = bone.posePosition;
                nodeTransform.identity().translationRotateScale(translation, rotation, scale);
            }
        }

        var globalTransform = parentTransform.mul(nodeTransform, new Matrix4f());
        if (bone != null && bone.jointId != -1) {
//            if (isNaN(globalTransform)) {
//                globalTransform = parentTransform.mul(bone.lastSuccessfulTransform, new Matrix4f());
//            }

            boneTransforms[bone.jointId] = globalTransform.mul(bone.inversePoseMatrix, new Matrix4f());
        }

        for (var child : node.children)
            readNodeHierarchy(animTime, child, globalTransform, boneTransforms);
    }

    private boolean isNaN(Matrix4f nodeTransform) {
        return Float.isNaN(nodeTransform.m00());
    }

    public int newNode(String nodeName) {
        return nodeIdMap.size();
    }

    @Override
    public String toString() {
        return this.name;
    }

    public AnimationNode[] getAnimationNodes() {
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


    public record Offset(TransformStorage<Float> uOffset, TransformStorage<Float> vOffset, TransformStorage<Float> uScale, TransformStorage<Float> vScale, float duration) {
        public static <T> T calcInterpolatedFloat(float animTime, TransformStorage<T> node, T defaultVal) {
            if (node.size() == 0) return defaultVal;

            var offset = findOffset(animTime, node);
            return offset.value();
        }

        public static <T> TransformStorage.TimeKey<T> findOffset(float animTime, TransformStorage<T> keys) {
            for (var key : keys) {
                if (animTime < key.time())
                    return keys.getBefore(key);
            }

            return keys.get(0);
        }

        public void calcOffset(float animTime, Transform instance) {

            var uOffset = calcInterpolatedFloat(animTime, this.uOffset(), 0f);
            var vOffset = calcInterpolatedFloat(animTime, this.vOffset(), 0f);
            var uScale = calcInterpolatedFloat(animTime, this.uScale(), 1f);
            var vScale = calcInterpolatedFloat(animTime, this.vScale(), 1f);

            instance.offset().set(uOffset, vOffset);
            instance.scale().set(uScale, vScale);
        }
    }
}
