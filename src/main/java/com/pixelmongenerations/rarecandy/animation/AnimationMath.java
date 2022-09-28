package com.pixelmongenerations.rarecandy.animation;

import com.pixelmongenerations.rarecandy.Pair;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class AnimationMath {

    public static Vector3f calcInterpolatedPosition(float AnimationTime, AnimationStorage.AnimationNode node) {
        if (node.positionKeys.size() == 1) return node.getDefaultPosition().value();

        var positions = findPositions(AnimationTime, node);
        var deltaTime = (float) (positions.b().time() - positions.a().time());
        var factor = (float) positions.a().time() / deltaTime;
        assert (factor >= 0.0f && factor <= 1.0f);
        var start = new Vector3f(positions.a().value());
        var end = new Vector3f(positions.b().value());
        var delta = new Vector3f(end.sub(start));
        return new Vector3f(start.add(delta.mul(factor)));
    }

    public static Quaternionf calcInterpolatedRotation(float animTime, AnimationStorage.AnimationNode node) {
        if (node.rotationKeys.size() == 1) return new Quaternionf(node.getDefaultRotation().value());

        var rotations = findRotations(animTime, node);
        var deltaTime = (float) (rotations.b().time() - rotations.a().time());
        var factor = (float) rotations.a().time() / deltaTime;
        assert (factor >= 0.0f && factor <= 1.0f);
        var start = new Quaternionf(rotations.a().value());
        var end = new Quaternionf(rotations.b().value());
        return new Quaternionf(start.slerp(end, factor));
    }

    public static Vector3f calcInterpolatedScaling(float animTime, AnimationStorage.AnimationNode node) {
        if (node.scaleKeys.size() == 1) return node.getDefaultScale().value();

        // FIXME: completely broken
        var out = new Vector3f(1, 1, 1);
        var scalings = findScalings(animTime, node);
        var deltaTime = (float) (scalings.b().time() - scalings.a().time());
        var factor = (float) scalings.a().time() / deltaTime;
        assert (factor >= 0.0f && factor <= 1.0f);
        var start = new Vector3f(scalings.a().value());
        var end = new Vector3f(scalings.b().value());
        var delta = new Vector3f(end.sub(start));
        return out;
    }

    public static Pair<Storage.TimeKey<Vector3f>, Storage.TimeKey<Vector3f>> findPositions(float animTime, AnimationStorage.AnimationNode node) {
        Storage.TimeKey<Vector3f> lastValue = null;
        for (var positionKeyTime : node.positionKeys) {
            if (animTime < positionKeyTime.time()) {
                return new Pair<>(lastValue, positionKeyTime);
            }

            lastValue = positionKeyTime;
        }

        lastValue = null;
        for (var positionKey : node.scaleKeys) {
            if (lastValue != null) {
                return new Pair<>(lastValue, positionKey);
            } else {
                lastValue = positionKey;
            }
        }

        throw new RuntimeException("Unreachable");
    }

    public static Pair<Storage.TimeKey<Quaternionf>, Storage.TimeKey<Quaternionf>> findRotations(float animTime, AnimationStorage.AnimationNode node) {
        Storage.TimeKey<Quaternionf> lastValue = null;
        for (var rotationKey : node.rotationKeys) {
            if (animTime < rotationKey.time()) {
                return new Pair<>(lastValue, rotationKey);
            }

            lastValue = rotationKey;
        }

        lastValue = null;
        for (var rotationKey : node.rotationKeys) {
            if (lastValue != null) {
                return new Pair<>(lastValue, rotationKey);
            } else {
                lastValue = rotationKey;
            }
        }

        throw new RuntimeException("Unreachable");
    }

    public static Pair<Storage.TimeKey<Vector3f>, Storage.TimeKey<Vector3f>> findScalings(float animTime, AnimationStorage.AnimationNode node) {
        Storage.TimeKey<Vector3f> lastValue = null;
        for (var scaleKey : node.scaleKeys) {
            if (animTime < scaleKey.time()) {
                return new Pair<>(lastValue, scaleKey);
            }

            lastValue = scaleKey;
        }

        lastValue = null;
        for (var scaleKey : node.scaleKeys) {
            if (lastValue != null) {
                return new Pair<>(lastValue, scaleKey);
            } else {
                lastValue = scaleKey;
            }
        }

        throw new RuntimeException("Unreachable");
    }
}
