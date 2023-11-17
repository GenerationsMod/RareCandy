package gg.generations.rarecandy.renderer.animation;

import gg.generations.rarecandy.pokeutils.GFLib.Anim.AnimationT;
import gg.generations.rarecandy.pokeutils.GFLib.Anim.ShaderEntryT;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GfbAnimation extends Animation<AnimationT> {
    public TransformStorage<Float> eyeOffsetU;
    public TransformStorage<Float> eyeOffsetV;

    public GfbAnimation(String name, AnimationT rawAnimation, Skeleton skeleton) {
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

        if(rawAnimation.getMaterial() != null) {
            var material = rawAnimation.getMaterial();

            var map = Arrays.stream(material.getTracks()).filter(a -> a.getName().equals("Eye")).flatMap(a -> Stream.of(a.getValues())).collect(Collectors.toMap(ShaderEntryT::getName, ShaderEntryT::getValue));

            var uOffset = map.get("ColorUVTranslateU");
            var vOffset = map.get("ColorUVTranslateV");

            eyeOffsetU = new TransformStorage<Float>();
            eyeOffsetV = new TransformStorage<Float>();

            if(uOffset != null) {
                uOffset.getValue().process(eyeOffsetU);
            } else {
                eyeOffsetU.add(0.0, 0.0f);
            }

            if(vOffset != null) {
                vOffset.getValue().process(eyeOffsetV);
            } else {
                eyeOffsetV.add(0.0, 0.0f);
            }
        }

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
