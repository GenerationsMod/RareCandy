package gg.generations.rarecandy.renderer.animation;

import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

/**
 * Instance of an animation.
 */
public class AnimationInstance {

    public double startTime = -1;
    public Matrix4f[] matrixTransforms;
    public final Map<String, Transform> offsets = new HashMap<>();
    private boolean registered = false;

    protected Animation animation;
    protected float currentTime;
    protected double timeAtPause;
    protected double timeAtUnpause;
    private boolean paused;
    private boolean unused;

    public AnimationInstance(Animation animation) {
        this.animation = animation;

        if(animation != null) animation.offsets.keySet().forEach(k -> offsets.put(k, new Transform()));
    }

    public void update(double secondsPassed) {
        updateStart(secondsPassed);

        if (!paused) {
            if (timeAtUnpause == -1) timeAtUnpause = secondsPassed - timeAtPause;
            float prevTime = currentTime;
            currentTime = animation.getAnimationTime(secondsPassed - timeAtUnpause);
            if (prevTime > currentTime) onLoop();
        } else if (timeAtPause == -1) timeAtPause = secondsPassed;
    }

    public void updateStart(double secondsPassed) {
        if (timeAtUnpause == 0) timeAtUnpause = secondsPassed;
        if (startTime == -1) startTime = secondsPassed;
    }

    public void pause() {
        paused = true;
        timeAtPause = -1;
    }

    public void unpause() {
        paused = false;
        timeAtUnpause = -1;
    }

    public void onLoop() {
    }

    public float getCurrentTime() {
        return currentTime;
    }

    public boolean isPaused() {
        return paused;
    }

    public void destroy() {
        this.unused = true;
        this.registered = false;
    }

    public boolean shouldDestroy() {
        return unused;
    }

    public Animation getAnimation() {
        return animation;
    }

    public Transform getOffset(String name) {
        var offset = offsets.get(name.replaceFirst("shiny_", "")/* Correction factor for now converted swsh models. TODO: More elegant solution.*/);

        return offset;
    }

    /**
     * Ensures this instance is registered with the controller exactly once.
     * O(1) check instead of O(n) contains().
     */
    public void ensureRegistered(AnimationController controller) {
        if (!registered && !unused) {
            controller.playingInstances.add(this);
            registered = true;
        }
    }
}
