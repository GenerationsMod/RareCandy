package gg.generations.rarecandy.renderer.loading;

import gg.generations.rarecandy.pokeutils.Pair;
import gg.generations.rarecandy.pokeutils.tracm.TRACM;
import gg.generations.rarecandy.pokeutils.tranm.QuatTrack;
import gg.generations.rarecandy.pokeutils.tranm.TRANMT;
import gg.generations.rarecandy.pokeutils.tranm.VectorTrack;
import gg.generations.rarecandy.renderer.animation.Animation;
import gg.generations.rarecandy.renderer.animation.Skeleton;
import gg.generations.rarecandy.renderer.animation.TranmUtil;
import org.joml.Vector3f;

public class TranmUtils {
    private static Vector3f ZERO = new Vector3f();

    public static Animation.AnimationNode[] getNodes(Skeleton skeleton, TRANMT tranm) {
        var animationNodes = new Animation.AnimationNode[skeleton.jointMap.size()];


        if (tranm != null) {
            for (int i = 0; i < tranm.getTrack().getTracks().length; i++) {
                var boneAnim = tranm.getTrack().getTracks()[i];

                if(!skeleton.boneIdMap.containsKey(boneAnim.getBoneName())) {
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

//                if(!ignoreScale) {

                var scale = boneAnim.getScale();

                switch (scale.getType()) {
                    case VectorTrack.DynamicVectorTrack -> TranmUtil.processDynamicVecTrack(scale.asDynamicVectorTrack(), node.scaleKeys);
                    case VectorTrack.FixedVectorTrack -> TranmUtil.processFixedVecTrack(scale.asFixedVectorTrack(), node.scaleKeys);
                    case VectorTrack.Framed8VectorTrack -> TranmUtil.processFramed8VecTrack(scale.asFramed8VectorTrack(), node.scaleKeys);
                    case VectorTrack.Framed16VectorTrack -> TranmUtil.processFramed16VecTrack(scale.asFramed16VectorTrack(), node.scaleKeys);
                }
//                } else {
//                    node.scaleKeys.add(0, skeleton.jointMap.get(boneAnim.getBoneName()).poseScale);
//                }


                    var translate = boneAnim.getTranslate();

                    switch (translate.getType()) {
                        case VectorTrack.DynamicVectorTrack -> TranmUtil.processDynamicVecTrack(translate.asDynamicVectorTrack(), node.positionKeys);
                        case VectorTrack.FixedVectorTrack -> TranmUtil.processFixedVecTrack(translate.asFixedVectorTrack(), node.positionKeys);
                        case VectorTrack.Framed8VectorTrack -> TranmUtil.processFramed8VecTrack(translate.asFramed8VectorTrack(), node.positionKeys);
                        case VectorTrack.Framed16VectorTrack -> TranmUtil.processFramed16VecTrack(translate.asFramed16VectorTrack(), node.positionKeys);
                    }
//                }
//                } else {
//                    node.positionKeys.add(0, new Vector3f(0, 0, 0));
//                }
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

    public static long getFps(Pair<TRANMT, TRACM> rawAnimation) {
        if(rawAnimation.a() != null) {
            return rawAnimation.a().getInfo().getAnimationRate();
        } else if(rawAnimation.b() != null) {
            return rawAnimation.b().config().framerate();
        } else {
            return 0L;
        }
    }
}
