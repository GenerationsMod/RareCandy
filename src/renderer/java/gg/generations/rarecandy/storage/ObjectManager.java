package gg.generations.rarecandy.storage;

import gg.generations.rarecandy.animation.AnimationController;
import gg.generations.rarecandy.components.RenderObject;
import gg.generations.rarecandy.rendering.ObjectInstance;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectManager {
    private final AnimationController animationController = new AnimationController();
    private final Map<RenderObject, List<ObjectInstance>> objects = new HashMap<>();

    public void update(double secondsPassed) {
        for (var objects : objects.values()) {
            if (objects.size() > 0) {
                for (var objectInstance : objects) {
                    if (objectInstance instanceof AnimatedObjectInstance animatedObjectInstance) {
                        if (animatedObjectInstance.currentAnimation != null) {
                            if (!animationController.playingInstances.contains((animatedObjectInstance.currentAnimation))) {
                                animationController.playingInstances.add(animatedObjectInstance.currentAnimation);
                            }
                        }
                    }
                }
            }
        }

        animationController.render(secondsPassed);
    }

    public void render() {
        for (var entry : objects.entrySet()) {
            var object = entry.getKey();

            if (object.isReady()) {
                object.update();
                object.render(entry.getValue());
            }
        }
    }

    public <T extends ObjectInstance> T add(@NotNull RenderObject object, @NotNull T instance) {
        instance.link(object);
        objects.putIfAbsent(object, new ArrayList<>());
        objects.get(object).add(instance);
        return instance;
    }

    /**
     * Used within Minecraft to provide easier support for the rendering style they use
     */
    public void clearObjects() {
        objects.clear();
    }
}
