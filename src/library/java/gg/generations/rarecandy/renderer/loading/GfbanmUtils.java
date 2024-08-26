package gg.generations.rarecandy.renderer.loading;

import gg.generations.rarecandy.pokeutils.gfbanm.AnimationT;
import gg.generations.rarecandy.renderer.animation.Animation;
import gg.generations.rarecandy.renderer.animation.Skeleton;
import gg.generations.rarecandy.renderer.animation.TransformStorage;

import java.util.HashMap;
import java.util.Map;

public class GfbanmUtils {
    public static Map<String, Animation.Offset> getOffsets(AnimationT rawAnimation) {
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

    public static Animation.AnimationNode[] getNodes(Skeleton skeleton, AnimationT rawAnimation) {

        var animationNodes = new Animation.AnimationNode[skeleton.jointMap.size()];

        if (rawAnimation.getSkeleton() != null) {
            for (var track : rawAnimation.getSkeleton().getTracks()) {
                var node = animationNodes[skeleton.boneIdMap.get(track.getName())] = new Animation.AnimationNode();

                if(track.getRotate().getValue() != null) track.getRotate().getValue().process(node.rotationKeys);
                else node.rotationKeys.add(0, skeleton.jointMap.get(track.getName()).poseRotation);
                if(track.getScale().getValue() != null) track.getScale().getValue().process(node.scaleKeys);
                else node.scaleKeys.add(0, skeleton.jointMap.get(track.getName()).poseScale);

                if(track.getTranslate().getValue() != null) track.getTranslate().getValue().process(node.positionKeys);
                else node.positionKeys.add(0, skeleton.jointMap.get(track.getName()).posePosition);
            }
        }

        for (int i = 0; i < animationNodes.length; i++) {

            if(animationNodes[i] == null) {
                var node = new Animation.AnimationNode();
                var joint = skeleton.jointMap.get(skeleton.bones[i].name);

                node.rotationKeys.add(0, joint.poseRotation);
                node.rotationKeys.add(0, joint.poseRotation);
                node.scaleKeys.add(0, joint.poseScale);

            }
        }
        
        return animationNodes;
    }
}
