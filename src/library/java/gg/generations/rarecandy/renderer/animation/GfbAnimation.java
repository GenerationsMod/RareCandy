package gg.generations.rarecandy.renderer.animation;

import com.google.gson.*;
import gg.generations.rarecandy.pokeutils.GFLib.Anim.*;
import gg.generations.rarecandy.renderer.rendering.Bone;
import org.apache.commons.compress.utils.Sets;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Stream.of;

public class GfbAnimation extends Animation<AnimationT> {


    public Map<String, Offset> offsets;

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

        fillEyeOffset(rawAnimation);

        animationModifier.accept(this, "gfb");
    }

    private void fillEyeOffset(AnimationT rawAnimation) {

        if(rawAnimation.getMaterial() != null) {
            var material = rawAnimation.getMaterial();

            offsets = new HashMap<>();

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
    }

    public AnimationNode[] fillAnimationNodes(AnimationT rawAnimation) {

        var animationNodes = new AnimationNode[rawAnimation.getSkeleton().getTracks().length];

        if (rawAnimation.getSkeleton() != null) {
            for (var track : rawAnimation.getSkeleton().getTracks()) {
                var node = animationNodes[nodeIdMap.computeIfAbsent(track.getName(), this::newNode)] = new AnimationNode();

                track.getRotate().getValue().process(node.rotationKeys);
                track.getScale().getValue().process(node.scaleKeys);
                track.getTranslate().getValue().process(node.positionKeys);
            }
        }
        return animationNodes;
    }

    public record Offset(TransformStorage<Float> uStorage, TransformStorage<Float> vStorage) {
        public void calcOffset(float animTime, Vector2f instance) {
            var u = calcInterpolatedFloat(animTime, uStorage);
            var v = calcInterpolatedFloat(animTime, vStorage);

            instance.set(u, v);
        }

        public static Float calcInterpolatedFloat(float animTime, TransformStorage<Float> node) {
            if (node.size() == 0) return 0.0f;

            var offset = findOffset(animTime, node);
            return offset.value();
        }

        public static TransformStorage.TimeKey<Float> findOffset(float animTime, TransformStorage<Float> keys) {
            for (var key : keys) {
                if (animTime < key.time())
                    return keys.getBefore(key);
            }

            return keys.get(0);
        }
    }
}
