package gg.generations.rarecandy.renderer.animation;

import gg.generations.rarecandy.pokeutils.Pair;
import gg.generations.rarecandy.pokeutils.tracm.TRACM;
import gg.generations.rarecandy.pokeutils.tranm.*;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

public class TranmAnimation extends Animation<Pair<gg.generations.rarecandy.pokeutils.tranm.Animation, TRACM>> {
    public TranmAnimation(String name, Pair<gg.generations.rarecandy.pokeutils.tranm.Animation, TRACM> rawAnimation, Skeleton skeleton) {
        super(name, (int) getFps(rawAnimation), skeleton, rawAnimation, TranmAnimation::fillAnimationNodes, TranmAnimation::fillTrOffsets);

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

        animationModifier.accept(this, "gfb");
    }

    private static long getFps(Pair<gg.generations.rarecandy.pokeutils.tranm.Animation, TRACM> rawAnimation) {
        if(rawAnimation.a() != null) {
            return rawAnimation.a().meta().fps();
        } else if(rawAnimation.b() != null) {
            return rawAnimation.b().config().framerate();
        } else {
            return 0L;
        }
    }

    public static <T> AnimationNode[] fillAnimationNodes(Animation<Pair<gg.generations.rarecandy.pokeutils.tranm.Animation, TRACM>> animation, Pair<gg.generations.rarecandy.pokeutils.tranm.Animation, TRACM> animationPair) {
        var rawAnimation = animationPair.a();

        if (rawAnimation != null) {

            var animationNodes = new AnimationNode[rawAnimation.anim().bonesLength()]; // BoneGroup

            for (int i = 0; i < rawAnimation.anim().bonesVector().length(); i++) {
                var boneAnim = rawAnimation.anim().bonesVector().get(i);

                var node = animationNodes[animation.nodeIdMap.computeIfAbsent(boneAnim.name().replace(".trmdl", ""), animation::newNode)] = new AnimationNode();


                switch (boneAnim.rotType()) {
                    case QuatTrack.DynamicQuatTrack -> TranmUtil.processDynamicQuatTrack((DynamicQuatTrack) Objects.requireNonNull(boneAnim.rot(new DynamicQuatTrack())), node.rotationKeys);
                    case QuatTrack.FixedQuatTrack -> TranmUtil.processFixedQuatTrack((FixedQuatTrack) Objects.requireNonNull(boneAnim.rot(new FixedQuatTrack())), node.rotationKeys);
                    case QuatTrack.Framed8QuatTrack -> TranmUtil.processFramed8QuatTrack((Framed8QuatTrack) Objects.requireNonNull(boneAnim.rot(new Framed8QuatTrack())), node.rotationKeys);
                    case QuatTrack.Framed16QuatTrack -> TranmUtil.processFramed16QuatTrack((Framed16QuatTrack) Objects.requireNonNull(boneAnim.rot(new Framed16QuatTrack())), node.rotationKeys);
                }

                switch (boneAnim.scaleType()) {
                    case VectorTrack.DynamicVectorTrack -> TranmUtil.processDynamicVecTrack((DynamicVectorTrack) Objects.requireNonNull(boneAnim.scale(new DynamicVectorTrack())), node.scaleKeys);
                    case VectorTrack.FixedVectorTrack -> TranmUtil.processFixedVecTrack((FixedVectorTrack) Objects.requireNonNull(boneAnim.scale(new FixedVectorTrack())), node.scaleKeys);
                    case VectorTrack.Framed8VectorTrack -> TranmUtil.processFramed8VecTrack((Framed8VectorTrack) Objects.requireNonNull(boneAnim.scale(new Framed8VectorTrack())), node.scaleKeys);
                    case VectorTrack.Framed16VectorTrack -> TranmUtil.processFramed16VecTrack((Framed16VectorTrack) Objects.requireNonNull(boneAnim.scale(new Framed16VectorTrack())), node.scaleKeys);
                }

                if (!Objects.requireNonNull(boneAnim.name()).equalsIgnoreCase("origin")) {
                    switch (boneAnim.transType()) {
                        case VectorTrack.DynamicVectorTrack -> TranmUtil.processDynamicVecTrack((DynamicVectorTrack) Objects.requireNonNull(boneAnim.trans(new DynamicVectorTrack())), node.positionKeys);
                        case VectorTrack.FixedVectorTrack -> TranmUtil.processFixedVecTrack((FixedVectorTrack) Objects.requireNonNull(boneAnim.trans(new FixedVectorTrack())), node.positionKeys);
                        case VectorTrack.Framed8VectorTrack -> TranmUtil.processFramed8VecTrack((Framed8VectorTrack) Objects.requireNonNull(boneAnim.trans(new Framed8VectorTrack())), node.positionKeys);
                        case VectorTrack.Framed16VectorTrack -> TranmUtil.processFramed16VecTrack((Framed16VectorTrack) Objects.requireNonNull(boneAnim.trans(new Framed16VectorTrack())), node.positionKeys);
                    }
                } else {
                    node.positionKeys.add(0, new Vector3f(0, 0, 0));
                }
            }

            return animationNodes;
        } else {
            return new AnimationNode[0];
        }
    }

    public static Map<String, Offset> fillTrOffsets(Pair<gg.generations.rarecandy.pokeutils.tranm.Animation, TRACM> animationPair) {
        var offsets = new HashMap<String, Offset>();

        if(animationPair.b() != null) {
            System.out.println();
        }

        return offsets;
    }

}
