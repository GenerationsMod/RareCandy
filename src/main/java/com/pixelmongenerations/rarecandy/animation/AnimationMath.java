package com.pixelmongenerations.rarecandy.animation;

import com.pixelmongenerations.rarecandy.Pair;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class AnimationMath {

    public static Vector3f calcInterpolatedPosition(float AnimationTime, AnimationStorage.AnimationNode node) {
        if (node.positionKeys.size() == 1) {
            return node.getDefaultPosition();
        }

        var positions = findPositions(AnimationTime, node);
        var deltaTime = (float) (positions.b() - positions.a());
        var factor = (float) (double) positions.a() / deltaTime;
        var start = new Vector3f(node.positionKeys.get(positions.a()));
        var end = new Vector3f(node.positionKeys.get(positions.b()));
        var delta = new Vector3f(end.sub(start));
        return new Vector3f(start.add(delta.mul(factor)));
    }

    public static Quaternionf calcInterpolatedRotation(float animTime, AnimationStorage.AnimationNode node) {
        if (node.rotationKeys.size() == 1) {
            return new Quaternionf(node.getDefaultRotation());
        }

        var rotations = findRotations(animTime, node);
        var deltaTime = (float) (rotations.b() - rotations.a());
        var factor = (float) (double) rotations.a() / deltaTime;
        var start = new Quaternionf(node.rotationKeys.get(rotations.a()));
        var end = new Quaternionf(node.rotationKeys.get(rotations.b()));
        return new Quaternionf(start.slerp(end, factor));
    }

    public static Vector3f calcInterpolatedScaling(float animTime, AnimationStorage.AnimationNode node) {
        if (node.scaleKeys.size() == 1) return node.getDefaultScale();

        var out = new Vector3f(1, 1, 1);
        var scalings = findScalings(animTime, node);
        var deltaTime = (float) (scalings.b() - scalings.a());
        var factor = (float) (double) scalings.a() / deltaTime;
        var start = new Vector3f(node.positionKeys.get(scalings.a()));
        var end = new Vector3f(node.positionKeys.get(scalings.b()));
        var delta = new Vector3f(end.sub(start));
        return out.add(start.add(delta.mul(factor)));
    }

    public static Pair<Double, Double> findPositions(float animTime, AnimationStorage.AnimationNode node) {
        double lastValue = -1;
        for (Double aDouble : node.positionKeys.keySet()) {
            if (animTime < (float) (double) aDouble) {
                return new Pair<>(lastValue, aDouble);
            }

            lastValue = aDouble;
        }

        return new Pair<>(0d, 0d);
    }

    public static Pair<Double, Double> findRotations(float animTime, AnimationStorage.AnimationNode node) {
        double lastValue = -1;
        for (Double aDouble : node.rotationKeys.keySet()) {
            if (animTime < (float) (double) aDouble) {
                return new Pair<>(lastValue, aDouble);
            }

            lastValue = aDouble;
        }

        return new Pair<>(0d, 0d);
    }

    public static Pair<Double, Double> findScalings(float animTime, AnimationStorage.AnimationNode node) {
        double lastValue = -1;
        for (Double aDouble : node.scaleKeys.keySet()) {
            if (animTime < (float) (double) aDouble) {
                return new Pair<>(lastValue, aDouble);
            }

            lastValue = aDouble;
        }

        return new Pair<>(0d, 0d);
    }
}
