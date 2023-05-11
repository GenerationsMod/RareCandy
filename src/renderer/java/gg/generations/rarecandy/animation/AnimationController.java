package gg.generations.rarecandy.animation;

import org.joml.Matrix4f;

import java.util.*;

/**
 * Manages all Animations
 */
public class AnimationController {
    public static final Matrix4f[] NO_ANIMATION = new Matrix4f[220];
    public final List<AnimationInstance> playingInstances = new ArrayList<>();
    public final Map<Animation, Matrix4f[]> instanceIgnoringAnimTransforms = new HashMap<>();

    public void render(double globalSecondsPassed) {
        var instancesToRemove = new ArrayList<AnimationInstance>();
        instanceIgnoringAnimTransforms.clear();

        for (var playingInstance : playingInstances) {
            if (playingInstance.animation == null) {
                System.err.println("Animation instance has null animation");
                continue;
            }

            if (playingInstance.shouldDestroy()) instancesToRemove.add(playingInstance);
            if (playingInstance.animation.ignoreInstancedTime)
                instanceIgnoringAnimTransforms.put(playingInstance.animation, playingInstance.animation.getFrameTransform(globalSecondsPassed));

            if (instanceIgnoringAnimTransforms.containsKey(playingInstance.animation)) {
                playingInstance.matrixTransforms = instanceIgnoringAnimTransforms.get(playingInstance.animation);
                continue;
            }

            if (playingInstance.startTime == -1) playingInstance.startTime = globalSecondsPassed;
            playingInstance.update(globalSecondsPassed);
            playingInstance.matrixTransforms = playingInstance.animation.getFrameTransform(playingInstance);
        }

        playingInstances.removeAll(instancesToRemove);
    }

    static {
        var identity = new Matrix4f().identity();
        Arrays.fill(NO_ANIMATION, identity);
    }
}
