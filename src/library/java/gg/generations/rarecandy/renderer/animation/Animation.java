package gg.generations.rarecandy.renderer.animation;

import gg.generations.rarecandy.pokeutils.ModelNode;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class Animation<T> {
    public static final int FPS_60 = 1000;
    public static final int FPS_24 = 400;
    public static final int GLB_SPEED = 30;
    public static BiConsumer<Animation, String> animationModifier = (animation, s) -> {
    };
    public final String name;
    public final double animationDuration;
    protected final Skeleton skeleton;
    public Map<String, Integer> nodeIdMap = new HashMap<>();

    private final AnimationNode[] animationNodes;
    public Map<String, Offset> offsets;

    public float ticksPerSecond;
    public boolean ignoreInstancedTime = false;

    public Animation(String name, int ticksPerSecond, Skeleton skeleton, T value, BiFunction<Animation<T>, T, AnimationNode[]> animationNodes, Function<T, Map<String, Offset>> offsets) {
        this.name = name;
        this.ticksPerSecond = ticksPerSecond;
        this.skeleton = skeleton;
        this.animationNodes = animationNodes.apply(this, value);
        this.offsets = offsets.apply(value);
        this.animationDuration = findLastKeyTime();

//        var boneList = List.of(this.skeleton.boneArray);

//        StringBuilder builder = new StringBuilder();
//
//        builder.append("version 1\n");
//        builder.append("nodes\n");
//
//        for (int i = 0; i < boneList.size(); i++) {
//            var bone = boneList.get(i);
//            builder.append("%s \"%s\" %s\n".formatted(i, bone.name, bone.parent));
//        }
//
//        builder.append("end\n");
//        builder.append("skeleton\n");
//
//        for (float i = 0; i < animationDuration; i++) {
//            builder.append("time " + i + "\n");
//
//            for (int j = 0; j < boneList.size(); j++) {
//                var bone = boneList.get(j);
//                var index = nodeIdMap.get(bone.name);
//
//                var position = bone.posePosition;
//                var rotation = bone.poseRotation;
//                var scale = bone.poseScale;
//
//                try {
//                    var animationNode = getAnimationNodes()[index];
//
//                    position = AnimationMath.calcInterpolatedPosition(i, animationNode);
//                    rotation = AnimationMath.calcInterpolatedRotation(i, animationNode);
//                    scale = AnimationMath.calcInterpolatedScaling(i, animationNode);
//                } catch (Exception e) {
//                }
//
//                var translate = position;
//                var rotate = rotation.getEulerAnglesZYX(new Vector3f());
//
//                builder.append("%s  %.6f %.6f %.6f  %.6f %.6f %.6f  %.6f %.6f %.6f\n".formatted(j, translate.x, translate.y, translate.z, rotate.x, rotate.y, rotate.z, scale.x, scale.y, scale.z));
//            }
//
//        }
//
//        builder.append("end");
//
//        try {
//            Files.writeString(Path.of(name + ".smd"), builder);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    public static <T> Map<String, GfbAnimation.Offset> fillOffsets(T item) {
        return new HashMap<>();
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

    private static final float[] matrix = new float[16];

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

                if (bone != null && this.isNaN(nodeTransform)) {
                    bone.lastSuccessfulTransform = new Matrix4f(nodeTransform);
                }
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
            if (isNaN(globalTransform)) {
                globalTransform = parentTransform.mul(bone.lastSuccessfulTransform, new Matrix4f());
            }

            boneTransforms[bone.jointId] = globalTransform.mul(bone.inversePoseMatrix, new Matrix4f());
        }

        for (var child : node.children)
            readNodeHierarchy(animTime, child, globalTransform, boneTransforms);
    }

    private boolean isNaN(Matrix4f nodeTransform) {
        return Float.isNaN(nodeTransform.m00());
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
}
