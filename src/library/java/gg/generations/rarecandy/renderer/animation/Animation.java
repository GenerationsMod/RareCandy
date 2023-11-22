package gg.generations.rarecandy.renderer.animation;

import de.javagl.jgltf.model.AnimationModel;
import de.javagl.jgltf.model.NodeModel;
import dev.thecodewarrior.binarysmd.studiomdl.SkeletonBlock;
import gg.generations.rarecandy.pokeutils.ModelNode;
import gg.generations.rarecandy.pokeutils.tranm.*;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.BiConsumer;

public abstract class Animation<T> {
    public static final int FPS_60 = 1000;
    public static final int FPS_24 = 400;
    public static final int GLB_SPEED = FPS_60;
    public static BiConsumer<Animation, String> animationModifier = (animation, s) -> {
    };
    public final String name;
    public final double animationDuration;
    protected final Skeleton skeleton;
    public Map<String, Integer> nodeIdMap = new HashMap<>();

    private final AnimationNode[] animationNodes;

    public float ticksPerSecond;
    public boolean ignoreInstancedTime = false;

    public Animation(String name, float ticksPerSecond, Skeleton skeleton, T item) {
        this.name = name;
        this.ticksPerSecond = ticksPerSecond;
        this.skeleton = skeleton;
        this.animationNodes = fillAnimationNodes(item);
        this.animationDuration = findLastKeyTime();
    }

    abstract AnimationNode[] fillAnimationNodes(T item);

    private static Vector3f convertArrayToVector3f(float[] array) {
        return new Vector3f().set(array);
    }

    private static Quaternionf convertArrayToQuaterionf(float[] array) {
        return new Quaternionf().set(array[0], array[1], array[2], array[3]);
    }

    private double findLastKeyTime() {
        var duration = 0d;

        for (var value : this.getAnimationNodes()) {
            if (value != null) {
                for (var key : value.positionKeys) duration = Math.max(key.time(), duration);
                for (var key : value.rotationKeys) duration = Math.max(key.time(), duration);
                for (var key : value.scaleKeys) duration = Math.max(key.time(), duration);
            }
        }

        return duration;
    }

    public float getAnimationTime(double secondsPassed) {
        var ticksPassed = (float) secondsPassed * 400;
        return (float) (ticksPassed % animationDuration);
    }

    public Matrix4f[] getFrameTransform(AnimationInstance instance) {
        var boneTransforms = new Matrix4f[this.skeleton.jointSize];
        readNodeHierarchy(instance.getCurrentTime(), skeleton.rootNode, new Matrix4f().identity(), boneTransforms);
        return boneTransforms;
    }

    public Matrix4f[] getFrameTransform(double secondsPassed) {
        var boneTransforms = new Matrix4f[this.skeleton.jointSize];
        readNodeHierarchy(getAnimationTime(secondsPassed), skeleton.rootNode, new Matrix4f().identity(), boneTransforms);
        return boneTransforms;
    }

    protected void readNodeHierarchy(float animTime, ModelNode node, Matrix4f parentTransform, Matrix4f[] boneTransforms) {
        var name = node.name;
        var nodeTransform = node.transform;
        if (node.id == -1) node.id = nodeIdMap.getOrDefault(name, -1);
        var bone = skeleton.get(name);

        if (node.id != -1) {
            var animNode = getAnimationNodes()[node.id];

            if (animNode != null) {
                var scale = AnimationMath.calcInterpolatedScaling(animTime, animNode);
                var rotation = AnimationMath.calcInterpolatedRotation(animTime, animNode);
                var translation = AnimationMath.calcInterpolatedPosition(animTime, animNode);
                nodeTransform.identity().translationRotateScale(translation, rotation, scale);
                if (bone != null && !Float.isNaN(nodeTransform.m00()))
                    bone.lastSuccessfulTransform = new Matrix4f(nodeTransform);
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
            if (Float.isNaN(globalTransform.m00())) {
                globalTransform = parentTransform.mul(bone.lastSuccessfulTransform, new Matrix4f());
            }

            boneTransforms[bone.jointId] = globalTransform.mul(bone.inversePoseMatrix, new Matrix4f());
        }

        for (var child : node.children)
            readNodeHierarchy(animTime, child, globalTransform, boneTransforms);
    }

    protected int newNode(String nodeName) {
        return nodeIdMap.size();
    }

    @Override
    public String toString() {
        return this.name;
    }

    public AnimationNode[] getAnimationNodes() {
        return animationNodes;
    }

    public record SmdBoneStateKey(int time, Vector3f pos, Quaternionf rot) {
    }

    public static class AnimationNode {
        public final TransformStorage<Vector3f> positionKeys = new TransformStorage<>();
        public final TransformStorage<Quaternionf> rotationKeys = new TransformStorage<>();
        public final TransformStorage<Vector3f> scaleKeys = new TransformStorage<>();

        public AnimationNode(List<SmdBoneStateKey> keys) {
            if (keys.isEmpty()) {
                positionKeys.add(0, new Vector3f());
                rotationKeys.add(0, new Quaternionf());
            } else {
                for (var key : keys) {
                    positionKeys.add(key.time(), key.pos());
                    rotationKeys.add(key.time(), key.rot());
                }
            }

            scaleKeys.add(0, new Vector3f(1, 1, 1));
        }

        public AnimationNode() {
        }

        public AnimationNode(List<AnimationModel.Channel> nodeChannels, NodeModel node) {
            if (nodeChannels.size() > 3) throw new RuntimeException("More channels than we can handle");

            for (var channel : nodeChannels) {
                switch (channel.getPath()) {
                    case "translation" -> {
                        var timeBuffer = channel.getSampler().getInput().getBufferViewModel().getBufferViewData().asFloatBuffer();
                        var translationBuffer = channel.getSampler().getOutput().getBufferViewModel().getBufferViewData().asFloatBuffer();

                        for (var i = 0; i < timeBuffer.capacity(); i++) {
                            positionKeys.add(timeBuffer.get(), new Vector3f(translationBuffer.get(), translationBuffer.get(), translationBuffer.get()));
                        }
                    }

                    case "rotation" -> {
                        var timeBuffer = channel.getSampler().getInput().getBufferViewModel().getBufferViewData().asFloatBuffer();
                        var rotationBuffer = channel.getSampler().getOutput().getBufferViewModel().getBufferViewData().asFloatBuffer();

                        for (var i = 0; i < timeBuffer.capacity(); i++) {
                            rotationKeys.add(timeBuffer.get(), new Quaternionf(rotationBuffer.get(), rotationBuffer.get(), rotationBuffer.get(), rotationBuffer.get()));
                        }
                    }

                    case "scale" -> {
                        var timeBuffer = channel.getSampler().getInput().getBufferViewModel().getBufferViewData().asFloatBuffer();
                        var scaleBuffer = channel.getSampler().getOutput().getBufferViewModel().getBufferViewData().asFloatBuffer();

                        for (var i = 0; i < timeBuffer.capacity(); i++) {
                            scaleKeys.add(timeBuffer.get(), new Vector3f(scaleBuffer.get(), scaleBuffer.get(), scaleBuffer.get()));
                        }
                    }

                    default -> throw new RuntimeException("Unknown Channel Type \"" + channel.getPath() + "\"");
                }
            }

            if (positionKeys.size() == 0)
                positionKeys.add(0, node.getTranslation() != null ? convertArrayToVector3f(node.getTranslation()) : new Vector3f());
            if (rotationKeys.size() == 0)
                rotationKeys.add(0, node.getRotation() != null ? convertArrayToQuaterionf(node.getRotation()) : new Quaternionf());
            if (scaleKeys.size() == 0)
                scaleKeys.add(0, node.getScale() != null ? convertArrayToVector3f(node.getScale()) : new Vector3f(1, 1, 1));
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
}
