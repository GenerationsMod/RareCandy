package gg.generations.rarecandy.renderer.animation;

import com.google.gson.*;
import gg.generations.rarecandy.pokeutils.GFLib.Anim.*;
import gg.generations.rarecandy.renderer.rendering.Bone;
import org.apache.commons.compress.utils.Sets;
import org.joml.Vector3f;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Stream.of;

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

        fillEyeOffset(rawAnimation);

        animationModifier.accept(this, "gfb");
    }

    private void fillEyeOffset(AnimationT rawAnimation) {


        if(rawAnimation.getMaterial() != null) {
            var material = rawAnimation.getMaterial();

            var map = Arrays.stream(material.getTracks()).filter(a -> a.getName().equals("Eye")).flatMap(a -> of(a.getValues())).collect(Collectors.toMap(ShaderEntryT::getName, ShaderEntryT::getValue));

            eyeOffsetU = new TransformStorage<Float>();
            eyeOffsetV = new TransformStorage<Float>();

            var uOffset = map.get("ColorUVTranslateU");
            var vOffset = map.get("ColorUVTranslateV");

            if(uOffset != null) uOffset.getValue().process(eyeOffsetU);
            else eyeOffsetU.add(0.0, 0.0f);

            if(vOffset != null) {
                vOffset.getValue().process(eyeOffsetV);
            } else {
                eyeOffsetV.add(0.0, 0.0f);
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
}
