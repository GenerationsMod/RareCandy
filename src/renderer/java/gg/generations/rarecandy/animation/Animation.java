package gg.generations.rarecandy.animation;

import de.javagl.jgltf.model.AnimationModel;
import de.javagl.jgltf.model.NodeModel;
import dev.thecodewarrior.binarysmd.studiomdl.SkeletonBlock;
import gg.generations.pokeutils.ModelNode;
import gg.generations.pokeutils.tranm.*;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.BiConsumer;

public class Animation {
    public static final int FPS_60 = 1000;
    public static final int FPS_24 = 400;
    public static final int GLB_SPEED = FPS_60;
    public static BiConsumer<Animation, String> animationModifier = (animation, s) -> {
    };
    public final String name;
    public final double animationDuration;
    public final AnimationNode[] animationNodes;
    protected final Skeleton skeleton;
    public Map<String, Integer> nodeIdMap = new HashMap<>();
    public float ticksPerSecond;
    public boolean ignoreInstancedTime = false;

    public Animation(AnimationModel rawAnimation, Skeleton skeleton, int speed) {
        this.name = rawAnimation.getName();
        this.ticksPerSecond = speed;
        this.skeleton = skeleton;
        this.animationNodes = fillAnimationNodesGlb(rawAnimation.getChannels());
        this.animationDuration = findLastKeyTime();
        animationModifier.accept(this, "glb");
    }

    public Animation(String name, gg.generations.pokeutils.tranm.Animation rawAnimation, Skeleton skeleton) {
        this.name = name;
        this.ticksPerSecond = FPS_60 - 95;
        this.skeleton = skeleton;
        this.animationNodes = fillAnimationNodesGfb(rawAnimation);
        this.animationDuration = findLastKeyTime();


        for (var animationNode : animationNodes) {
            if (animationNode != null) {
                if (animationNode.positionKeys.getAtTime((int) animationDuration - 10) == null)
                    animationNode.positionKeys.add(animationDuration, animationNode.positionKeys.get(0).value());
                if (animationNode.rotationKeys.getAtTime((int) animationDuration - 10) == null)
                    animationNode.rotationKeys.add(animationDuration, animationNode.rotationKeys.get(0).value());
                if (animationNode.scaleKeys.getAtTime((int) animationDuration - 10) == null)
                    animationNode.scaleKeys.add(animationDuration, animationNode.scaleKeys.get(0).value());
            }
        }

        animationModifier.accept(this, "gfb");
    }

    public Animation(String name, SkeletonBlock smdFile, Skeleton bones, int speed) {
        this.name = name;
        this.ticksPerSecond = speed;
        this.skeleton = bones;
        this.animationNodes = fillAnimationNodesSmdx(smdFile.keyframes);
        this.animationDuration = findLastKeyTime();

        animationModifier.accept(this, "smd");
    }

    private static Vector3f convertArrayToVector3f(float[] array) {
        return new Vector3f().set(array);
    }

    private static Quaternionf convertArrayToQuaterionf(float[] array) {
        return new Quaternionf().set(array[0], array[1], array[2], array[3]);
    }

    private double findLastKeyTime() {
        var duration = 0d;

        for (var value : this.animationNodes) {
            if (value != null)
                for (var key : value.positionKeys) duration = Math.max(key.time(), duration);
        }

        return duration;
    }

    public float getAnimationTime(double secondsPassed) {
        var ticksPassed = (float) secondsPassed * 400;
        return (float) (ticksPassed % animationDuration);
    }

    public Matrix4f[] getFrameTransform(AnimationInstance instance) {
        var boneTransforms = new Matrix4f[this.skeleton.boneArray.length];
        readNodeHierarchy(instance.getCurrentTime(), skeleton.rootNode, new Matrix4f().identity(), boneTransforms);
        return boneTransforms;
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
        var bone = skeleton.get(name);

        if (node.id != -1) {
            var animNode = animationNodes[node.id];

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
        if (bone != null) {
            int transformId = skeleton.getId(bone);
            if (Float.isNaN(globalTransform.m00()))
                globalTransform = parentTransform.mul(bone.lastSuccessfulTransform, new Matrix4f());

            boneTransforms[transformId] = globalTransform.mul(bone.inversePoseMatrix, new Matrix4f());
        }

        for (var child : node.children)
            readNodeHierarchy(animTime, child, globalTransform, boneTransforms);
    }

    private AnimationNode[] fillAnimationNodesGlb(List<AnimationModel.Channel> channels) {
        var animationNodes = new AnimationNode[channels.size()];

        for (var channel : channels) {
            var node = channel.getNodeModel();
            animationNodes[nodeIdMap.computeIfAbsent(node.getName(), this::newNode)] = new AnimationNode(channels.stream().filter(c -> c.getNodeModel().equals(node)).toList(), node);
        }

        return animationNodes;
    }

    private AnimationNode[] fillAnimationNodesGfb(gg.generations.pokeutils.tranm.Animation rawAnimation) {
        var animationNodes = new AnimationNode[skeleton.boneMap.size()]; // BoneGroup

        int trueIndex = -1;

        for (int i = 0; i < rawAnimation.anim().bonesVector().length(); i++) {
            var boneAnim = rawAnimation.anim().bonesVector().get(i);
            if(!skeleton.boneMap.containsKey(boneAnim.name())) {
                continue;
            }

            trueIndex++;

            nodeIdMap.put(Objects.requireNonNull(boneAnim.name()).replace(".trmdl", ""), trueIndex);
            System.out.println(trueIndex);

            var node = animationNodes[trueIndex] = new AnimationNode();

            switch (boneAnim.rotType()) {
                case QuatTrack.DynamicQuatTrack ->
                        TranmUtil.processDynamicQuatTrack((DynamicQuatTrack) Objects.requireNonNull(boneAnim.rot(new DynamicQuatTrack())), node.rotationKeys);
                case QuatTrack.FixedQuatTrack ->
                        TranmUtil.processFixedQuatTrack((FixedQuatTrack) Objects.requireNonNull(boneAnim.rot(new FixedQuatTrack())), node.rotationKeys);
                case QuatTrack.Framed8QuatTrack ->
                        TranmUtil.processFramed8QuatTrack((Framed8QuatTrack) Objects.requireNonNull(boneAnim.rot(new Framed8QuatTrack())), node.rotationKeys);
                case QuatTrack.Framed16QuatTrack ->
                        TranmUtil.processFramed16QuatTrack((Framed16QuatTrack) Objects.requireNonNull(boneAnim.rot(new Framed16QuatTrack())), node.rotationKeys);
            }

            switch (boneAnim.scaleType()) {
                case VectorTrack.DynamicVectorTrack ->
                        TranmUtil.processDynamicVecTrack((DynamicVectorTrack) Objects.requireNonNull(boneAnim.scale(new DynamicVectorTrack())), node.scaleKeys);
                case VectorTrack.FixedVectorTrack ->
                        TranmUtil.processFixedVecTrack((FixedVectorTrack) Objects.requireNonNull(boneAnim.scale(new FixedVectorTrack())), node.scaleKeys);
                case VectorTrack.Framed8VectorTrack ->
                        TranmUtil.processFramed8VecTrack((Framed8VectorTrack) Objects.requireNonNull(boneAnim.scale(new Framed8VectorTrack())), node.scaleKeys);
                case VectorTrack.Framed16VectorTrack ->
                        TranmUtil.processFramed16VecTrack((Framed16VectorTrack) Objects.requireNonNull(boneAnim.scale(new Framed16VectorTrack())), node.scaleKeys);
            }

            switch (boneAnim.transType()) {
                case VectorTrack.DynamicVectorTrack ->
                        TranmUtil.processDynamicVecTrack((DynamicVectorTrack) Objects.requireNonNull(boneAnim.trans(new DynamicVectorTrack())), node.positionKeys);
                case VectorTrack.FixedVectorTrack ->
                        TranmUtil.processFixedVecTrack((FixedVectorTrack) Objects.requireNonNull(boneAnim.trans(new FixedVectorTrack())), node.positionKeys);
                case VectorTrack.Framed8VectorTrack ->
                        TranmUtil.processFramed8VecTrack((Framed8VectorTrack) Objects.requireNonNull(boneAnim.trans(new Framed8VectorTrack())), node.positionKeys);
                case VectorTrack.Framed16VectorTrack ->
                        TranmUtil.processFramed16VecTrack((Framed16VectorTrack) Objects.requireNonNull(boneAnim.trans(new Framed16VectorTrack())), node.positionKeys);
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

    @Override
    public String toString() {
        return this.name;
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
