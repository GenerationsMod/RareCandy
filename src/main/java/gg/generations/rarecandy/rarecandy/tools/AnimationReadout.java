package gg.generations.rarecandy.rarecandy.tools;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gg.generations.rarecandy.pokeutils.tranm.*;
import gg.generations.rarecandy.renderer.animation.Animation.AnimationNode;
import gg.generations.rarecandy.renderer.animation.TranmUtil;
import gg.generations.rarecandy.tools.gui.DialogueUtils;
import gg.generations.rarecandy.tools.pkcreator.Convert;
import org.lwjgl.util.nfd.NativeFileDialog;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

public class AnimationReadout {
    public static void main(String[] args) throws IOException {
                NativeFileDialog.NFD_Init();

        var chosenFile = DialogueUtils.chooseFile("GFBANM;gfbanm");
        if(chosenFile != null) {
            var animation = Animation.getRootAsAnimation(ByteBuffer.wrap(Files.readAllBytes(chosenFile)));

//            System.out.println(fillAnimationNodesGfb(animation));

//            var json = new JsonObject();
//
//            var meta = new JsonObject();
//            meta.addProperty("keyframes", animation.meta().keyframes());
//            meta.addProperty("fps", animation.meta().fps());
//            meta.addProperty("loops", animation.meta().loops() == 1);
//
//            var anim = new JsonObject();
//            var initData = populateInitData(animation);
//            anim.add("initData", initData);
//            json.add("meta", meta);
//            json.add("anim", anim);

            System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(fillAnimationNodesGfb(animation)));
        }
    }

    private static JsonArray populateInitData(Animation animation) {
        var json = new JsonArray();

        System.out.println(Arrays.toString(IntStream.range(0, animation.anim().initDataVector().length()).peek(System.out::println).mapToObj(j -> {
            return animation.anim().initDataVector().get(0).isInit();
        }).toArray()));

//        IntStream.range(0, animation.initDataLength()).mapToObj(animation::initData).map(boneInit -> {
//            var initJson = new JsonObject();
//            initJson.addProperty("isInit", boneInit.isInit());
//            return initJson;
//        }).forEach(json::add);

        return json;
    }

    private static List<AnimationNode> fillAnimationNodesGfb(gg.generations.rarecandy.pokeutils.tranm.Animation rawAnimation) {
//        var animationNodes = new gg.generations.rarecandy.renderer.animation.Animation.AnimationNode[skeleton.boneMap.size()]; // BoneGroup

        int trueIndex = -1;
        var list = new ArrayList<AnimationNode>();

        for (int i = 0; i < rawAnimation.anim().bonesVector().length(); i++) {
            var boneAnim = rawAnimation.anim().bonesVector().get(i);
//            if(!skeleton.boneMap.containsKey(boneAnim.name())) {
//                continue;
//            }

            trueIndex++;

//            nodeIdMap.put(Objects.requireNonNull(boneAnim.name()).replace(".trmdl", ""), trueIndex);

            var node = new gg.generations.rarecandy.renderer.animation.Animation.AnimationNode();
            list.add(node);

            switch (boneAnim.rotType()) {
                case QuatTrack.DynamicQuatTrack ->
                        TranmUtil.processDynamicQuatTrack((DynamicQuatTrack) Objects.requireNonNull(boneAnim.rot(new DynamicQuatTrack())), node.rotationKeys);
                case QuatTrack.FixedQuatTrack ->
                        TranmUtil.processFixedQuatTrack((FixedQuatTrack) Objects.requireNonNull(boneAnim.rot(new FixedQuatTrack())), node.rotationKeys);
                case QuatTrack.Framed8QuatTrack ->
                        TranmUtil.processFramed8QuatTrack((Framed8QuatTrack) Objects.requireNonNull(boneAnim.rot(new Framed8QuatTrack())), node.rotationKeys);
                case QuatTrack.Framed16QuatTrack ->
                        TranmUtil.processFramed16QuatTrack((Framed16QuatTrack) Objects.requireNonNull(boneAnim.rot(new Framed16QuatTrack())), node.rotationKeys);
            }

            switch (boneAnim.scaleType()) {
                case VectorTrack.DynamicVectorTrack ->
                        TranmUtil.processDynamicVecTrack((DynamicVectorTrack) Objects.requireNonNull(boneAnim.scale(new DynamicVectorTrack())), node.scaleKeys);
                case VectorTrack.FixedVectorTrack ->
                        TranmUtil.processFixedVecTrack((FixedVectorTrack) Objects.requireNonNull(boneAnim.scale(new FixedVectorTrack())), node.scaleKeys);
                case VectorTrack.Framed8VectorTrack ->
                        TranmUtil.processFramed8VecTrack((Framed8VectorTrack) Objects.requireNonNull(boneAnim.scale(new Framed8VectorTrack())), node.scaleKeys);
                case VectorTrack.Framed16VectorTrack ->
                        TranmUtil.processFramed16VecTrack((Framed16VectorTrack) Objects.requireNonNull(boneAnim.scale(new Framed16VectorTrack())), node.scaleKeys);
            }

            switch (boneAnim.transType()) {
                case VectorTrack.DynamicVectorTrack ->
                        TranmUtil.processDynamicVecTrack((DynamicVectorTrack) Objects.requireNonNull(boneAnim.trans(new DynamicVectorTrack())), node.positionKeys);
                case VectorTrack.FixedVectorTrack ->
                        TranmUtil.processFixedVecTrack((FixedVectorTrack) Objects.requireNonNull(boneAnim.trans(new FixedVectorTrack())), node.positionKeys);
                case VectorTrack.Framed8VectorTrack ->
                        TranmUtil.processFramed8VecTrack((Framed8VectorTrack) Objects.requireNonNull(boneAnim.trans(new Framed8VectorTrack())), node.positionKeys);
                case VectorTrack.Framed16VectorTrack ->
                        TranmUtil.processFramed16VecTrack((Framed16VectorTrack) Objects.requireNonNull(boneAnim.trans(new Framed16VectorTrack())), node.positionKeys);
            }
        }

        return list;
    }
}
