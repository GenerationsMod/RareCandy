package gg.generations.rarecandy.renderer.loading;

import dev.thecodewarrior.binarysmd.studiomdl.NodesBlock;
import dev.thecodewarrior.binarysmd.studiomdl.SMDFile;
import dev.thecodewarrior.binarysmd.studiomdl.SkeletonBlock;
import gg.generations.rarecandy.renderer.animation.Animation;
import gg.generations.rarecandy.renderer.animation.Skeleton;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;
import java.util.stream.Collectors;

public class SmdUtils {
    public static Animation.AnimationNode[] getNode(Animation animation, Skeleton skeleton, SMDFile item) {
        List<SkeletonBlock.@NotNull Keyframe> skeletonBlock = null;
        Map<Integer, String> nodesMap = null;

        for (var block : item.blocks) {
            if (block instanceof SkeletonBlock skeletonBlock1) {
                skeletonBlock = skeletonBlock1.keyframes;
            }
            if(block instanceof NodesBlock nodes) {
                nodesMap = nodes.bones.stream().collect(Collectors.toMap(a -> a.id, a -> a.name));
            }
        }

        if(skeletonBlock == null || nodesMap == null) throw new RuntimeException("Error!");

        return fillAnimationNodesSmdx(animation, skeleton, skeletonBlock, nodesMap);
    }

    private static Animation.AnimationNode[] fillAnimationNodesSmdx(Animation animation, Skeleton skeleton, @NotNull List<SkeletonBlock.Keyframe> keyframes, Map<Integer, String> nodeMap) {
        var nodes = new HashMap<String, List<SmdBoneStateKey>>();

        for (var keyframe : keyframes) {
            var time = keyframe.time;
            var states = keyframe.states;

            for (var boneState : states) {
                if (boneState.bone < skeleton.bones.length - 1) {
                    var id = nodeMap.get(boneState.bone);
                    var list = nodes.computeIfAbsent(id, a -> new ArrayList<>());

                    var pos = id.equals("origin") ? new Vector3f() : new Vector3f(boneState.posX, boneState.posY, boneState.posZ);

                    list.add(new SmdBoneStateKey(time, pos, new Quaternionf().rotateZYX(boneState.rotZ, boneState.rotY, boneState.rotX)));
                }
            }

            nodes.forEach((k, v) -> v.sort(Comparator.comparingInt(SmdBoneStateKey::time)));
        }

        var animationNodes = new Animation.AnimationNode[nodes.size()];
        for (var entry : nodes.entrySet()) {
            animationNodes[animation.nodeIdMap.computeIfAbsent(entry.getKey(), animation::newNode)] = createNode(entry.getValue());
        }

        return animationNodes;
    }

    public static Animation.AnimationNode createNode(List<SmdBoneStateKey> keys) {
        var animationNode = new Animation.AnimationNode();

        if (keys.isEmpty()) {
            animationNode.positionKeys.add(0, new Vector3f());
            animationNode.rotationKeys.add(0, new Quaternionf());
        } else {
            for (var key : keys) {
                animationNode.positionKeys.add(key.time(), key.pos());
                animationNode.rotationKeys.add(key.time(), key.rot());
            }
        }

        animationNode.scaleKeys.add(0, new Vector3f(1, 1, 1));

        return animationNode;
    }

    public record SmdBoneStateKey(int time, Vector3f pos, Quaternionf rot) {
    }
}
