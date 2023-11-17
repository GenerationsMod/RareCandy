package gg.generations.rarecandy.renderer.animation;

import gg.generations.rarecandy.pokeutils.GFLib.Anim.AnimationT;

import java.util.Objects;

public class GfamnAnimation extends Animation<AnimationT> {
    public GfamnAnimation(String name, AnimationT rawAnimation, Skeleton skeleton) {
        super(name, FPS_60 - 95, skeleton, rawAnimation);

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

    public AnimationNode[] fillAnimationNodes(AnimationT rawAnimation) {
        var animationNodes = new AnimationNode[skeleton.boneMap.size()]; // BoneGroup

        if (rawAnimation.getSkeleton() != null) {
            int trueIndex = -1;

            for (int i = 0; i < rawAnimation.getSkeleton().getTracks().length; i++) {
                var boneAnim = rawAnimation.getSkeleton().getTracks()[i];
                if (!skeleton.boneMap.containsKey(boneAnim.getName())) {
                    continue;
                }

                trueIndex++;

                nodeIdMap.put(Objects.requireNonNull(boneAnim.getName()).replace(".trmdl", ""), trueIndex);

                var node = animationNodes[trueIndex] = new AnimationNode();

                boneAnim.getRotate().getValue().process(node.rotationKeys);
                boneAnim.getScale().getValue().process(node.scaleKeys);
                boneAnim.getTranslate().getValue().process(node.positionKeys);
            }

        }
        return animationNodes;
    }
}
