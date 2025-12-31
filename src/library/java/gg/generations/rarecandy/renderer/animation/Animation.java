package gg.generations.rarecandy.renderer.animation;

import gg.generations.rarecandy.pokeutils.ModelNode;
import gg.generations.rarecandy.pokeutils.SkeletalTransform;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class Animation {
    public static final int FPS_60 = 1000;
    public static final int FPS_24 = 400;
    public static final int GLB_SPEED = 30;
    public static BiConsumer<Animation, String> animationModifier = (animation, s) -> {
    };
    public static Vector3f TRANSLATE = new Vector3f();
    protected static Vector3f SCALE = new Vector3f(1, 1, 1);
    protected static Vector3f TRANSLATION = new Vector3f();

    private static final Matrix4f[] MATRIX = new Matrix4f[220];
    private static final Matrix4fStack GLOBAL = new Matrix4fStack(220);
    private final static Matrix4f IDENTITY = new Matrix4f();
    private static final Matrix4f TEMP_GLOBAL_TRANSFORM = new Matrix4f();
    private static final Matrix4f TEMP_BONE_RESULT = new Matrix4f();
    private static final Vector3f TEMP_ORIGIN_VECTOR = new Vector3f();

    static {
        for (int i = 0; i < MATRIX.length; i++) {
            MATRIX[i] = new Matrix4f();
        }
    }

    public final int id;
    public final double animationDuration;
    protected final Skeleton skeleton;
    private final SkeletalTransform rootOffset;

    private final AnimationNode[] animationNodes;
    public Offset[] offsets;


    private Matrix4f[] cachedBoneTransforms;
    private final Matrix4f cachedIdentity = new Matrix4f().identity();

    public float ticksPerSecond;
    public boolean loops;
    public boolean ignoreInstancedTime = false;

    private boolean ignoreScaling;

    public Animation(int id, int ticksPerSecond, boolean loops, Skeleton skeleton, AnimationNode[] animationNodes, Offset[] offsets, boolean ignoreScaling, SkeletalTransform offset) {
        this.id = id;
        this.ticksPerSecond = ticksPerSecond;
        this.loops = loops;
        this.skeleton = skeleton;
        this.animationNodes = animationNodes;
        this.rootOffset = offset;

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
            for (var value : this.offsets) {
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

        if (cachedBoneTransforms == null || cachedBoneTransforms.length != skeleton.jointMap.size()) {
            cachedBoneTransforms = new Matrix4f[skeleton.jointMap.size()];
            for (int i = 0; i < cachedBoneTransforms.length; i++) {
                cachedBoneTransforms[i] = new Matrix4f();
            }
        }

        // Reset all transforms to identity before populating
        for (Matrix4f mat : cachedBoneTransforms) {
            mat.identity();
        }

        GLOBAL.identity();

        readNodeHierarchy(instance.getCurrentTime(), skeleton.rootNode, cachedBoneTransforms, false, 0);
        return cachedBoneTransforms;
    }

    public void getFrameOffset(AnimationInstance instance) {
        assert instance.offsets.length == offsets.length;

        for (int i = 0; i < offsets.length; i++) {
            var offsetInstance = instance.offsets[i];
            offsetInstance.offset().zero();
            offsetInstance.scale().set(1, 1);

            if(offsets[i] != null) offsets[i].calcOffset(instance.getCurrentTime(), offsetInstance);
        }
    }

    public Matrix4f[] getFrameTransform(double secondsPassed) {
        if (cachedBoneTransforms == null || cachedBoneTransforms.length != skeleton.jointMap.size()) {
            cachedBoneTransforms = new Matrix4f[skeleton.jointMap.size()];
            for (int i = 0; i < cachedBoneTransforms.length; i++) {
                cachedBoneTransforms[i] = new Matrix4f();
            }
        }

        for (Matrix4f mat : cachedBoneTransforms) {
            mat.identity();
        }

        readNodeHierarchy(getAnimationTime(secondsPassed), skeleton.rootNode, cachedBoneTransforms, false, 0);
        return cachedBoneTransforms;
    }

    public void readNodeHierarchy(float animTime, ModelNode node, Matrix4f[] boneTransforms, boolean offsetUsed, int depth) {


        var name = node.name;
        var nodeTransform = MATRIX[depth].set(node.transform);  // Reuses existing static 'matrix' field

        var animationNodeId = skeleton.boneIdMap.getOrDefault(name, -1);
        var bone = skeleton.get(name);

        if (animationNodeId != -1) {
            var animNode = animationNodes[animationNodeId];

            if (animNode != null) {
                var scale = ignoreScaling ? SCALE : AnimationMath.calcInterpolatedScaling(animTime, animNode);
                var rotation = AnimationMath.calcInterpolatedRotation(animTime, animNode);

                // Reuse pooled Vector3f for "origin" case
                Vector3f translation;
                if (name.equalsIgnoreCase("origin")) {
                    translation = TEMP_ORIGIN_VECTOR.set(0);
                } else {
                    translation = AnimationMath.calcInterpolatedPosition(animTime, animNode);
                }

                if (!offsetUsed) {
                    offsetUsed = true;
                    translation.add(rootOffset.position());
                    rotation.mul(rootOffset.rotation());
                }

                if(!isIdentityTransform(translation, scale, rotation, 1e-5f)) nodeTransform.identity().translationRotateScale(translation, rotation, scale);
            }
        }



        // Reuse pooled Matrix4f for globalTransform
        TEMP_GLOBAL_TRANSFORM.set(GLOBAL).mul(nodeTransform);

        if (bone != null && animationNodeId >= 0 && animationNodeId < boneTransforms.length) {
            // Write directly into pre-allocated array slot
            TEMP_GLOBAL_TRANSFORM.mul(bone.inverseBindMatrix, boneTransforms[animationNodeId]);
        }

        var nextDepth = depth + 1;

        GLOBAL.pushMatrix();
        GLOBAL.set(TEMP_GLOBAL_TRANSFORM);

        for (var child : node.children) {
            readNodeHierarchy(animTime, child, boneTransforms, offsetUsed, nextDepth);
        }
        GLOBAL.popMatrix();
    }

    private boolean isNaN(Matrix4f nodeTransform) {
        return Float.isNaN(nodeTransform.m00());
    }

//    @Override
//    public String toString() {
//        return this.name;
//    }

    public AnimationNode[] getAnimationNodes() {
        return animationNodes;
    }


    public static class AnimationNode {
        public final TransformStorage<Vector3f> positionKeys = new TransformStorage<>();
        public final TransformStorage<Quaternionf> rotationKeys = new TransformStorage<>();
        public final TransformStorage<Vector3f> scaleKeys = new TransformStorage<>();

        public AnimationNode() {
        }

        public static AnimationNode[] generateDefaults(Skeleton skeleton) {
            var animationNodes = new Animation.AnimationNode[skeleton.jointMap.size()];

            for (int i = 0; i < animationNodes.length; i++) {

                if(animationNodes[i] == null) {
                    var node = new Animation.AnimationNode();
                    var joint = skeleton.jointMap.get(skeleton.bones[i].name);

                    node.rotationKeys.add(0, joint.poseRotation);
                    node.rotationKeys.add(0, joint.poseRotation);
                    node.scaleKeys.add(0, joint.poseScale);

                }
            }

            return animationNodes;
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

    public static boolean isIdentityTransform(
            Vector3f translation,
            Vector3f scale,
            Quaternionf rotation,
            float eps
    ) {
        boolean tX = translation.x == 0.0f;
        boolean tY = translation.y == 0.0f;
        boolean tZ = translation.z == 0.0f;

        boolean sX = scale.x == 0.0f;
        boolean sY = scale.y == 0.0f;
        boolean sZ = scale.z == 0.0f;

        boolean rX = rotation.x == 0.0f;
        boolean rY = rotation.y == 0.0f;
        boolean rZ = rotation.z == 0.0f;
        boolean rW = rotation.w == 1.0f;

        return tX && tY && tZ
                && sX && sY && sZ
                && rX && rY && rZ && rW;
    }
}


