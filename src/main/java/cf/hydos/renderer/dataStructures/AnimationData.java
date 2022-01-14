package cf.hydos.renderer.dataStructures;

/**
 * Contains the extracted data for an animationrenderer.animation, which includes the length of
 * the entire animationrenderer.animation and the data for all the keyframes of the animationrenderer.animation.
 */
public class AnimationData {

    public final float lengthSeconds;
    public final KeyFrameData[] keyFrames;

    public AnimationData(float lengthSeconds, KeyFrameData[] keyFrames) {
        this.lengthSeconds = lengthSeconds;
        this.keyFrames = keyFrames;
    }

}
