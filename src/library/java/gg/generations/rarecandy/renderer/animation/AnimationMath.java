package gg.generations.rarecandy.renderer.animation;

import gg.generations.rarecandy.pokeutils.Pair;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class AnimationMath {
    private static final Pair<TransformStorage.TimeKey<Quaternionf>, TransformStorage.TimeKey<Quaternionf>> QUATERIONF_KEYPAIR = new Pair<>(null, null);
    private static final Pair<TransformStorage.TimeKey<Vector3f>, TransformStorage.TimeKey<Vector3f>> VECTOR3F_KEYPAIR = new Pair<>(null, null);

    private static final Vector3f DEST_SCALE = new Vector3f();
    private static final Vector3f DEST_TRANSLATION = new Vector3f();
    private static final Quaternionf DEST_ROTATION = new Quaternionf();

    public static Vector3f calcInterpolatedPosition(float animTime, Animation.AnimationNode node) {
        if (node.positionKeys.size() == 1) return node.getDefaultPosition().value();

        var positions = findPositions(animTime, node);
        float factor = (float) ((animTime - (float) positions.a().time()) / (positions.b().time() - positions.a().time()));

        var start = positions.a().value();
        var end = positions.b().value();
        return DEST_TRANSLATION.set(end).sub(start).mul(factor).add(start);
    }

    public static Quaternionf calcInterpolatedRotation(float animTime, Animation.AnimationNode node) {
        if (node.rotationKeys.size() == 1) return new Quaternionf(node.getDefaultRotation().value());

        var rotations = findRotations(animTime, node);
        var deltaTime = (float) (rotations.b().time() - rotations.a().time());
        var factor = (animTime - (float) rotations.a().time()) / deltaTime;
        var start = rotations.a().value();
        var end = rotations.b().value();
        return DEST_ROTATION.set(start).slerp(end, factor);
    }

    public static Vector3f calcInterpolatedScaling(float animTime, Animation.AnimationNode node) {
        if (node.scaleKeys.size() == 1) return node.getDefaultScale().value();

        var scalings = findScalings(animTime, node);
        var deltaTime = (float) (scalings.b().time() - scalings.a().time());
        var factor = (animTime - (float) scalings.a().time()) / deltaTime;
        var start = scalings.a().value();
        var end = scalings.b().value();
        return DEST_SCALE.set(end).sub(start).mul(factor).add(start);
    }

    public static Pair<TransformStorage.TimeKey<Vector3f>, TransformStorage.TimeKey<Vector3f>> findPositions(float animTime, Animation.AnimationNode node) {
        for (var key : node.positionKeys) {
            if (animTime < key.time())
                return VECTOR3F_KEYPAIR.set(node.positionKeys.getBefore(key), key);
        }

        return VECTOR3F_KEYPAIR.set(node.positionKeys.get(0), node.positionKeys.get(1));
    }

    public static Pair<TransformStorage.TimeKey<Quaternionf>, TransformStorage.TimeKey<Quaternionf>> findRotations(float animTime, Animation.AnimationNode node) {
        for (var key : node.rotationKeys) {
            if (animTime < key.time())
                return QUATERIONF_KEYPAIR.set(node.rotationKeys.getBefore(key), key);
        }

        return QUATERIONF_KEYPAIR.set(node.rotationKeys.get(0), node.rotationKeys.get(1));
    }

    public static Pair<TransformStorage.TimeKey<Vector3f>, TransformStorage.TimeKey<Vector3f>> findScalings(float animTime, Animation.AnimationNode node) {
        for (var key : node.scaleKeys) {
            if (animTime < key.time())
                return VECTOR3F_KEYPAIR.set(node.scaleKeys.getBefore(key), key);
        }

        return VECTOR3F_KEYPAIR.set(node.scaleKeys.get(0), node.scaleKeys.get(1));
    }
}
