package com.pokemod.rarecandy.animation;

import java.util.Map;

/**
 * Used for animations which have a start, loop, and end.
 */
public class ThreeStageAnimationInstance extends AnimationInstance {

    private final Animation enterAnim;
    private final Animation loopAnim;
    private final Animation endAnim;
    private Animation nextAnim;
    private Runnable finish;

    public ThreeStageAnimationInstance(Map<String, Animation> animationMap, String enterAnim, String loopAnim, String endAnim, String... fallbacks) {
        super(null);
        this.enterAnim = animationMap.getOrDefault(enterAnim, findFallback(animationMap, fallbacks));
        this.loopAnim = animationMap.getOrDefault(loopAnim, findFallback(animationMap, fallbacks));
        this.endAnim = animationMap.getOrDefault(endAnim, findFallback(animationMap, fallbacks));
        this.animation = this.enterAnim;
    }

    @Override
    public void updateStart(double secondsPassed) {
        if (finish != null && animation != endAnim && nextAnim == null) { // if set to finish, the animation is not already set to exit, and if the animation is not at the loop stage yet
            animation = endAnim;
            reset(secondsPassed);
        } else if (nextAnim != null) {
            animation = nextAnim;
            reset(secondsPassed);
            nextAnim = null;
        }

        super.updateStart(secondsPassed);
    }

    @Override
    public void onLoop() {
        if (animation == enterAnim) nextAnim = loopAnim;
        if (animation == endAnim) finish.run();
    }

    public void reset(double secondsPassed) {
        startTime = secondsPassed;
        timeAtUnpause = secondsPassed;
        timeAtPause = 0;
        currentTime = animation.getAnimationTime(secondsPassed - timeAtUnpause);
    }

    public void finish(Runnable onFinish) {
        this.finish = onFinish;
    }

    private Animation findFallback(Map<String, Animation> animationMap, String[] fallbacks) {
        for (var fallback : fallbacks)
            if (animationMap.containsKey(fallback)) return animationMap.get(fallback);
        throw new RuntimeException("Failed to find original or any fallback animations. Available Animations: " + animationMap.keySet());
    }
}
