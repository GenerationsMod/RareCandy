package com.pokemod.rarecandy.animation;

import org.joml.Matrix4f;

/**
 * Instance of an animation.
 */
public class AnimationInstance {

    public final Animation animation;
    public double startTime = -1;
    private float prevTime;
    private float currentTime;
    private double timeAtPause;
    private double timeAtUnpause;
    private boolean paused;
    private boolean unused;
    public Matrix4f[] matrixTransforms;

    public AnimationInstance(Animation animation) {
        this.animation = animation;
    }

    public void update(double secondsPassed) {
        if (!paused) {
            if (timeAtUnpause == -1) timeAtUnpause = secondsPassed - timeAtPause;
            prevTime = currentTime;
            currentTime = animation.getAnimationTime(secondsPassed - timeAtUnpause);

            if(prevTime > currentTime) {
                System.out.println("animation looped");
            }
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
