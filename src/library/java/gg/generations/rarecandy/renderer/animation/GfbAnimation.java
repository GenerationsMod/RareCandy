package gg.generations.rarecandy.renderer.animation;

import gg.generations.rarecandy.pokeutils.GFLib.Anim.AnimationT;
import gg.generations.rarecandy.pokeutils.ModelConfig;

import java.util.HashMap;
import java.util.Map;

public class GfbAnimation extends Animation<AnimationT> {

    public GfbAnimation(String name, AnimationT rawAnimation, Skeleton skeleton, ModelConfig config) {
        super(name, (int) rawAnimation.getInfo().getFrameRate(), skeleton, rawAnimation, GfbAnimation::fillAnimationNodes, rawAnimation1 -> fillGfbOffsets(rawAnimation1, config));

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

    public static Map<String, Offset> fillGfbOffsets(AnimationT rawAnimation, ModelConfig config) {
        var offsets = new HashMap<String, Offset>();

        if (rawAnimation.getMaterial() != null) {
            var material = rawAnimation.getMaterial();

            for (var track : material.getTracks()) {
                var trackName = track.getName();

                var uOffset = new TransformStorage<Float>();
                var vOffset = new TransformStorage<Float>();
                var uScale = new TransformStorage<Float>();
                var vScale = new TransformStorage<Float>();

                for (var entry : track.getValues()) {
                    if (entry.getName().equals("ColorUVTranslateU")) {
                        entry.getValue().getValue().process(uOffset);
                    } else if (entry.getName().equals("ColorUVTranslateV")) {
                        entry.getValue().getValue().process(vOffset);
                    }
                }

                if (uOffset.size() == 0) uOffset.add(0, 0f);
                if (vOffset.size() == 0) uOffset.add(0, 0f);

                var offset = new GfbOffset(uOffset, vOffset, uScale, vScale);

                config.getMaterialsForAnimation(trackName).forEach(a -> offsets.put(a, offset));
            }
        }

        return offsets;
    }

    public static record GfbOffset(TransformStorage<Float> uOffset, TransformStorage<Float> vOffset,
                                   TransformStorage<Float> uScale, TransformStorage<Float> vScale) implements Offset {
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

        @Override
        public void calcOffset(float animTime, Transform instance) {

            var uOffset = calcInterpolatedFloat(animTime, this.uOffset(), 0f);
            var vOffset = calcInterpolatedFloat(animTime, this.vOffset(), 0f);
            var uScale = calcInterpolatedFloat(animTime, this.uScale(), 1f);
            var vScale = calcInterpolatedFloat(animTime, this.vScale(), 1f);

            instance.offset().set(uOffset, vOffset);
            instance.scale().set(uScale, vScale);
        }
    }

    public static AnimationNode[] fillAnimationNodes(Animation<AnimationT> animation, AnimationT rawAnimation) {

        var animationNodes = new AnimationNode[rawAnimation.getSkeleton().getTracks().length];

        if (rawAnimation.getSkeleton() != null) {
            for (var track : rawAnimation.getSkeleton().getTracks()) {
                var node = animationNodes[animation.nodeIdMap.computeIfAbsent(track.getName(), animation::newNode)] = new AnimationNode();

                if (track.getRotate().getValue() != null) track.getRotate().getValue().process(node.rotationKeys);
                else node.rotationKeys.add(0, animation.skeleton.boneMap.get(track.getName()).poseRotation);
                if (track.getScale().getValue() != null) track.getScale().getValue().process(node.scaleKeys);
                else node.scaleKeys.add(0, animation.skeleton.boneMap.get(track.getName()).poseScale);
                if (track.getTranslate().getValue() != null && !track.getName().equalsIgnoreCase("origin"))
                    track.getTranslate().getValue().process(node.positionKeys);
                else node.positionKeys.add(0, animation.skeleton.boneMap.get(track.getName()).posePosition);
            }
        }
        return animationNodes;
    }

}
