package com.pokemod.rarecandy.animation;

import com.pokemod.pokeutils.ModelNode;
import com.pokemod.pokeutils.tranm.*;
import de.javagl.jgltf.model.AnimationModel;
import de.javagl.jgltf.model.NodeModel;
import dev.thecodewarrior.binarysmd.studiomdl.SkeletonBlock;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

public class Animation {
    public static final int FPS_60 = 1000;
    public static final int FPS_24 = 400;
    public static final int GLB_SPEED = FPS_60;
    public final String name;
    public final double animationDuration;
    public Map<String, Integer> nodeIdMap = new HashMap<>();
    public final AnimationNode[] animationNodes;
    public float ticksPerSecond;
    protected final Skeleton skeleton;

    public Animation(AnimationModel rawAnimation, Skeleton skeleton, int speed) {
        this.name = rawAnimation.getName();
        this.ticksPerSecond = speed;
        this.skeleton = skeleton;
        this.animationNodes = fillAnimationNodesGlb(rawAnimation.getChannels());
        this.animationDuration = findLastKeyTime();
    }

    public Animation(String name, com.pokemod.pokeutils.tranm.Animation rawAnimation, Skeleton skeleton) {
        this.name = name;
        this.ticksPerSecond = FPS_60 - 95;
        this.skeleton = skeleton;
        this.animationNodes = fillAnimationNodesGfb(rawAnimation);
        this.animationDuration = findLastKeyTime();
    }

    public Animation(String name, SkeletonBlock smdFile, Skeleton bones, int speed) {
        this.name = name;
        this.ticksPerSecond = speed;
        this.skeleton = bones;
        this.animationNodes = fillAnimationNodesSmdx(smdFile.keyframes);
        this.animationDuration = findLastKeyTime();
    }

    private double findLastKeyTime() {
        var duration = 0d;

        for (var value : this.animationNodes) {
            if (value != null) {
                if (value.positionKeys.size() == 0)
                    continue;
                var key = value.positionKeys.get(value.positionKeys.size() - 1);
                duration = key.time() > duration ? key.time() : duration;
            }
        }

        return duration;
    }

    public float getAnimationTime(double secondsPassed) {
        var tps = ticksPerSecond != 0 ? ticksPerSecond : 25.0f;
        var ticksPassed = (float) secondsPassed * tps;
        return (float) (ticksPassed % animationDuration);
    }

    public Matrix4f[] getFrameTransform(double secondsPassed) {
        var boneTransforms = new Matrix4f[this.skeleton.boneArray.length];
        readNodeHierarchy(getAnimationTime(secondsPassed), skeleton.rootNode, new Matrix4f().identity(), boneTransforms);
        return boneTransforms;
    }

    protected void readNodeHierarchy(float animTime, ModelNode node, Matrix4f parentTransform, Matrix4f[] boneTransforms) {
        var name = node.name;
        var nodeTransform = node.transform;
        if (node.id == -1) node.id = nodeIdMap.getOrDefault(name, -1);

        if (node.id != -1) {
            var animNode = animationNodes[node.id];

            if (animNode != null) {
                var scale = AnimationMath.calcInterpolatedScaling(animTime, animNode);
                var rotation = AnimationMath.calcInterpolatedRotation(animTime, animNode);
                var translation = AnimationMath.calcInterpolatedPosition(animTime, animNode);
                nodeTransform.identity().translationRotateScale(translation, rotation, scale);
            }
        } else {
            var bone = skeleton.get(name);

            try {
                var scale = bone.poseScale;
                var rotation = bone.poseRotation;
                var translation = bone.posePosition;
                nodeTransform.identity().translationRotateScale(translation, rotation, scale);
            } catch (Exception ignored) {}
        }

        var globalTransform = parentTransform.mul(nodeTransform, new Matrix4f());
        var bone = skeleton.get(name);
        if (bone != null)
            boneTransforms[skeleton.getId(bone)] = globalTransform.mul(bone.inversePoseMatrix, new Matrix4f());

        for (var child : node.children) {
            readNodeHierarchy(animTime, child, globalTransform, boneTransforms);
        }
    }

    private AnimationNode[] fillAnimationNodesGlb(List<AnimationModel.Channel> channels) {
        var animationNodes = new AnimationNode[channels.size()];

        for (var channel : channels) {
            var node = channel.getNodeModel();
            animationNodes[nodeIdMap.computeIfAbsent(node.getName(), this::newNode)] = new AnimationNode(channels.stream().filter(c -> c.getNodeModel().equals(node)).toList(), node);
        }

        return animationNodes;
    }

    private AnimationNode[] fillAnimationNodesGfb(com.pokemod.pokeutils.tranm.Animation rawAnimation) {
        var animationNodes = new AnimationNode[skeleton.boneMap.size()]; // BoneGroup

        for (int i = 0; i < rawAnimation.anim().bonesLength(); i++) {
            var boneAnim = rawAnimation.anim().bones(i);
            nodeIdMap.put(boneAnim.name().replace(".trmdl", ""), i);
            animationNodes[i] = new AnimationNode();

            switch (boneAnim.rotType()) {
                case QuatTrack.DynamicQuatTrack -> TranmUtil.processDynamicQuatTrack((DynamicQuatTrack) Objects.requireNonNull(boneAnim.rot(new DynamicQuatTrack())), animationNodes[i].rotationKeys);
                case QuatTrack.FixedQuatTrack -> TranmUtil.processFixedQuatTrack((FixedQuatTrack) Objects.requireNonNull(boneAnim.rot(new FixedQuatTrack())), animationNodes[i].rotationKeys);
                case QuatTrack.Framed8QuatTrack -> TranmUtil.processFramed8QuatTrack((Framed8QuatTrack) Objects.requireNonNull(boneAnim.rot(new Framed8QuatTrack())), animationNodes[i].rotationKeys);
                case QuatTrack.Framed16QuatTrack -> TranmUtil.processFramed16QuatTrack((Framed16QuatTrack) Objects.requireNonNull(boneAnim.rot(new Framed16QuatTrack())), animationNodes[i].rotationKeys);
            }

            switch (boneAnim.scaleType()) {
                case VectorTrack.DynamicVectorTrack -> TranmUtil.processDynamicVecTrack((DynamicVectorTrack) Objects.requireNonNull(boneAnim.scale(new DynamicVectorTrack())), animationNodes[i].scaleKeys);
                case VectorTrack.FixedVectorTrack -> TranmUtil.processFixedVecTrack((FixedVectorTrack) Objects.requireNonNull(boneAnim.scale(new FixedVectorTrack())), animationNodes[i].scaleKeys);
                case VectorTrack.Framed8VectorTrack -> TranmUtil.processFramed8VecTrack((Framed8VectorTrack) Objects.requireNonNull(boneAnim.scale(new Framed8VectorTrack())), animationNodes[i].scaleKeys);
                case VectorTrack.Framed16VectorTrack -> TranmUtil.processFramed16VecTrack((Framed16VectorTrack) Objects.requireNonNull(boneAnim.scale(new Framed16VectorTrack())), animationNodes[i].scaleKeys);
            }

            switch (boneAnim.transType()) {
                case VectorTrack.DynamicVectorTrack -> TranmUtil.processDynamicVecTrack((DynamicVectorTrack) Objects.requireNonNull(boneAnim.trans(new DynamicVectorTrack())), animationNodes[i].positionKeys);
                case VectorTrack.FixedVectorTrack -> TranmUtil.processFixedVecTrack((FixedVectorTrack) Objects.requireNonNull(boneAnim.trans(new FixedVectorTrack())), animationNodes[i].positionKeys);
                case VectorTrack.Framed8VectorTrack -> TranmUtil.processFramed8VecTrack((Framed8VectorTrack) Objects.requireNonNull(boneAnim.trans(new Framed8VectorTrack())), animationNodes[i].positionKeys);
                case VectorTrack.Framed16VectorTrack -> TranmUtil.processFramed16VecTrack((Framed16VectorTrack) Objects.requireNonNull(boneAnim.trans(new Framed16VectorTrack())), animationNodes[i].positionKeys);
            }
        }

        return animationNodes;
    }

    private AnimationNode[] fillAnimationNodesSmdx(List<SkeletonBlock.Keyframe> keyframes) {
        var nodes = new HashMap<String, List<SmdBoneStateKey>>();

        for (var keyframe : keyframes) {
            var time = keyframe.time;
            var states = keyframe.states;

            for (var boneState : states) {
                if (boneState.bone < skeleton.boneArray.length - 1) {
                    var id = skeleton.getName(boneState.bone);
                    var list = nodes.computeIfAbsent(id, a -> new ArrayList<>());
                    list.add(new SmdBoneStateKey(time, new Vector3f(boneState.posX, boneState.posY, boneState.posZ), new Quaternionf().rotateZYX(boneState.rotZ, boneState.rotY, boneState.rotX)));
                }
            }

            nodes.forEach((k, v) -> v.sort(Comparator.comparingInt(SmdBoneStateKey::time)));
        }

        var animationNodes = new AnimationNode[nodes.size()];
        for (var entry : nodes.entrySet()) {
            animationNodes[nodeIdMap.computeIfAbsent(entry.getKey(), this::newNode)] = new AnimationNode(entry.getValue());
        }

        return animationNodes;
    }

    private int newNode(String nodeName) {
        return nodeIdMap.size();
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
