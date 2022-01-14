package cf.hydos.renderer.loaders;

import cf.hydos.renderer.animation.Animation;
import cf.hydos.renderer.animation.JointTransform;
import cf.hydos.renderer.animation.KeyFrame;
import cf.hydos.renderer.animation.Quaternion;
import cf.hydos.renderer.colladaLoader.ColladaLoader;
import cf.hydos.renderer.dataStructures.AnimationData;
import cf.hydos.renderer.dataStructures.JointTransformData;
import cf.hydos.renderer.dataStructures.KeyFrameData;
import cf.hydos.renderer.utils.MyFile;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

/**
 * This class loads up an animationrenderer.animation collada file, gets the information from it,
 * and then creates and returns an {@link Animation} from the extracted data.
 */
public class AnimationLoader {

    /**
     * Loads up a collada animationrenderer.animation file, and returns and animationrenderer.animation created from
     * the extracted animationrenderer.animation data from the file.
     *
     * @param colladaFile - the collada file containing data about the desired
     *                    animationrenderer.animation.
     * @return The animationrenderer.animation made from the data in the file.
     */
    public static Animation loadAnimation(MyFile colladaFile) {
        AnimationData animationData = ColladaLoader.loadColladaAnimation(colladaFile);
        KeyFrame[] frames = new KeyFrame[animationData.keyFrames.length];
        for (int i = 0; i < frames.length; i++) {
            frames[i] = createKeyFrame(animationData.keyFrames[i]);
        }
        return new Animation(animationData.lengthSeconds, frames);
    }

    /**
     * Creates a keyframe from the data extracted from the collada file.
     *
     * @param data - the data about the keyframe that was extracted from the
     *             collada file.
     * @return The keyframe.
     */
    private static KeyFrame createKeyFrame(KeyFrameData data) {
        Map<String, JointTransform> map = new HashMap<String, JointTransform>();
        for (JointTransformData jointData : data.jointTransforms) {
            JointTransform jointTransform = createTransform(jointData);
            map.put(jointData.jointNameId, jointTransform);
        }
        return new KeyFrame(data.time, map);
    }

    /**
     * Creates a joint transform from the data extracted from the collada file.
     *
     * @param data - the data from the collada file.
     * @return The joint transform.
     */
    private static JointTransform createTransform(JointTransformData data) {
        Matrix4f mat = data.jointLocalTransform;
        Vector3f translation = new Vector3f(mat.m30(), mat.m31(), mat.m32());
        Quaternion rotation = Quaternion.fromMatrix(mat);
        return new JointTransform(translation, rotation);
    }

}
