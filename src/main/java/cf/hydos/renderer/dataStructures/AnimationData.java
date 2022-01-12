package cf.hydos.renderer.dataStructures;

/**
 * Contains the extracted data for an com.thinmatrix.animationrenderer.animation, which includes the length of
 * the entire com.thinmatrix.animationrenderer.animation and the data for all the keyframes of the com.thinmatrix.animationrenderer.animation.
 * 
 * @author Karl
 *
 */
public class AnimationData {

	public final float lengthSeconds;
	public final KeyFrameData[] keyFrames;

	public AnimationData(float lengthSeconds, KeyFrameData[] keyFrames) {
		this.lengthSeconds = lengthSeconds;
		this.keyFrames = keyFrames;
	}

}
