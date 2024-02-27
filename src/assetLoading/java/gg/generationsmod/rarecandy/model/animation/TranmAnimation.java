package gg.generationsmod.rarecandy.model.animation;

import gg.generationsmod.rarecandy.Pair;
import gg.generationsmod.rarecandy.model.animation.tracm.TRACM;
import gg.generationsmod.rarecandy.model.animation.tracm.TrackMaterialAnim;
import gg.generationsmod.rarecandy.model.animation.tracm.TrackMaterialValueList;
import gg.generationsmod.rarecandy.model.animation.tranm.*;
import org.joml.Vector3f;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static gg.generationsmod.rarecandy.model.animation.GfbAnimation.GfbOffset.calcInterpolatedFloat;

public class TranmAnimation extends Animation<Pair<gg.generationsmod.rarecandy.model.animation.tranm.Animation, TRACM>> {
    public TranmAnimation(String name, byte[] tranm, byte[] tracm, Skeleton skeleton) {
        this(name, new Pair<>(tranm != null ? gg.generationsmod.rarecandy.model.animation.tranm.Animation.getRootAsAnimation(ByteBuffer.wrap(tranm)) : null, tracm != null ? TRACM.getRootAsTRACM(ByteBuffer.wrap(tracm)) : null), skeleton);
    }
    public TranmAnimation(String name, Pair<gg.generationsmod.rarecandy.model.animation.tranm.Animation, TRACM> rawAnimation, Skeleton skeleton) {
        super(name, (int) getFps(rawAnimation), skeleton, rawAnimation, TranmAnimation::fillAnimationNodes, TranmAnimation::fillTrOffsets);
    }

    private static long getFps(Pair<gg.generationsmod.rarecandy.model.animation.tranm.Animation, TRACM> rawAnimation) {
        if(rawAnimation.a() != null) {
            return rawAnimation.a().meta().fps();
        } else if(rawAnimation.b() != null) {
            return rawAnimation.b().config().framerate();
        } else {
            return 0L;
        }
    }

    public static AnimationNode[] fillAnimationNodes(Animation<Pair<gg.generationsmod.rarecandy.model.animation.tranm.Animation, TRACM>> animation, Pair<gg.generationsmod.rarecandy.model.animation.tranm.Animation, TRACM> animationPair) {
        var rawAnimation = animationPair.a();

        var animationNodes = new AnimationNode[animation.skeleton.nodes.length]; // BoneGroup

        for (int i = 0; i < rawAnimation.anim().bonesLength(); i++) {
            var boneAnim = rawAnimation.anim().bones(i);
            animation.nodeIdMap.put(Objects.requireNonNull(boneAnim.name()).replace(".trmdl", ""), i);
            animationNodes[i] = new AnimationNode();

            switch (boneAnim.rotType()) {
                case QuatTrack.DynamicQuatTrack ->
                        TranmUtil.processDynamicQuatTrack((DynamicQuatTrack) Objects.requireNonNull(boneAnim.rot(new DynamicQuatTrack())), animationNodes[i].rotationKeys);
                case QuatTrack.FixedQuatTrack ->
                        TranmUtil.processFixedQuatTrack((FixedQuatTrack) Objects.requireNonNull(boneAnim.rot(new FixedQuatTrack())), animationNodes[i].rotationKeys);
                case QuatTrack.Framed8QuatTrack ->
                        TranmUtil.processFramed8QuatTrack((Framed8QuatTrack) Objects.requireNonNull(boneAnim.rot(new Framed8QuatTrack())), animationNodes[i].rotationKeys);
                case QuatTrack.Framed16QuatTrack ->
                        TranmUtil.processFramed16QuatTrack((Framed16QuatTrack) Objects.requireNonNull(boneAnim.rot(new Framed16QuatTrack())), animationNodes[i].rotationKeys);
            }

            switch (boneAnim.scaleType()) {
                case VectorTrack.DynamicVectorTrack ->
                        TranmUtil.processDynamicVecTrack((DynamicVectorTrack) Objects.requireNonNull(boneAnim.scale(new DynamicVectorTrack())), animationNodes[i].scaleKeys);
                case VectorTrack.FixedVectorTrack ->
                        TranmUtil.processFixedVecTrack((FixedVectorTrack) Objects.requireNonNull(boneAnim.scale(new FixedVectorTrack())), animationNodes[i].scaleKeys);
                case VectorTrack.Framed8VectorTrack ->
                        TranmUtil.processFramed8VecTrack((Framed8VectorTrack) Objects.requireNonNull(boneAnim.scale(new Framed8VectorTrack())), animationNodes[i].scaleKeys);
                case VectorTrack.Framed16VectorTrack ->
                        TranmUtil.processFramed16VecTrack((Framed16VectorTrack) Objects.requireNonNull(boneAnim.scale(new Framed16VectorTrack())), animationNodes[i].scaleKeys);
            }


                switch (boneAnim.transType()) {
                    case VectorTrack.DynamicVectorTrack -> TranmUtil.processDynamicVecTrack((DynamicVectorTrack) Objects.requireNonNull(boneAnim.trans(new DynamicVectorTrack())), animationNodes[i].positionKeys);
                    case VectorTrack.FixedVectorTrack -> TranmUtil.processFixedVecTrack((FixedVectorTrack) Objects.requireNonNull(boneAnim.trans(new FixedVectorTrack())), animationNodes[i].positionKeys);
                    case VectorTrack.Framed8VectorTrack -> TranmUtil.processFramed8VecTrack((Framed8VectorTrack) Objects.requireNonNull(boneAnim.trans(new Framed8VectorTrack())), animationNodes[i].positionKeys);
                    case VectorTrack.Framed16VectorTrack -> TranmUtil.processFramed16VecTrack((Framed16VectorTrack) Objects.requireNonNull(boneAnim.trans(new Framed16VectorTrack())), animationNodes[i].positionKeys);
                }


        }

        return animationNodes;
    }



    public static Map<String, Offset> fillTrOffsets(Pair<gg.generationsmod.rarecandy.model.animation.tranm.Animation, TRACM> animationPair) {
        var offsets = new HashMap<String, Offset>();

        if(animationPair.b() != null) {
            IntStream.range(0, animationPair.b().tracksLength()).mapToObj(a -> animationPair.b().tracks(a)).filter(a -> a.materialAnimation() != null).flatMap(a -> IntStream.range(0, a.materialAnimation().materialTrackLength()).mapToObj(b -> a.materialAnimation().materialTrack(b))).collect(Collectors.toMap(b -> b.name(), b -> IntStream.range(0, b.animValuesLength()).mapToObj(b::animValues).collect(Collectors.toMap(TrackMaterialAnim::name, c -> {

                return new GfbAnimation.GfbOffset(toStorage(c.list().red()), toStorage(c.list().green()), toStorage(c.list().blue()), toStorage(c.list().alpha()));
            })))).forEach((k, v) -> {
                if(v.containsKey("UVScaleOffset")) offsets.put(k, v.get("UVScaleOffset"));
            });
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
}
