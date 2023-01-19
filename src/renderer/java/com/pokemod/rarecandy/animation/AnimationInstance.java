package com.pokemod.rarecandy.animation;

import org.joml.Matrix4f;

/**
 * Instance of an animation.
 */
public class AnimationInstance {

    public final Animation animation;
    private final Runnable onLoop;
    public double startTime = -1;
    private float currentTime;
    private double timeAtPause;
    private double timeAtUnpause;
    private boolean paused;
    private boolean unused;
    public Matrix4f[] matrixTransforms;

    public AnimationInstance(Animation animation, Runnable onLoop) {
        this.animation = animation;
        this.onLoop = onLoop;
    }

    public void update(double secondsPassed) {
        if (!paused) {
            if (timeAtUnpause == -1) timeAtUnpause = secondsPassed - timeAtPause;
            float prevTime = currentTime;
            currentTime = animation.getAnimationTime(secondsPassed - timeAtUnpause);

            if(prevTime > currentTime && onLoop != null) onLoop.run();
        } else {
            if (timeAtPause == -1) {
                timeAtPause = secondsPassed;
            }
        }
    }

    public void pause() {
        paused = true;
        timeAtPause = -1;
    }

    public void unpause() {
        paused = false;
        timeAtUnpause = -1;
    }

    public float getCurrentTime() {
        return currentTime;
    }

    public boolean isPaused() {
        return paused;
    }

    public void destroy() {
        this.unused = true;
    }

    public boolean shouldDestroy() {
        return unused;
    }
}
