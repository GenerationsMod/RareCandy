package gg.generations.rarecandy.renderer.storage;

import gg.generations.rarecandy.renderer.animation.AnimationController;
import gg.generations.rarecandy.renderer.components.RenderObject;
import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class ObjectManager {
    private final AnimationController animationController = new AnimationController();
    private final Map<RenderObject, List<ObjectInstance>> objects = new HashMap<>();

    public void update(double secondsPassed) {
        for (var objects : objects.values())
            if (!objects.isEmpty())
                for (var objectInstance : objects)
                    if (objectInstance instanceof AnimatedObjectInstance animatedObjectInstance)
                        if (animatedObjectInstance.currentAnimation != null)
                            if (!animationController.playingInstances.contains((animatedObjectInstance.currentAnimation)))
                                animationController.playingInstances.add(animatedObjectInstance.currentAnimation);


        animationController.render(secondsPassed);
    }

    public void render(Predicate<Material> predicate) {
        for (var entry : objects.entrySet()) {
            var object = entry.getKey();

            if (object.isReady()) {
                object.update();
                object.render(predicate, entry.getValue());
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
