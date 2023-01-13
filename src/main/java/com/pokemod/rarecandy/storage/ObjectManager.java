package com.pokemod.rarecandy.storage;

import com.pokemod.rarecandy.animation.Animation;
import com.pokemod.rarecandy.components.RenderObject;
import com.pokemod.rarecandy.rendering.ObjectInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.*;

public class ObjectManager {
    public static final Matrix4f[] NO_ANIMATION = new Matrix4f[256];
    private final Map<Animation, Integer> inUseAnimations = new HashMap<>();
    private final Map<Animation, Matrix4f[]> frameTransformMap = new HashMap<>();
    private final Map<RenderObject, List<ObjectInstance>> staticObjects = new HashMap<>();
    private final Map<RenderObject, Map<Animation, List<AnimatedInstance>>> animatedObjects = new HashMap<>();

    public void update(double secondsPassed) {
        for (var animation : inUseAnimations.keySet()) {
            frameTransformMap.remove(animation);
            frameTransformMap.put(animation, animation.getFrameTransform(secondsPassed));
        }
    }

    public void render() {
        for (var entry : staticObjects.entrySet()) {
            var object = entry.getKey();

            if (object.isReady()) {
                object.update();
                object.render(entry.getValue());
            }
        }

        for (var entry : animatedObjects.entrySet()) {
            var object = entry.getKey();
            object.update();

            for (var animationEntries : entry.getValue().entrySet()) {
                for (var instance : animationEntries.getValue()) {
                    instance.transforms = frameTransformMap.getOrDefault(instance.currentAnimation, NO_ANIMATION);
                }

                object.render(new ArrayList<>(animationEntries.getValue()));
            }
        }
    }

    public <T extends ObjectInstance> T add(@NotNull RenderObject object, @NotNull T instance) {
        instance.link(object);

        if (instance instanceof AnimatedInstance animatedInstance) {
            animatedObjects.putIfAbsent(object, new HashMap<>());
            var animationStates = animatedObjects.get(object);
            animationStates.putIfAbsent(animatedInstance.currentAnimation, new ArrayList<>());
            animationStates.get(animatedInstance.currentAnimation).add(animatedInstance);
        } else {
            staticObjects.putIfAbsent(object, new ArrayList<>());
            staticObjects.get(object).add(instance);
        }

        return instance;
    }

    /**
     * Called if you want to remove the memory of the last calculated frame for an animation
     */
    public void cleanStorage(@NotNull Animation animation) {
        frameTransformMap.remove(animation);
    }

    /**
     * Used to manually change the use count of an animation. This is useful for example if you want to disable animations at a further distance and the animation can stay on a single frame without issue
     */
    public void decrementUse(@NotNull Animation animation) {
        if (inUseAnimations.containsKey(animation)) {
            int uses = inUseAnimations.remove(animation) - 1;
            if (uses > 0) inUseAnimations.put(animation, uses);
        }
    }

    /**
     * Used after {@link ObjectManager#decrementUse(Animation)} to make sure the animation can get updated again
     */
    public void incrementUse(@NotNull Animation animation) {
        if (inUseAnimations.containsKey(animation)) {
            int uses = inUseAnimations.remove(animation) + 1;
            inUseAnimations.put(animation, uses);
        }
    }

    /**
     * Updates information that the oldAnim is used 1 less time and adds newAnim to the updating animation map
     *
     * @param instance the objected that is changing animation
     * @param newAnim  the animation that is now playing
     */
    public void changeAnimation(@NotNull AnimatedInstance instance, @NotNull Animation newAnim) {
        changeAnimation(instance.currentAnimation, newAnim);
        instance.currentAnimation = newAnim;
    }

    /**
     * Updates information that the oldAnim is used 1 less time and adds newAnim to the updating animation map
     *
     * @param oldAnim the animation that used to be played
     * @param newAnim the animation that is now playing
     */
    public void changeAnimation(@Nullable Animation oldAnim, @NotNull Animation newAnim) {
        if (inUseAnimations.containsKey(oldAnim)) {
            int uses = inUseAnimations.remove(oldAnim) - 1;
            if (uses > 0) inUseAnimations.put(oldAnim, uses);
        }

        inUseAnimations.put(newAnim, 1);
    }

    /**
     * Used within Minecraft to provide easier support for the rendering style they use
     */
    public void clearObjects() {
        staticObjects.clear();
        animatedObjects.clear();
    }

    static {
        var identity = new Matrix4f().identity();
        Arrays.fill(NO_ANIMATION, identity);
    }
}
