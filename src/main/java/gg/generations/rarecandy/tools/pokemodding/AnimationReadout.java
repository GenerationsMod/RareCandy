package gg.generations.rarecandy.tools.pokemodding;

import com.google.gson.*;
import dev.thecodewarrior.binarysmd.studiomdl.NodesBlock;
import dev.thecodewarrior.binarysmd.studiomdl.SMDFile;
import dev.thecodewarrior.binarysmd.studiomdl.SkeletonBlock;
import gg.generations.rarecandy.pokeutils.Pair;
import gg.generations.rarecandy.pokeutils.gfbanm.Anim.*;
import gg.generations.rarecandy.pokeutils.tracm.TRACM;
import gg.generations.rarecandy.pokeutils.tracm.TrackMaterial;
import gg.generations.rarecandy.pokeutils.tracm.TrackMaterialAnim;
import gg.generations.rarecandy.pokeutils.tranm.DynamicRotationTrackT;
import gg.generations.rarecandy.pokeutils.tranm.DynamicVectorTrackT;
import gg.generations.rarecandy.pokeutils.tranm.FixedRotationTrackT;
import gg.generations.rarecandy.pokeutils.tranm.FixedVectorTrackT;
import gg.generations.rarecandy.pokeutils.tranm.Framed16RotationTrackT;
import gg.generations.rarecandy.pokeutils.tranm.Framed16VectorTrackT;
import gg.generations.rarecandy.pokeutils.tranm.Framed8RotationTrackT;
import gg.generations.rarecandy.pokeutils.tranm.Framed8VectorTrackT;
import gg.generations.rarecandy.pokeutils.tranm.RotationTrackUnion;
import gg.generations.rarecandy.pokeutils.tranm.Vec3T;
import gg.generations.rarecandy.pokeutils.tranm.VectorTrackUnion;
import gg.generations.rarecandy.pokeutils.tranm.sVec3T;
import gg.generations.rarecandy.renderer.animation.Animation.AnimationNode;
import gg.generations.rarecandy.renderer.animation.SmdAnimation;
import gg.generations.rarecandy.renderer.animation.TranmUtilExperimental;
import gg.generations.rarecandy.renderer.animation.TransformStorage;
import gg.generations.rarecandy.tools.gui.DialogueUtils;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.util.nfd.NativeFileDialog;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AnimationReadout {
    private static Gson gson = new GsonBuilder().setPrettyPrinting()
            .registerTypeAdapter(TrackProcesser.class, (JsonSerializer<TrackProcesser<?>>) (src, typeOfSrc, context) -> {
                if(src instanceof DynamicFloatTrackT track) return context.serialize(track);
                if(src instanceof DynamicRotationTrackT track) return context.serialize(track);
                if(src instanceof DynamicVectorTrackT track) return context.serialize(track);
                if(src instanceof FixedFloatTrackT track) return context.serialize(track);
                if(src instanceof FixedRotationTrackT track) return context.serialize(track);
                if(src instanceof FixedVectorTrackT track) return context.serialize(track);
                if(src instanceof Framed16FloatTrackT track) return context.serialize(track);
                if(src instanceof Framed16RotationTrackT track) return context.serialize(track);
                if(src instanceof Framed16VectorTrackT track) return context.serialize(track);
                if(src instanceof Framed8FloatTrackT track) return context.serialize(track);
                if(src instanceof Framed8RotationTrackT track) return context.serialize(track);
                if(src instanceof Framed8VectorTrackT track) return context.serialize(track);
                return null;
            }).create();
    public static void main(String[] args) throws IOException {
        NativeFileDialog.NFD_Init();

        var chosenFile = DialogueUtils.chooseFile("TRACM;tracm");
        if(chosenFile != null) {
            var tracm = TRACM.getRootAsTRACM(ByteBuffer.wrap(Files.readAllBytes(chosenFile)));

            var map = IntStream.range(0, 3).mapToObj(tracm::tracks).map(a -> IntStream.range(0, a.materialAnimation().materialTrackLength()).mapToObj(b -> a.materialAnimation().materialTrack(b)).collect(Collectors.toMap(TrackMaterial::name, b -> IntStream.range(0, b.animValuesLength()).mapToObj(b::animValues).map(TrackMaterialAnim::name).collect(Collectors.toList())))).toList();

            System.out.println(map);

//            var pair = fillAnimationNodes(new SMDTextReader().read(Files.readString(chosenFile)));



//            var animation = new AnimationT();
//
//            var info = new InfoT();
//            info.setDoesLoop(0);
//            info.setFrameRate(30);
//            info.setKeyFrames(Stream.of(pair.a()).flatMapToDouble(a -> DoubleStream.concat(Stream.of(a.rotationKeys.values).mapToDouble(b -> b.time()), DoubleStream.concat(Stream.of(a.rotationKeys.values).mapToDouble(TransformStorage.TimeKey::time), Stream.of(a.rotationKeys.values).mapToDouble(TransformStorage.TimeKey::time)))).mapToInt(a -> (int) a).max().getAsInt());
//            animation.setInfo(info);
//
//            var skeleton = new BoneAnimationT();
//
//            var tracks = new BoneTrackT[pair.a().length];
//
//            for (int i = 0; i < pair.b().size(); i++) {
//                var track = new BoneTrackT();
//                track.setName(pair.b().get(i));
//                var node = pair.a()[i];
//
//                track.setRotate(createRotationUnion(node.rotationKeys, pair.a().length));
//                track.setTranslate(createVectorUnion(node.positionKeys, pair.a().length));
//                track.setScale(createVectorUnion(node.scaleKeys, pair.a().length));
//                tracks[i] = track;
//            }
//
//            skeleton.setTracks(tracks);
//
//            animation.setSkeleton(skeleton);
//
//            Files.write(Path.of(chosenFile.getFileName().toString().replace("smd", "gfbanm")), animation.serializeToBinary());
        }
    }

    public static void gfbanmPrintOut(String[] args) throws IOException {
        NativeFileDialog.NFD_Init();

        var chosenFile = DialogueUtils.chooseFile("GFBANM;gfbanm");
        if(chosenFile != null) {
            var animation = AnimationT.deserializeFromBinary(Files.readAllBytes(chosenFile));

            Files.writeString(Path.of(chosenFile.getFileName().toString().replace("gfbanm", "json")), gson.toJson(animation));
        }
    }

    private static VectorTrackUnion createVectorUnion(TransformStorage<Vector3f> positionKeys, int length) {
        var rot = new VectorTrackUnion();

        if(positionKeys.values.length == 1) {
            var rotation = new FixedVectorTrackT();
            rot.setValue(rotation);
            rot.setType((byte) 1);

            var array = new Vec3T();
            array.setX(positionKeys.values[0].value().x());
            array.setY(positionKeys.values[0].value().y());
            array.setZ(positionKeys.values[0].value().z());
            rotation.setCo(array);

            return rot;
        }

        var rotation = new DynamicVectorTrackT();
        rot.setValue(rotation);
        rot.setType((byte) 2);

        var array = new Vec3T[positionKeys.size()];

        for (int j = 0; j < positionKeys.size(); j++) {
            var rots = new Vec3T();
            var key = positionKeys.values[j];
            var val = key.value();
            rots.setX(val.x());
            rots.setY(val.y());
            rots.setZ(val.z());

            array[j] = rots;
        }

        rotation.setCo(array);

        return rot;
    }

    private static RotationTrackUnion createRotationUnion(TransformStorage<Quaternionf> rotationKeys, int length) {
        var rot = new RotationTrackUnion();
        var rotation = new DynamicRotationTrackT();

        rot.setValue(rotation);
        rot.setType((byte) 2);

        var array = new sVec3T[rotationKeys.size()];

        for (int j = 0; j < rotationKeys.size(); j++) {
            var rots = new sVec3T();
            var key  = rotationKeys.values[j];
                var val = TranmUtilExperimental.pack(key.value());
                rots.setX(val[0]);
                rots.setY(val[1]);
                rots.setZ(val[2]);


            array[j] = rots;
        }

        rotation.setCo(array);

        return rot;
    }

    public static Pair<AnimationNode[], List<String>> fillAnimationNodes(SMDFile item) {
        @NotNull List<SkeletonBlock.@NotNull Keyframe> skeletonBlock = null;
        Map<Integer, String> nodesMap = null;
        List<String> bonesMap = null;

        for (var block : item.blocks) {
            if (block instanceof SkeletonBlock skeletonBlock1) {
                skeletonBlock = skeletonBlock1.keyframes;
            }
            if(block instanceof NodesBlock nodes) {
                nodesMap = nodes.bones.stream().collect(Collectors.toMap(a -> a.id, a -> a.name));
                bonesMap = nodes.bones.stream().sorted(Comparator.comparingInt(a -> a.id)).map(a -> a.name).toList();
            }
        }

        if(skeletonBlock == null || nodesMap == null) throw new RuntimeException("Error!");

        return fillAnimationNodesSmdx(skeletonBlock, nodesMap, bonesMap);
    }

    private static Pair<AnimationNode[], List<String>> fillAnimationNodesSmdx(@NotNull List<SkeletonBlock.Keyframe> keyframes, Map<Integer, String> nodeMap, List<String> bonesMap) {
        var nodes = new HashMap<String, List<SmdAnimation.SmdBoneStateKey>>();

        for (var keyframe : keyframes) {
            var time = keyframe.time;
            var states = keyframe.states;

            for (var boneState : states) {
                var id = nodeMap.get(boneState.bone);
                var list = nodes.computeIfAbsent(id, a -> new ArrayList<>());
                list.add(new SmdAnimation.SmdBoneStateKey(time, new Vector3f(boneState.posX, boneState.posY, boneState.posZ), new Quaternionf().rotateZYX(boneState.rotZ, boneState.rotY, boneState.rotX)));
            }

            nodes.forEach((k, v) -> v.sort(Comparator.comparingInt(SmdAnimation.SmdBoneStateKey::time)));
        }

        var animationNodes = new AnimationNode[nodes.size()];

        for (var entry : nodes.entrySet()) {
            animationNodes[bonesMap.indexOf(entry.getKey())] = createNode(entry.getValue());
        }

        return new Pair<>(animationNodes, bonesMap);
    }

    public static AnimationNode createNode(List<SmdAnimation.SmdBoneStateKey> keys) {
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

}
