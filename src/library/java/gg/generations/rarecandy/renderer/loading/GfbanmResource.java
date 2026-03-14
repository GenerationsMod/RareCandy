package gg.generations.rarecandy.renderer.loading;

import gg.generations.rarecandy.pokeutils.PixelAsset;
import gg.generations.rarecandy.pokeutils.gfbanm.AnimationT;
import gg.generations.rarecandy.renderer.animation.Animation;
import gg.generations.rarecandy.renderer.animation.TransformStorage;

import java.util.HashMap;
import java.util.Map;

public record GfbanmResource(AnimationT rawAnimation) implements AnimResource {

    public static void read(PixelAsset asset, HashMap<String, AnimResource> aninResouces) {
        asset.files.entrySet().stream()
                .filter(a -> !aninResouces.containsKey(a.getKey()))
                .filter(entry -> entry.getKey().endsWith(".pkx") || entry.getKey().endsWith(".gfbanm"))
                .forEach(entry -> {
                    aninResouces.put(AnimResource.cleanAnimName(entry.getKey()), new GfbanmResource(AnimationT.deserializeFromBinary(entry.getValue())));
                });
    }

    public Map<String, Animation.Offset> getOffsets() {
        var offsets = new HashMap<String, Animation.Offset>();

        if(rawAnimation.getMaterial() != null) {
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

                var duration = 0.0;
                for (var key : uOffset) duration = Math.max(key.time(), duration);
                for (var key : vOffset) duration = Math.max(key.time(), duration);
                for (var key : uScale) duration = Math.max(key.time(), duration);
                for (var key : vScale) duration = Math.max(key.time(), duration);

                offsets.put(trackName, new Animation.Offset(uOffset, vOffset, uScale, vScale, (float) duration));
            }
        }

        return offsets;
    }

    public Map<String, Animation.AnimationNode> getNodes() {

        var animationNodes = new HashMap<String, Animation.AnimationNode>();

        if (rawAnimation.getSkeleton() != null) {
            for (var track : rawAnimation.getSkeleton().getTracks()) {

                var node = new Animation.AnimationNode();

                if(track.getRotate().getValue() != null) track.getRotate().getValue().process(node.rotationKeys);
                if(track.getScale().getValue() != null) track.getScale().getValue().process(node.scaleKeys);
                if(track.getTranslate().getValue() != null) track.getTranslate().getValue().process(node.positionKeys);

                animationNodes.put(track.getName(), node);
            }
        }

        return animationNodes;
    }

    @Override
    public long fps() {
        return rawAnimation.getInfo().getFrameRate();
    }

    @Override
    public boolean loops() {
        return rawAnimation.getInfo().getDoesLoop() == 1;
    }
}
