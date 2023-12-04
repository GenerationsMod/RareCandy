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
import java.util.stream.Stream;

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

    public Animation(String name, int ticksPerSecond, Skeleton skeleton, T item) {
        this.name = name;
        this.ticksPerSecond = ticksPerSecond/400f;
        this.skeleton = skeleton;
        this.animationNodes = fillAnimationNodes(item);
        this.animationDuration = findLastKeyTime();
    }

    abstract AnimationNode[] fillAnimationNodes(T item);

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
        var ticksPassed = (float) secondsPassed * 400 * 0.5f;
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

//                nodeTransform.get(matrix);
//
//                for (int i = 0; i < 16; i++) {
//                    if(Float.isNaN(matrix[i])) {
//                        System.out.println("oh no!");
//                    }
//                }

                if (bone != null) {
                    if (this.isNaN(nodeTransform)) {
                        bone.lastSuccessfulTransform = new Matrix4f(nodeTransform);
                    } else {
                        System.out.println(bone.name + " " + "OH no!");
                    }
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
        return !Float.isNaN(nodeTransform.m00())
                && !Float.isNaN(nodeTransform.m01())
                && !Float.isNaN(nodeTransform.m02())
                && !Float.isNaN(nodeTransform.m03())
                && !Float.isNaN(nodeTransform.m00())
                && !Float.isNaN(nodeTransform.m01())
                && !Float.isNaN(nodeTransform.m02())
                && !Float.isNaN(nodeTransform.m03())
                && !Float.isNaN(nodeTransform.m00())
                && !Float.isNaN(nodeTransform.m01())
                && !Float.isNaN(nodeTransform.m02())
                && !Float.isNaN(nodeTransform.m03())
                && !Float.isNaN(nodeTransform.m00())
                && !Float.isNaN(nodeTransform.m01())
                && !Float.isNaN(nodeTransform.m02())
                && !Float.isNaN(nodeTransform.m03());
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
}
