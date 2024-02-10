package gg.generationsmod.rarecandy.model.animation;

import gg.generationsmod.rarecandy.model.animation.gfbanm.AnimationT;
import gg.generationsmod.rarecandy.model.animation.gfbanm.BoneTrackT;
import gg.generationsmod.rarecandy.model.config.pk.ModelConfig;

import java.util.HashMap;
import java.util.Map;

public class GfbAnimation extends Animation<AnimationT> {

    public GfbAnimation(String name, AnimationT rawAnimation, Skeleton skeleton, ModelConfig config) {
        super(name, (int) rawAnimation.getInfo().getFrameRate(), skeleton, rawAnimation, GfbAnimation::fillAnimationNodes, rawAnimation1 -> fillGfbOffsets(rawAnimation1, config));
    }

    public static Map<String, Offset> fillGfbOffsets(AnimationT rawAnimation, gg.generationsmod.rarecandy.model.config.pk.ModelConfig config) {
        var offsets = new HashMap<String, Offset>();

        if(rawAnimation.getMaterial() != null) {
            var material = rawAnimation.getMaterial();

            for(var track : material.getTracks()) {
                var trackName = track.getName();

                var uOffset = new TransformStorage<Float>();
                var vOffset = new TransformStorage<Float>();
                var uScale = new TransformStorage<Float>();
                var vScale = new TransformStorage<Float>();

                for(var entry : track.getValues()) {
                    if(entry.getName().equals("ColorUVTranslateU")) {
                        entry.getValue().getValue().process(uOffset);
                    } else if(entry.getName().equals("ColorUVTranslateV")) {
                        entry.getValue().getValue().process(vOffset);
                    }
                }

                if(uOffset.size() == 0) uOffset.add(0, 0f);
                if(vOffset.size() == 0) uOffset.add(0, 0f);

                var offset = new GfbOffset(uOffset, vOffset, uScale, vScale);

                config.getMaterialsForAnimation(trackName).forEach(a -> offsets.put(a, offset));
            }
        }

        return offsets;
    }

    public static record GfbOffset(TransformStorage<Float> uOffset, TransformStorage<Float> vOffset, TransformStorage<Float> uScale, TransformStorage<Float> vScale) implements Offset {
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
            BoneTrackT[] tracks = rawAnimation.getSkeleton().getTracks();
            for (int i = 0, tracksLength = tracks.length; i < tracksLength; i++) {
                var track = tracks[i];
                animation.nodeIdMap.put(track.getName(), i);
                var node = animationNodes[i] = new AnimationNode();

                if (track.getRotate().getValue() != null) track.getRotate().getValue().process(node.rotationKeys);
                if (track.getScale().getValue() != null) track.getScale().getValue().process(node.scaleKeys);
                if (track.getTranslate().getValue() != null && !track.getName().equalsIgnoreCase("origin")) track.getTranslate().getValue().process(node.positionKeys);
            }
        }
        return animationNodes;
    }

}
