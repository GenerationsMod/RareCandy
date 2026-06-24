package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.renderer.animation.Animation;
import gg.generations.rarecandy.renderer.animation.AnimationController;
import gg.generations.rarecandy.renderer.animation.AnimationInstance;
import imgui.ImGui;

import java.util.Map;

public final class AnimationPlaybackPanel {
    private final RareCandyCanvas canvas;
    private final float[] progress = new float[1];

    private Animation boundAnimation;
    private double seconds;
    private long lastNanos = System.nanoTime();
    private boolean paused;
    private boolean reversed;

    public AnimationPlaybackPanel(RareCandyCanvas canvas) {
        this.canvas = canvas;
    }

    public void render() {
        ImGui.begin("Animation Playback");

        Animation animation = resolveAnimation();
        if (animation == null) {
            unbind();
            ImGui.text("No animation loaded");
            ImGui.end();
            return;
        }

        bind(animation);
        updateClock(animation);

        double duration = durationSeconds(animation);
        progress[0] = duration > 0.0 ? (float) (seconds / duration) : 0.0f;

        ImGui.text(animationName(animation));
        ImGui.text(String.format("%.3f / %.3f s", seconds, duration));

        if (ImGui.sliderFloat("Progress", progress, 0.0f, 1.0f)) {
            seconds = wrap(animation, duration * progress[0]);
            lastNanos = System.nanoTime();
            paused = true;
        }

        if (ImGui.button(paused ? "Play" : "Pause")) {
            paused = !paused;
            lastNanos = System.nanoTime();
        }
        ImGui.sameLine();
        if (ImGui.button("Rewind")) {
            seconds = 0.0;
            lastNanos = System.nanoTime();
        }
        ImGui.sameLine();
        if (ImGui.button(reversed ? "Forward" : "Reverse")) {
            reversed = !reversed;
            lastNanos = System.nanoTime();
        }
        ImGui.sameLine();
        if (ImGui.button("Reset")) {
            seconds = 0.0;
            paused = false;
            reversed = false;
            lastNanos = System.nanoTime();
        }

        ImGui.end();
    }

    private Animation resolveAnimation() {
        if (canvas.loadedModelInstance != null && canvas.loadedModelInstance.currentAnimation != null) {
            return canvas.loadedModelInstance.currentAnimation.getAnimation();
        }

        if (canvas.loadedModel == null || canvas.currentAnimation == null) {
            return null;
        }

        int id = canvas.loadedModel.animationNameToId.getOrDefault(canvas.currentAnimation, -1);
        return id >= 0 && id < canvas.loadedModel.animations.length ? canvas.loadedModel.animations[id] : null;
    }

    private void bind(Animation animation) {
        if (canvas.loadedModelInstance == null) {
            return;
        }

        AnimationInstance current = canvas.loadedModelInstance.currentAnimation;
        if (current instanceof ControlledAnimationInstance controlled && controlled.owner == this && controlled.getAnimation() == animation) {
            return;
        }

        if (animation != boundAnimation) {
            seconds = 0.0;
            paused = false;
            reversed = false;
            boundAnimation = animation;
        }

        canvas.loadedModelInstance.changeAnimation(new ControlledAnimationInstance(animation, this));
        lastNanos = System.nanoTime();
    }

    private void unbind() {
        boundAnimation = null;
        seconds = 0.0;
        paused = false;
        reversed = false;
        lastNanos = System.nanoTime();
    }

    private void updateClock(Animation animation) {
        long now = System.nanoTime();
        double delta = (now - lastNanos) / 1_000_000_000.0;
        lastNanos = now;

        if (!paused) {
            seconds = wrap(animation, seconds + (reversed ? -delta : delta));
        }
    }

    private String animationName(Animation animation) {
        if (canvas.currentAnimation != null) {
            return canvas.currentAnimation;
        }

        if (canvas.loadedModel != null) {
            for (Map.Entry<String, Integer> entry : canvas.loadedModel.animationNameToId.entrySet()) {
                int id = entry.getValue();
                if (id >= 0 && id < canvas.loadedModel.animations.length && canvas.loadedModel.animations[id] == animation) {
                    return entry.getKey();
                }
            }
        }

        return "<animation>";
    }

    private static double durationSeconds(Animation animation) {
        if (animation == null || animation.animationDuration <= 0.0 || animation.ticksPerSecond == 0.0f) {
            return 0.0;
        }

        return animation.animationDuration / animation.ticksPerSecond;
    }

    private static double wrap(Animation animation, double seconds) {
        double duration = durationSeconds(animation);
        if (duration <= 0.0) {
            return Math.max(0.0, seconds);
        }

        double wrapped = seconds % duration;
        return wrapped < 0.0 ? wrapped + duration : wrapped;
    }

    private static final class ControlledAnimationInstance extends AnimationInstance {
        private final AnimationPlaybackPanel owner;

        private ControlledAnimationInstance(Animation animation, AnimationPlaybackPanel owner) {
            super(animation);
            this.owner = owner;
        }

        @Override
        public void update(double secondsPassed) {
            if (animation == null) {
                matrixTransforms = AnimationController.NO_ANIMATION;
                return;
            }

            updateStart(secondsPassed);
            currentTime = animation.getAnimationTime(owner.seconds);
            matrixTransforms = animation.getFrameTransform(owner.seconds);
        }
    }
}
