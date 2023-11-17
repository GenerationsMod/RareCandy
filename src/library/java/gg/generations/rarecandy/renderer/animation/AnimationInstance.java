package gg.generations.rarecandy.renderer.animation;

import org.joml.Matrix4f;

/**
 * Instance of an animation.
 */
public class AnimationInstance {

    public double startTime = -1;
    public Matrix4f[] matrixTransforms;

    protected Animation animation;
    protected float currentTime;
    protected double timeAtPause;
    protected double timeAtUnpause;
    private boolean paused;
    private boolean unused;

    public AnimationInstance(Animation animation) {
        this.animation = animation;
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
    }

    public boolean shouldDestroy() {
        return unused;
    }

    public Animation getAnimation() {
        return animation;
    }
}
