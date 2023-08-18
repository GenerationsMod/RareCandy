package gg.generations.rarecandy.legacy.animation;

import gg.generations.pokeutils.Pair;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class AnimationMath {

    public static Vector3f calcInterpolatedPosition(float animTime, Animation.AnimationNode node) {
        if (node.positionKeys.size() == 1) return node.getDefaultPosition().value();

        var positions = findPositions(animTime, node);
        var deltaTime = (float) (positions.b().time() - positions.a().time());
        var factor = (animTime - (float) positions.a().time()) / deltaTime;
        var start = new Vector3f(positions.a().value());
        var end = new Vector3f(positions.b().value());
        var delta = new Vector3f(end.sub(start));
        return new Vector3f(start.add(delta.mul(factor)));
    }

    public static Quaternionf calcInterpolatedRotation(float animTime, Animation.AnimationNode node) {
        if (node.rotationKeys.size() == 1) return new Quaternionf(node.getDefaultRotation().value());

        var rotations = findRotations(animTime, node);
        var deltaTime = (float) (rotations.b().time() - rotations.a().time());
        var factor = (animTime - (float) rotations.a().time()) / deltaTime;
        var start = new Quaternionf(rotations.a().value());
        var end = new Quaternionf(rotations.b().value());
        return new Quaternionf(start.slerp(end, factor));
    }

    public static Vector3f calcInterpolatedScaling(float animTime, Animation.AnimationNode node) {
        if (node.scaleKeys.size() == 1) return node.getDefaultScale().value();

        var out = new Vector3f();
        var scalings = findScalings(animTime, node);
        var deltaTime = (float) (scalings.b().time() - scalings.a().time());
        var factor = (animTime - (float) scalings.a().time()) / deltaTime;
        var start = new Vector3f(scalings.a().value());
        var end = new Vector3f(scalings.b().value());
        var delta = new Vector3f(end.sub(start));
        return out.add(start.add(delta.mul(factor)));
    }

    public static Pair<TransformStorage.TimeKey<Vector3f>, TransformStorage.TimeKey<Vector3f>> findPositions(float animTime, Animation.AnimationNode node) {
        for (var key : node.positionKeys) {
            if (animTime < key.time())
                return new Pair<>(node.positionKeys.getBefore(key), key);
        }

        return new Pair<>(node.positionKeys.get(0), node.positionKeys.get(1));
    }

    public static Pair<TransformStorage.TimeKey<Quaternionf>, TransformStorage.TimeKey<Quaternionf>> findRotations(float animTime, Animation.AnimationNode node) {
        for (var key : node.rotationKeys) {
            if (animTime < key.time())
                return new Pair<>(node.rotationKeys.getBefore(key), key);
        }

        return new Pair<>(node.rotationKeys.get(0), node.rotationKeys.get(1));
    }

    public static Pair<TransformStorage.TimeKey<Vector3f>, TransformStorage.TimeKey<Vector3f>> findScalings(float animTime, Animation.AnimationNode node) {
        for (var key : node.scaleKeys) {
            if (animTime < key.time())
                return new Pair<>(node.scaleKeys.getBefore(key), key);
        }

        return new Pair<>(node.scaleKeys.get(0), node.scaleKeys.get(1));
    }
}
