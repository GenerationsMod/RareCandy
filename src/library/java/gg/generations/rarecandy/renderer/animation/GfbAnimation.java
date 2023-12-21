package gg.generations.rarecandy.renderer.animation;

import gg.generations.rarecandy.pokeutils.GFLib.Anim.*;

import java.util.*;

import static java.util.stream.Stream.of;

public class GfbAnimation extends Animation<AnimationT> {

    public Map<String, Offset> offsets;

    public GfbAnimation(String name, AnimationT rawAnimation, Skeleton skeleton) {
        super(name, (int) rawAnimation.getInfo().getFrameRate(), skeleton, rawAnimation, GfbAnimation::fillAnimationNodes, GfbAnimation::fillGfbOffsets);

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

    public static Map<String, Offset> fillGfbOffsets(AnimationT rawAnimation) {
        var offsets = new HashMap<String, Offset>();

        if(rawAnimation.getMaterial() != null) {
            var material = rawAnimation.getMaterial();

            for(var track : material.getTracks()) {
                var trackName = track.getName();

                var uOffset = new TransformStorage<Float>();
                var vOffset = new TransformStorage<Float>();

                for(var entry : track.getValues()) {
                    if(entry.getName().equals("ColorUVTranslateU")) {
                        entry.getValue().getValue().process(uOffset);
                    } else if(entry.getName().equals("ColorUVTranslateV")) {
                        entry.getValue().getValue().process(vOffset);
                    }
                }

                if(uOffset.size() == 0) uOffset.add(0, 0f);
                if(vOffset.size() == 0) uOffset.add(0, 0f);

                

                offsets.put(trackName, new Offset(uOffset, vOffset));
            }
        }

        return offsets;
    }

    public static AnimationNode[] fillAnimationNodes(Animation<AnimationT> animation, AnimationT rawAnimation) {

        var animationNodes = new AnimationNode[rawAnimation.getSkeleton().getTracks().length];

        if (rawAnimation.getSkeleton() != null) {
            for (var track : rawAnimation.getSkeleton().getTracks()) {
                var node = animationNodes[animation.nodeIdMap.computeIfAbsent(track.getName(), animation::newNode)] = new AnimationNode();

                if(track.getRotate().getValue() != null) track.getRotate().getValue().process(node.rotationKeys);
                else node.rotationKeys.add(0, animation.skeleton.boneMap.get(track.getName()).poseRotation);
                if(track.getScale().getValue() != null) track.getScale().getValue().process(node.scaleKeys);
                else node.scaleKeys.add(0, animation.skeleton.boneMap.get(track.getName()).poseScale);
                if(track.getTranslate().getValue() != null && !track.getName().equalsIgnoreCase("origin")) track.getTranslate().getValue().process(node.positionKeys);
                else node.positionKeys.add(0, animation.skeleton.boneMap.get(track.getName()).posePosition);
            }
        }
        return animationNodes;
    }

}
