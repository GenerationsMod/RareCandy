package com.pixelmongenerations.rarecandy.animation;

import com.pixelmongenerations.pkl.assimp.AssimpUtils;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.assimp.AINodeAnim;

public class AnimationMath {

    public static void calcInterpolatedPosition(Vector3f Out, float AnimationTime, AINodeAnim pNodeAnim) {
        if (pNodeAnim.mNumPositionKeys() == 1) {
            Out.set(AssimpUtils.from(pNodeAnim.mPositionKeys().get(0).mValue()));
            return;
        }

        var PositionIndex = findPosition(AnimationTime, pNodeAnim);
        var NextPositionIndex = (PositionIndex + 1);
        var DeltaTime = (float) (pNodeAnim.mPositionKeys().get(NextPositionIndex).mTime() - pNodeAnim.mPositionKeys().get(PositionIndex).mTime());
        var Factor = (AnimationTime - (float) pNodeAnim.mPositionKeys().get(PositionIndex).mTime()) / DeltaTime;
        Vector3f Start = AssimpUtils.from(pNodeAnim.mPositionKeys().get(PositionIndex).mValue());
        Vector3f End = AssimpUtils.from(pNodeAnim.mPositionKeys().get(NextPositionIndex).mValue());
        Vector3f Delta = End.sub(Start);
        Out.set(Start.add(Delta.mul(Factor)));
    }

    public static void calcInterpolatedRotation(Quaternionf Out, float AnimationTime, AINodeAnim pNodeAnim) {
        if (pNodeAnim.mNumRotationKeys() == 1) {
            Out.set(AssimpUtils.from(pNodeAnim.mRotationKeys().get(0).mValue()));
            return;
        }

        int RotationIndex = findRotation(AnimationTime, pNodeAnim);
        int NextRotationIndex = (RotationIndex + 1);
        assert (NextRotationIndex < pNodeAnim.mNumRotationKeys());
        float DeltaTime = (float) (pNodeAnim.mRotationKeys().get(NextRotationIndex).mTime() - pNodeAnim.mRotationKeys().get(RotationIndex).mTime());
        float Factor = (AnimationTime - (float) pNodeAnim.mRotationKeys().get(RotationIndex).mTime()) / DeltaTime;
        assert (Factor >= 0.0f && Factor <= 1.0f);
        Quaternionf StartRotationQ = AssimpUtils.from(pNodeAnim.mRotationKeys().get(RotationIndex).mValue());
        Quaternionf EndRotationQ = AssimpUtils.from(pNodeAnim.mRotationKeys().get(NextRotationIndex).mValue());
        Out.set(StartRotationQ.slerp(EndRotationQ, Factor));
    }

    public static Vector3f calcInterpolatedScaling(Vector3f Out, float AnimationTime, AINodeAnim pNodeAnim) {
        if (pNodeAnim.mNumScalingKeys() == 1) return AssimpUtils.from(pNodeAnim.mScalingKeys().get(0).mValue());

        int ScalingIndex = findScaling(AnimationTime, pNodeAnim);
        int NextScalingIndex = (ScalingIndex + 1);
        assert (NextScalingIndex < pNodeAnim.mNumScalingKeys());
        float DeltaTime = (float) (pNodeAnim.mScalingKeys().get(NextScalingIndex).mTime() - pNodeAnim.mScalingKeys().get(ScalingIndex).mTime());
        float Factor = (AnimationTime - (float) pNodeAnim.mScalingKeys().get(ScalingIndex).mTime()) / DeltaTime;
        assert (Factor >= 0.0f && Factor <= 1.0f);
        Vector3f Start = AssimpUtils.from(pNodeAnim.mScalingKeys().get(ScalingIndex).mValue());
        Vector3f End = AssimpUtils.from(pNodeAnim.mScalingKeys().get(NextScalingIndex).mValue());
        Vector3f Delta = End.sub(Start);
        return Out.set(Start.add(Delta.mul(Factor)));
    }

    public static int findPosition(float AnimationTime, AINodeAnim pNodeAnim) {
        for (int i = 0; i < pNodeAnim.mNumPositionKeys() - 1; i++) {
            if (AnimationTime < (float) pNodeAnim.mPositionKeys().get(i + 1).mTime()) {
                return i;
            }
        }

        return 0;
    }

    public static int findRotation(float AnimationTime, AINodeAnim pNodeAnim) {
        for (int i = 0; i < pNodeAnim.mNumRotationKeys() - 1; i++) {
            if (AnimationTime < (float) pNodeAnim.mRotationKeys().get(i + 1).mTime()) {
                return i;
            }
        }

        return 0;
    }

    public static int findScaling(float AnimationTime, AINodeAnim pNodeAnim) {
        for (int i = 0; i < pNodeAnim.mNumScalingKeys() - 1; i++) {
            if (AnimationTime < (float) pNodeAnim.mScalingKeys().get(i + 1).mTime()) {
                return i;
            }
        }

        return 0;
    }
}
