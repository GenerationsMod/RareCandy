package gg.generations.rarecandy.renderer.loading;

import gg.generations.rarecandy.pokeutils.PixelAsset;
import gg.generations.rarecandy.pokeutils.tracm.TRACM;
import gg.generations.rarecandy.pokeutils.tracm.TrackMaterialValueList;
import gg.generations.rarecandy.pokeutils.tranm.QuatTrack;
import gg.generations.rarecandy.pokeutils.tranm.TRANMT;
import gg.generations.rarecandy.pokeutils.tranm.VectorTrack;
import gg.generations.rarecandy.renderer.animation.Animation;
import gg.generations.rarecandy.renderer.animation.Skeleton;
import gg.generations.rarecandy.renderer.animation.TranmUtil;
import gg.generations.rarecandy.renderer.animation.TransformStorage;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record TrAnimationResource(TRANMT tranm, TRACM tracm) implements AnimResource {

    public static void read(PixelAsset asset, Map<String, AnimResource> completed) {

            var list = asset.files.keySet().stream().filter(a -> a.endsWith("tranm") || a.endsWith("tracm")).collect(Collectors.toCollection(ArrayList::new));

            while (!list.isEmpty()) {
                var a = list.remove(0);

                var name = a.replace(".tranm", "").replace("tracm", "");
                TRANMT tranm = null;
                TRACM tracm = null;

                if (a.endsWith(".tranm")) {
                    tranm = TRANMT.deserializeFromBinary(asset.files.get(a));

                    var index = list.indexOf(a.replace(".tranm", ".tracm"));

                    if (index != -1) {
                        tracm = TRACM.getRootAsTRACM(ByteBuffer.wrap(asset.files.get(list.remove(index))));
                    }
                } else {
                    if (a.endsWith(".tracm")) {
                        tracm = TRACM.getRootAsTRACM(ByteBuffer.wrap(asset.files.get(a)));

                        var index = list.indexOf(a.replace(".tracm", ".tranm"));

                        if (index != -1) {
                            tranm = TRANMT.deserializeFromBinary(asset.files.get(list.remove(index)));
                        }
                    }
                }

                completed.put(name, new TrAnimationResource(tranm, tracm));
            }
        }

    @Override
    public Animation.AnimationNode[] getNodes(Skeleton skeleton) {
        var animationNodes = new Animation.AnimationNode[skeleton.jointMap.size()];

        if (tranm != null) {
            for (int i = 0; i < tranm.getTrack().getTracks().length; i++) {
                var boneAnim = tranm.getTrack().getTracks()[i];

                if (!skeleton.boneIdMap.containsKey(boneAnim.getBoneName())) {
                    continue;
                }

                var node = animationNodes[skeleton.boneIdMap.get(boneAnim.getBoneName())] = new Animation.AnimationNode();

                var rotate = boneAnim.getRotate();

                switch (boneAnim.getRotate().getType()) {
                    case QuatTrack.DynamicQuatTrack -> TranmUtil.processDynamicQuatTrack(rotate.asDynamicRotationTrack(), node.rotationKeys);
                    case QuatTrack.FixedQuatTrack -> TranmUtil.processFixedQuatTrack(rotate.asFixedRotationTrack(), node.rotationKeys);
                    case QuatTrack.Framed8QuatTrack -> TranmUtil.processFramed8QuatTrack(rotate.asFramed8RotationTrack(), node.rotationKeys);
                    case QuatTrack.Framed16QuatTrack -> TranmUtil.processFramed16QuatTrack(rotate.asFramed16RotationTrack(), node.rotationKeys);
                }

                var scale = boneAnim.getScale();

                switch (scale.getType()) {
                    case VectorTrack.DynamicVectorTrack -> TranmUtil.processDynamicVecTrack(scale.asDynamicVectorTrack(), node.scaleKeys);
                    case VectorTrack.FixedVectorTrack -> TranmUtil.processFixedVecTrack(scale.asFixedVectorTrack(), node.scaleKeys);
                    case VectorTrack.Framed8VectorTrack -> TranmUtil.processFramed8VecTrack(scale.asFramed8VectorTrack(), node.scaleKeys);
                    case VectorTrack.Framed16VectorTrack -> TranmUtil.processFramed16VecTrack(scale.asFramed16VectorTrack(), node.scaleKeys);
                }

                var translate = boneAnim.getTranslate();

                switch (translate.getType()) {
                    case VectorTrack.DynamicVectorTrack -> TranmUtil.processDynamicVecTrack(translate.asDynamicVectorTrack(), node.positionKeys);
                    case VectorTrack.FixedVectorTrack -> TranmUtil.processFixedVecTrack(translate.asFixedVectorTrack(), node.positionKeys);
                    case VectorTrack.Framed8VectorTrack -> TranmUtil.processFramed8VecTrack(translate.asFramed8VectorTrack(), node.positionKeys);
                    case VectorTrack.Framed16VectorTrack -> TranmUtil.processFramed16VecTrack(translate.asFramed16VectorTrack(), node.positionKeys);
                }
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

    public Map<String, Animation.Offset> getOffsets() {
        var offsets = new HashMap<String, Animation.Offset>();

        if (tracm != null) {

            for (int i = 0; i < tracm.tracksLength(); i++) {
                var tracks = tracm.tracks(i);
                var materialTimeline = tracks.materialAnimation();

                if (materialTimeline == null) continue;

                for (int j = 0; j < materialTimeline.materialTrackLength(); j++) {
                    var materialTrack = materialTimeline.materialTrack(j);

                    for (int k = 0; k < materialTrack.animValuesLength(); k++) {
                        var animValues = materialTrack.animValues(k);

                        var list = animValues.list();

                        var uOffset = toStorage(list.blue());
                        var vOffset = toStorage(list.alpha());
                        var uScale = toStorage(list.green());
                        var vScale = toStorage(list.red());

                        var duration = 0.0;
                        for (var key : uOffset) duration = Math.max(key.time(), duration);
                        for (var key : vOffset) duration = Math.max(key.time(), duration);
                        for (var key : uScale) duration = Math.max(key.time(), duration);
                        for (var key : vScale) duration = Math.max(key.time(), duration);

                        offsets.putIfAbsent(materialTrack.name(), new Animation.Offset(
                                uOffset,
                                vOffset,
                                uScale,
                                vScale, (float) duration));
                    }

                }
            }
        }

        return offsets;
    }

    private TransformStorage<Float> toStorage(TrackMaterialValueList value) {
        var storage = new TransformStorage<Float>();
        IntStream.range(0, value.valuesLength()).mapToObj(value::values).forEach(val -> storage.add(val.time(), val.value()));
        return storage;
    }

    public long fps() {
        if(tranm != null) {
            return tranm.getInfo().getAnimationRate();
        } else if(tracm != null) {
            return tracm.config().framerate();
        } else {
            return 0L;
        }
    }
}
