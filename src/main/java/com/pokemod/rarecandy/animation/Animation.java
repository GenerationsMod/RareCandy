package com.pokemod.rarecandy.animation;

import com.pokemod.pkl.ModelNode;
import com.pokemod.rarecandy.Pair;
import de.javagl.jgltf.model.AnimationModel;
import de.javagl.jgltf.model.NodeModel;
import dev.thecodewarrior.binarysmd.studiomdl.SMDFile;
import dev.thecodewarrior.binarysmd.studiomdl.SkeletonBlock;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

public class Animation {

    public final String name;
    public final float ticksPerSecond;
    public final double animationDuration;
    public final Map<String, AnimationNode> animationNodes;
    protected final Skeleton skeleton;

    public Animation(AnimationModel rawAnimation, Skeleton skeleton) {
        this.name = rawAnimation.getName();
        this.ticksPerSecond = 1000;
        this.animationNodes = new HashMap<>();
        this.skeleton = skeleton;

        fillAnimationNodesGlb(rawAnimation.getChannels());
        this.animationDuration = findLastKeyTime();
    }

    public Animation(String name, SkeletonBlock smdFile, Skeleton bones) {
        this.name = name;
        this.ticksPerSecond = 1000;
        this.animationNodes = new HashMap<>();
        this.skeleton = bones;
        fillAnimationNodesSmdx(smdFile.keyframes);
        this.animationDuration = findLastKeyTime();
    }

    @Nullable
    private static <T> T getBlock(SMDFile file, Class<T> tClass) {
        return file.blocks.stream().filter(tClass::isInstance).map(tClass::cast).findFirst().orElse(null);
    }

    private double findLastKeyTime() {
        var duration = 0d;

        for (var value : this.animationNodes.values()) {
            var key = value.positionKeys.get(value.positionKeys.size() - 1);
            duration = key.time() > duration ? key.time() : duration;
        }

        return duration;
    }

    public double getAnimationTime(double secondsPassed) {
        var tps = ticksPerSecond != 0 ? ticksPerSecond : 25.0f;
        var ticksPassed = (float) secondsPassed * tps;
        return ticksPassed % animationDuration;
    }

    public Matrix4f[] getFrameTransform(double animTime) {
        var boneTransforms = new Matrix4f[this.skeleton.boneArray.length];
        readNodeHierarchy((float) animTime, skeleton.rootNode, new Matrix4f().identity(), boneTransforms);
        return boneTransforms;
    }

    protected void readNodeHierarchy(float animTime, ModelNode node, Matrix4f parentTransform, Matrix4f[] boneTransforms) {
        var name = node.name;
        var nodeTransform = node.transform;
        var animNode = animationNodes.get(name);

        if (animNode != null) {
            var scale = AnimationMath.calcInterpolatedScaling(animTime, animNode);
            var rotation = AnimationMath.calcInterpolatedRotation(animTime, animNode);
            var translation = AnimationMath.calcInterpolatedPosition(animTime, animNode);

            nodeTransform.identity().translationRotateScale(translation, rotation, scale);
        }

        var globalTransform = parentTransform.mul(nodeTransform, new Matrix4f());
        var bone = skeleton.get(name);
        if (bone != null)
            boneTransforms[skeleton.getId(bone)] = globalTransform.mul(bone.inversePoseMatrix, new Matrix4f());

        for (var child : node.children) {
            readNodeHierarchy(animTime, child, globalTransform, boneTransforms);
        }
    }

    private void fillAnimationNodesGlb(List<AnimationModel.Channel> channels) {
        for (var channel : channels) {
            var node = channel.getNodeModel();
            animationNodes.put(node.getName(), new AnimationNode(channels.stream().filter(c -> c.getNodeModel().equals(node)).toList(), node));
        }
    }


    private void fillAnimationNodesSmdx(List<SkeletonBlock.Keyframe> keyframes) {
        Map<String, List<Pair<Vector3f, Quaternionf>>> nodes = new HashMap<>();

        for (SkeletonBlock.Keyframe keyframe : keyframes) {
            var time = keyframe.time;
            var states = keyframe.states;

            for (SkeletonBlock.BoneState state : states) {
                var id = skeleton.getName(state.bone);
                List<Pair<Vector3f, Quaternionf>> list = nodes.computeIfAbsent(id, a -> new ArrayList<>());
                list.add(time, new Pair<Vector3f, Quaternionf>(new Vector3f(state.posX, state.posY, state.posZ), new Quaternionf().rotateXYZ(state.rotX, state.rotY, state.rotZ)));
            }
        }

        nodes.forEach((key, node) -> {
            animationNodes.put(key, new AnimationNode(node));
        });
    }

    public static class AnimationNode {
        public final TransformStorage<Vector3f> positionKeys = new TransformStorage<>();
        public final TransformStorage<Quaternionf> rotationKeys = new TransformStorage<>();
        public final TransformStorage<Vector3f> scaleKeys = new TransformStorage<>();

        public AnimationNode(List<Pair<Vector3f, Quaternionf>> pairs) {
            if(pairs.isEmpty()) {
                positionKeys.add(0, new Vector3f());
                rotationKeys.add(0, new Quaternionf());
            } else {
                for (int time = 0; time < pairs.size(); time++) {
                    positionKeys.add(time, pairs.get(time).a());
                    rotationKeys.add(time, pairs.get(time).b());
                }
            }

            scaleKeys.add(0, new Vector3f(1, 1, 1));
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

            if (positionKeys.size() == 0) positionKeys.add(0, node.getTranslation() != null ? convertArrayToVector3f(node.getTranslation()) : new Vector3f());
            if (rotationKeys.size() == 0) rotationKeys.add(0, node.getRotation() != null ? convertArrayToQuaterionf(node.getRotation()) : new Quaternionf());
            if (scaleKeys.size() == 0) scaleKeys.add(0, node.getScale() != null ? convertArrayToVector3f(node.getScale()) : new Vector3f(1, 1, 1));
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

    private static Vector3f convertArrayToVector3f(float[] array) {
        return new Vector3f().set(array);
    }

    private static Quaternionf convertArrayToQuaterionf(float[] array) {
        return new Quaternionf().set(array[0], array[1], array[2], array[3]);
    }
}
