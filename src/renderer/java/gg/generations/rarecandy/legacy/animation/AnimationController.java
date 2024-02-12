package gg.generations.rarecandy.legacy.animation;

import gg.generationsmod.rarecandy.model.animation.Animation;
import gg.generationsmod.rarecandy.model.animation.GfbAnimation;
import gg.generationsmod.rarecandy.model.animation.Transform;
import org.joml.Matrix2f;
import org.joml.Matrix4f;

import java.util.*;

/**
 * Manages all Animations
 */
public class AnimationController {
    public static final Matrix4f[] NO_ANIMATION = new Matrix4f[220];
    public static final Transform NO_OFFSET = new Transform();

    static {
        var identity = new Matrix4f().identity();
        Arrays.fill(NO_ANIMATION, identity);
    }

    public final List<AnimationInstance> playingInstances = new ArrayList<>();
    public final Map<Animation, Matrix4f[]> instanceIgnoringAnimTransforms = new HashMap<>();

    public void render(double globalSecondsPassed) {
        var instancesToRemove = new ArrayList<AnimationInstance>();
        instanceIgnoringAnimTransforms.clear();

        for (var playingInstance : playingInstances) {
            if (playingInstance.animation == null) {
                System.err.println("Animation instance has null animation"); //TODO: Hook into a logger
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
            playingInstance.matrixTransforms = playingInstance.animation.getFrameTransform(globalSecondsPassed);
        }

        playingInstances.removeAll(instancesToRemove);
    }
}
