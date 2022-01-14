package cf.hydos.renderer.animation;

import java.util.Map;

/**
 * Represents one keyframe of an animationrenderer.animation. This contains the timestamp of the
 * keyframe, which is the time (in seconds) from the start of the animationrenderer.animation when
 * this keyframe occurs.
 * <p>
 * It also contains the desired bone-space transforms of all of the joints in
 * the animated entity at this keyframe in the animationrenderer.animation (i.e. it contains all
 * the joint transforms for the "pose" at this time of the animationrenderer.animation.). The
 * joint transforms are stored in a map, indexed by the name of the joint that
 * they should be applied to.
 */
public class KeyFrame {

    private final float timeStamp;
    private final Map<String, JointTransform> pose;

    /**
     * @param timeStamp      - the time (in seconds) that this keyframe occurs during the
     *                       animationrenderer.animation.
     * @param jointKeyFrames - the local-space transforms for all the joints at this
     *                       keyframe, indexed by the name of the joint that they should be
     *                       applied to.
     */
    public KeyFrame(float timeStamp, Map<String, JointTransform> jointKeyFrames) {
        this.timeStamp = timeStamp;
        this.pose = jointKeyFrames;
    }

    /**
     * @return The time in seconds of the keyframe in the animationrenderer.animation.
     */
    protected float getTimeStamp() {
        return timeStamp;
    }

    /**
     * @return The desired bone-space transforms of all the joints at this
     * keyframe, of the animationrenderer.animation, indexed by the name of the joint that
     * they correspond to. This basically represents the "pose" at this
     * keyframe.
     */
    protected Map<String, JointTransform> getJointKeyFrames() {
        return pose;
    }

}
