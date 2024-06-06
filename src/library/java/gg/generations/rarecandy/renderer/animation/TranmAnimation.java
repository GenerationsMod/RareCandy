package gg.generations.rarecandy.renderer.animation;

import gg.generations.rarecandy.pokeutils.Pair;
import gg.generations.rarecandy.pokeutils.tracm.TRACM;
import gg.generations.rarecandy.pokeutils.tracm.TrackMaterialAnim;
import gg.generations.rarecandy.pokeutils.tracm.TrackMaterialValueList;
import gg.generations.rarecandy.pokeutils.tranm.QuatTrack;
import gg.generations.rarecandy.pokeutils.tranm.TRANMT;
import gg.generations.rarecandy.pokeutils.tranm.VectorTrack;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static gg.generations.rarecandy.renderer.animation.GfbAnimation.GfbOffset.calcInterpolatedFloat;

public class TranmAnimation extends Animation<Pair<gg.generations.rarecandy.pokeutils.tranm.TRANMT, TRACM>> {
    public TranmAnimation(String name, Pair<gg.generations.rarecandy.pokeutils.tranm.TRANMT, TRACM> rawAnimation, Skeleton skeleton, boolean ignoreScale) {
        super(name, (int) getFps(rawAnimation), skeleton, rawAnimation, (animation, animationPair) -> fillAnimationNodes(animation, animationPair, skeleton, ignoreScale), TranmAnimation::fillTrOffsets);

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

        animationModifier.accept(this, "gfb");
    }

    private static long getFps(Pair<gg.generations.rarecandy.pokeutils.tranm.TRANMT, TRACM> rawAnimation) {
        if (rawAnimation.a() != null) {
            return rawAnimation.a().getInfo().getAnimationRate();
        } else if (rawAnimation.b() != null) {
            return rawAnimation.b().config().framerate();
        } else {
            return 0L;
        }
    }

    public static <T> AnimationNode[] fillAnimationNodes(Animation<Pair<TRANMT, TRACM>> animation, Pair<TRANMT, TRACM> animationPair, Skeleton skeleton, boolean ignoreScale) {
        var rawAnimation = animationPair.a();

        if (rawAnimation != null) {
            var animationNodes = new AnimationNode[rawAnimation.getTrack().getTracks().length]; // BoneGroup

            for (int i = 0; i < rawAnimation.getTrack().getTracks().length; i++) {
                var boneAnim = rawAnimation.getTrack().getTracks()[i];

                var node = animationNodes[animation.nodeIdMap.computeIfAbsent(boneAnim.getBoneName().replace(".trmdl", ""), animation::newNode)] = new AnimationNode();

                var rotate = boneAnim.getRotate();

                switch (boneAnim.getRotate().getType()) {
                    case QuatTrack.DynamicQuatTrack ->
                            TranmUtil.processDynamicQuatTrack(rotate.asDynamicRotationTrack(), node.rotationKeys);
                    case QuatTrack.FixedQuatTrack ->
                            TranmUtil.processFixedQuatTrack(rotate.asFixedRotationTrack(), node.rotationKeys);
                    case QuatTrack.Framed8QuatTrack ->
                            TranmUtil.processFramed8QuatTrack(rotate.asFramed8RotationTrack(), node.rotationKeys);
                    case QuatTrack.Framed16QuatTrack ->
                            TranmUtil.processFramed16QuatTrack(rotate.asFramed16RotationTrack(), node.rotationKeys);
                }

                if(!ignoreScale) {
                    var scale = boneAnim.getScale();
                    switch (scale.getType()) {
                        case VectorTrack.DynamicVectorTrack -> TranmUtil.processDynamicVecTrack(scale.asDynamicVectorTrack(), node.scaleKeys);
                        case VectorTrack.FixedVectorTrack -> TranmUtil.processFixedVecTrack(scale.asFixedVectorTrack(), node.scaleKeys);
                        case VectorTrack.Framed8VectorTrack -> TranmUtil.processFramed8VecTrack(scale.asFramed8VectorTrack(), node.scaleKeys);
                        case VectorTrack.Framed16VectorTrack -> TranmUtil.processFramed16VecTrack(scale.asFramed16VectorTrack(), node.scaleKeys);
                    }
                } else {
                    node.scaleKeys.add(0, new Vector3f(1,1,1));
                }

                if (!Objects.requireNonNull(boneAnim.getBoneName()).equalsIgnoreCase("origin")) {
                    var translate = boneAnim.getTranslate();
                    switch (translate.getType()) {
                        case VectorTrack.DynamicVectorTrack ->
                                TranmUtil.processDynamicVecTrack(translate.asDynamicVectorTrack(), node.positionKeys);
                        case VectorTrack.FixedVectorTrack ->
                                TranmUtil.processFixedVecTrack(translate.asFixedVectorTrack(), node.positionKeys);
                        case VectorTrack.Framed8VectorTrack ->
                                TranmUtil.processFramed8VecTrack(translate.asFramed8VectorTrack(), node.positionKeys);
                        case VectorTrack.Framed16VectorTrack ->
                                TranmUtil.processFramed16VecTrack(translate.asFramed16VectorTrack(), node.positionKeys);
                    }
                } else {
                    node.positionKeys.add(0, new Vector3f(0, 0, 0));
                }
            }


            return animationNodes;
        } else {
            return new AnimationNode[0];
        }
    }

    public static Map<String, Offset> fillTrOffsets(Pair<gg.generations.rarecandy.pokeutils.tranm.TRANMT, TRACM> animationPair) {
        var offsets = new HashMap<String, Offset>();

        if (animationPair.b() != null) {
            var tracm = animationPair.b();

            for (int i = 0; i < tracm.tracksLength(); i++) {
                var tracks = tracm.tracks(i);
                var materialTimeline = tracks.materialAnimation();

                if(materialTimeline == null) continue;

                for (int j = 0; j < materialTimeline.materialTrackLength(); j++) {
                    var materialTrack = materialTimeline.materialTrack(j);

                    for (int k = 0; k < materialTrack.animValuesLength(); k++) {
                        var animValues = materialTrack.animValues(k);

                        var list = animValues.list();

                        offsets.computeIfAbsent(animValues.name(), a -> new GfbAnimation.GfbOffset(
                                toStorage(list.red()),
                                toStorage(list.green()),
                                toStorage(list.blue()),
                                toStorage(list.alpha())));
                    }

                }
            }

//            IntStream.range(0, animationPair.b().tracksLength())
//                    .mapToObj(a -> animationPair.b().tracks(a)).filter(a -> a.materialAnimation() != null)
//                    .flatMap(a -> IntStream.range(0, a.materialAnimation().materialTrackLength())
//                            .mapToObj(b -> a.materialAnimation().materialTrack(b))).collect(
//                            Collectors.toMap(
//                                    TrackMaterial::name,
//                                    b -> IntStream.range(0,
//                                                    b.animValuesLength())
//                                            .mapToObj(b::animValues)
//                                            .distinct().collect(
//                                                    Collectors.toMap(
//                                                            TrackMaterialAnim::name,
//                                                            c -> new GfbAnimation.GfbOffset(
//                                                                    toStorage(c.list().red()),
//                                                                    toStorage(c.list().green()),
//                                                                    toStorage(c.list().blue()),
//                                                                    toStorage(c.list().alpha()))))))
//                    .forEach((k, v) -> {
//                if(v.containsKey("UVScaleOffset")) offsets.put(k, v.get("UVScaleOffset"));
//            });
        }

        return offsets;
    }

    private static TransformStorage<Float> toStorage(TrackMaterialValueList value) {
        var storage = new TransformStorage<Float>();

        for (int i = 0; i < value.valuesLength(); i++) {
            var val = value.values(i);

            storage.add(val.time(), val.value());
        }

        return storage;
    }

    public record TraOffset(TransformStorage<Transform> storage) implements Offset {
        @Override
        public void calcOffset(float animTime, Transform instance) {
            var transform = calcInterpolatedFloat(animTime, storage(), AnimationController.NO_OFFSET);

            instance.offset().set(transform.offset());
            instance.scale().set(transform.scale());
        }
    }
}
