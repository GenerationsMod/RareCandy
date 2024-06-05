package gg.generations.rarecandy.renderer.animation;

import dev.thecodewarrior.binarysmd.studiomdl.NodesBlock;
import dev.thecodewarrior.binarysmd.studiomdl.SMDFile;
import dev.thecodewarrior.binarysmd.studiomdl.SkeletonBlock;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;
import java.util.stream.Collectors;

public class SmdAnimation extends Animation<SMDFile> {

    public SmdAnimation(String name, SMDFile smdFile, Skeleton bones, int ticksPerSecond) {
        super(name, ticksPerSecond, bones, smdFile, SmdAnimation::fillAnimationNodes, Animation::fillOffsets);
        animationModifier.accept(this, "smd");
    }

    public static AnimationNode[] fillAnimationNodes(Animation<SMDFile> animation, SMDFile item) {
        @NotNull List<SkeletonBlock.@NotNull Keyframe> skeletonBlock = null;
        Map<Integer, String> nodesMap = null;

        for (var block : item.blocks) {
            if (block instanceof SkeletonBlock skeletonBlock1) {
                skeletonBlock = skeletonBlock1.keyframes;
            }
            if (block instanceof NodesBlock nodes) {
                nodesMap = nodes.bones.stream().collect(Collectors.toMap(a -> a.id, a -> a.name));
            }
        }

        if (skeletonBlock == null || nodesMap == null) throw new RuntimeException("Error!");

        return fillAnimationNodesSmdx(animation, skeletonBlock, nodesMap);
    }

    private static AnimationNode[] fillAnimationNodesSmdx(Animation<SMDFile> animation, @NotNull List<SkeletonBlock.Keyframe> keyframes, Map<Integer, String> nodeMap) {
        var nodes = new HashMap<String, List<SmdBoneStateKey>>();

        for (var keyframe : keyframes) {
            var time = keyframe.time;
            var states = keyframe.states;

            for (var boneState : states) {
                if (boneState.bone < animation.skeleton.boneArray.length - 1) {
                    var id = nodeMap.get(boneState.bone);
                    var list = nodes.computeIfAbsent(id, a -> new ArrayList<>());

                    var pos = id.equals("origin") ? new Vector3f() : new Vector3f(boneState.posX, boneState.posY, boneState.posZ);

                    list.add(new SmdBoneStateKey(time, pos, new Quaternionf().rotateZYX(boneState.rotZ, boneState.rotY, boneState.rotX)));
                }
            }

            nodes.forEach((k, v) -> v.sort(Comparator.comparingInt(SmdBoneStateKey::time)));
        }

        var animationNodes = new AnimationNode[nodes.size()];
        for (var entry : nodes.entrySet()) {
            animationNodes[animation.nodeIdMap.computeIfAbsent(entry.getKey(), animation::newNode)] = createNode(entry.getValue());
        }

        return animationNodes;
    }

    public static AnimationNode createNode(List<SmdBoneStateKey> keys) {
        var animationNode = new AnimationNode();

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
