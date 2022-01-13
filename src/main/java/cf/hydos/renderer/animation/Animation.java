package cf.hydos.renderer.animation;

import cf.hydos.renderer.animatedModel.AnimatedModel;

/**
 * * Represents an com.thinmatrix.animationrenderer.animation that can applied to an {@link AnimatedModel} . It
 * contains the length of the com.thinmatrix.animationrenderer.animation in seconds, and a list of
 * {@link KeyFrame}s.
 */
public class Animation {

    private final float length;//in seconds
    private final KeyFrame[] keyFrames;

    /**
     * @param lengthInSeconds - the total length of the com.thinmatrix.animationrenderer.animation in seconds.
     * @param frames          - all the keyframes for the com.thinmatrix.animationrenderer.animation, ordered by time of
     *                        appearance in the com.thinmatrix.animationrenderer.animation.
     */
    public Animation(float lengthInSeconds, KeyFrame[] frames) {
        this.keyFrames = frames;
        this.length = lengthInSeconds;
    }

    /**
     * @return The length of the com.thinmatrix.animationrenderer.animation in seconds.
     */
    public float getLength() {
        return length;
    }

    /**
     * @return An array of the com.thinmatrix.animationrenderer.animation's keyframes. The array is ordered based
     * on the order of the keyframes in the com.thinmatrix.animationrenderer.animation (first keyframe of
     * the com.thinmatrix.animationrenderer.animation in array position 0).
     */
    public KeyFrame[] getKeyFrames() {
        return keyFrames;
    }

}
