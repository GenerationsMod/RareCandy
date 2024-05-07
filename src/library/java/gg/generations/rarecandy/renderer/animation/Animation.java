package gg.generations.rarecandy.renderer.animation;

import gg.generations.rarecandy.pokeutils.ModelNode;
import gg.generations.rarecandy.renderer.loading.ModelLoader;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Animation {
    public static final int FPS_60 = 1000;
    public static final int FPS_24 = 400;
    public static final int GLB_SPEED = 30;
    public static BiConsumer<Animation, String> animationModifier = (animation, s) -> {
    };
    protected static Vector3f TRANSLATE = new Vector3f();
    protected static Vector3f SCALE = new Vector3f(1, 1, 1);
    protected static Vector3f TRANSLATION = new Vector3f();
    public final String name;
    public final double animationDuration;
    protected final Skeleton skeleton;
    public Map<String, Integer> nodeIdMap = new HashMap<>();

    private final AnimationNode[] animationNodes;
    public Map<String, Offset> offsets;

    public float ticksPerSecond;
    public boolean ignoreInstancedTime = false;

    private boolean ignoreScaling;

    public Animation(String name, int ticksPerSecond, Skeleton skeleton, AnimationNode[] animationNodes, Map<String, Offset> offsets, boolean ignoreScaling) {
        this.name = name;
        this.ticksPerSecond = ticksPerSecond;
        this.skeleton = skeleton;
        this.animationNodes = animationNodes;

        this.offsets = offsets;
        this.animationDuration = findLastKeyTime();
        this.ignoreScaling = ignoreScaling;

        if(this.animationNodes != null) {
            for (var animationNode : getAnimationNodes()) {
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
    }
    public Animation(String name, int ticksPerSecond, Skeleton skeleton, ModelLoader.NodeProvider animationNodes, Map<String, Offset> offsets, boolean ignoreScaling) {
        this.name = name;
        this.ticksPerSecond = ticksPerSecond;
        this.skeleton = skeleton;
        this.animationNodes = animationNodes.getNode(this, skeleton);

        this.offsets = offsets;
        this.animationDuration = findLastKeyTime();
        this.ignoreScaling = ignoreScaling;

        if(this.animationNodes != null) {
            for (var animationNode : getAnimationNodes()) {
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
    }

    public static Animation fromBuffer(ByteBuffer buffer, Skeleton skeleton) {
        var name = ModelNode.extractName(buffer);

        var tps = buffer.get();

        var nodesLength = buffer.getShort();

        var animationNodes = new AnimationNode[nodesLength];

        for (int i = 0; i < nodesLength; i++) {
            animationNodes[i] = new AnimationNode(buffer);
        }

        var idMap = generateNodeIdMap(buffer);

        var offsets = getOffsets(buffer);


    }

    private static Map<String, Offset> getOffsets(ByteBuffer buffer) {
        var length = buffer.get();


    }

    private static Map<String, Integer> generateNodeIdMap(ByteBuffer buffer) {
        var length = buffer.get();

        var map = new HashMap<String, Integer>();

        for (int i = 0; i < length; i++) {
            var key = ModelNode.extractName(buffer);
            var value = buffer.getInt();

            map.put(key, value);
        }

        return map;
    }

    public static <T> Map<String, Offset> fillOffsets(T item) {
        return new HashMap<>();
    }

    private double findLastKeyTime() {
        var duration = 0d;

        if(animationNodes != null) {

            for (var value : this.getAnimationNodes()) {
                if (value != null) {
                    for (var key : value.positionKeys) duration = Math.max(key.time(), duration);
                    for (var key : value.rotationKeys) duration = Math.max(key.time(), duration);
                    for (var key : value.scaleKeys) duration = Math.max(key.time(), duration);
                }
            }
        }

        if(duration == 0) {
            for (var value : this.offsets.values()) {
                if (value != null) {
                    duration = Math.max(value.duration(), duration);
                }
            }
        }

        return duration;
    }

    public float getAnimationTime(double secondsPassed) {
        var ticksPassed = (float) secondsPassed * (ticksPerSecond);
            return (float) (ticksPassed % animationDuration);
    }

    public Matrix4f[] getFrameTransform(AnimationInstance instance) {
        var boneTransforms = new Matrix4f[this.skeleton.bones.length];
        readNodeHierarchy(instance.getCurrentTime(), skeleton.rootNode, new Matrix4f().identity(), boneTransforms);
        return boneTransforms;
    }

    public void getFrameOffset(AnimationInstance instance) {
        this.offsets.forEach((k, v) -> {
            var offsetInstance = instance.offsets.computeIfAbsent(k, a -> new Transform());
            offsetInstance.offset().zero();
            offsetInstance.scale().set(1, 1);

            offsets.get(k).calcOffset(instance.getCurrentTime(), offsetInstance);
        });
    }

    public Matrix4f[] getFrameTransform(double secondsPassed) {
        var boneTransforms = new Matrix4f[this.skeleton.bones.length];
        readNodeHierarchy(getAnimationTime(secondsPassed), skeleton.rootNode, new Matrix4f().identity(), boneTransforms);
        return boneTransforms;
    }

    private static final Matrix4f matrix = new Matrix4f();

    public void readNodeHierarchy(float animTime, ModelNode node, Matrix4f parentTransform, Matrix4f[] boneTransforms) {
        var name = node.name;
        var nodeTransform = matrix.set(node.transform);

        var animationNodeId = nodeIdMap.getOrDefault(name, -1);
        var bone = skeleton.get(name);

        if (animationNodeId != -1) {
            var animNode = animationNodes[animationNodeId];

            if (animNode != null) {
                var scale = ignoreScaling ? SCALE : AnimationMath.calcInterpolatedScaling(animTime, animNode);
                var rotation = AnimationMath.calcInterpolatedRotation(animTime, animNode);
                var translation = name.equalsIgnoreCase("origin") ? TRANSLATION : AnimationMath.calcInterpolatedPosition(animTime, animNode);
                nodeTransform.identity().translationRotateScale(translation, rotation, scale);

//                if (bone != null && !this.isNaN(nodeTransform)) {
//                    bone.lastSuccessfulTransform = nodeTransform.set(nodeTransform);
//                }
            }
        }

        var globalTransform = parentTransform.mul(nodeTransform, new Matrix4f());

        if (bone != null) {

//            if (isNaN(globalTransform)) {
//                globalTransform = parentTransform.mul(bone.lastSuccessfulTransform, new Matrix4f());
//            }

            boneTransforms[skeleton.getId(bone)] = globalTransform.mul(bone.inverseBindMatrix, new Matrix4f());
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

        public static Vector3f fromBuffer(ByteBuffer buffer) {
            return new Vector3f(buffer.getFloat(), buffer.getFloat(), buffer.getFloat());
        }

        public static void toBuffer(ByteBuffer buffer, Vector3f vector3f) {
            buffer.putFloat(vector3f.x()).putFloat(vector3f.y()).putFloat(vector3f.z());
        }

        public static Quaternionf quatFromBuffer(ByteBuffer buffer) {
            float scaleFactor = 127.5f;

            float x = buffer.getShort() / scaleFactor;
            float y = buffer.getShort() / scaleFactor;
            float z = buffer.getShort() / scaleFactor;
            float w = buffer.getShort() / scaleFactor;


            return new Quaternionf(x, y, z, w);
        }

        public static void quatToBuffer(ByteBuffer buffer, Quaternionf quaternion) {
            float scaleFactor = 127.5f;

            short x = (short) Math.round(quaternion.x() * scaleFactor);
            short y = (short) Math.round(quaternion.y() * scaleFactor);
            short z = (short) Math.round(quaternion.z() * scaleFactor);
            short w = (short) Math.round(quaternion.w() * scaleFactor);

            buffer.putShort(x).putShort(y).putShort(z).putShort(w);
        }

        public AnimationNode(ByteBuffer buffer) {
            positionKeys.fromBuffer(buffer, AnimationNode::fromBuffer);
            rotationKeys.fromBuffer(buffer, AnimationNode::quatFromBuffer);
            scaleKeys.fromBuffer(buffer, AnimationNode::fromBuffer);
        }

        public void fillBuffer(ByteBuffer buffer) {
            positionKeys.fillByteBuffer(buffer, AnimationNode::toBuffer);
            rotationKeys.fillByteBuffer(buffer, AnimationNode::quatToBuffer);
            scaleKeys.fillByteBuffer(buffer, AnimationNode::toBuffer);
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

        public void fillBuffer(ByteBuffer buffer) {
            buffer.putFloat(duration);
            uOffset.fillByteBuffer(buffer, ByteBuffer::putFloat);
            vOffset.fillByteBuffer(buffer, ByteBuffer::putFloat);
            uScale.fillByteBuffer(buffer, ByteBuffer::putFloat);
            vScale.fillByteBuffer(buffer, ByteBuffer::putFloat);
        }

        public static Offset fromBuffer(ByteBuffer buffer) {
            var duration = buffer.getFloat();

            var uOffset = new TransformStorage<Float>().fromBuffer(buffer, ByteBuffer::getFloat);

            var vOffset = new TransformStorage<Float>().fromBuffer(buffer, ByteBuffer::getFloat);

            var uScale = new TransformStorage<Float>().fromBuffer(buffer, ByteBuffer::getFloat);

            var vScale = new TransformStorage<Float>().fromBuffer(buffer, ByteBuffer::getFloat);

            return new Offset(uOffset, vOffset, uScale, vScale, duration);
        }
    }
}


