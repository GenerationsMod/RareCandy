package gg.generations.rarecandy.renderer.loading;

import dev.thecodewarrior.binarysmd.formats.SMDBinaryReader;
import dev.thecodewarrior.binarysmd.formats.SMDTextReader;
import dev.thecodewarrior.binarysmd.studiomdl.NodesBlock;
import dev.thecodewarrior.binarysmd.studiomdl.SMDFile;
import dev.thecodewarrior.binarysmd.studiomdl.SkeletonBlock;
import gg.generations.rarecandy.pokeutils.resource.ResourceReader;
import gg.generations.rarecandy.renderer.animation.Animation;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.msgpack.core.MessagePack;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public record SmdResource(SMDFile item) implements AnimResource {
    private static final List<String> EXTENSIONS = List.of("smd", "smdx");
    private static final SMDTextReader SMD_READER = new SMDTextReader();
    private static final SMDBinaryReader SMDX_READER = new SMDBinaryReader();

    public static void read(ResourceReader asset, HashMap<String, AnimResource> aninResouces) throws IOException {
        var files = asset.getFileNames().stream().filter(a -> EXTENSIONS.stream().anyMatch(a::endsWith)).toList();

        for (var entry : files) {
            var split = entry.split("\\.");

            var smdFile = getFile(split[1], asset.getFile(entry));
            aninResouces.put(split[0], smdFile);
        }
    }

    private static SmdResource getFile(String extension, byte[] data) {
        return new SmdResource(switch (extension) {
            case "smd" -> SMD_READER.read(new String(data));
            case "smdx" -> {
                try {
                    yield SMDX_READER.read(MessagePack.newDefaultUnpacker(data));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            default -> throw new RuntimeException("Error! Improper extension for animations: " + extension);
        });
    }

    public Map<String, Animation.AnimationNode> getNodes() {
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

        return fillAnimationNodesSmdx(skeletonBlock, nodesMap);
    }

    private static Map<String, Animation.AnimationNode> fillAnimationNodesSmdx(@NotNull List<SkeletonBlock.Keyframe> keyframes, Map<Integer, String> nodeMap) {
        var nodes = new HashMap<String, List<SmdBoneStateKey>>();

        for (var keyframe : keyframes) {
            var time = keyframe.time;
            var states = keyframe.states;

            for (var boneState : states) {

                var id = nodeMap.get(boneState.bone);
                var list = nodes.computeIfAbsent(id, a -> new ArrayList<>());

                var pos = new Vector3f(boneState.posX, boneState.posY, boneState.posZ);

                list.add(new SmdBoneStateKey(time, pos, new Quaternionf().rotateZYX(boneState.rotZ, boneState.rotY, boneState.rotX)));
            }

            nodes.forEach((k, v) -> v.sort(Comparator.comparingInt(SmdBoneStateKey::time)));
        }

        var animationNodes = new HashMap<String, Animation.AnimationNode>();
        for (var entry : nodes.entrySet()) {
            animationNodes.put(entry.getKey(), createNode(entry.getValue()));
        }

        return animationNodes;
    }

    private static Animation.AnimationNode createNode(List<SmdBoneStateKey> keys) {
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

    @Override
    public Map<String, Animation.Offset> getOffsets() {
        return new HashMap<>();
    }

    @Override
    public long fps() {
        return 30;
    }

    @Override
    public boolean loops() {
        return false;
    }
}
