package gg.generations.rarecandy.renderer.storage;

import gg.generations.rarecandy.renderer.animation.AnimationController;
import gg.generations.rarecandy.renderer.components.MultiRenderObject;
import gg.generations.rarecandy.renderer.pipeline.traditional.TraditionalPipeline;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import gg.generations.rarecandy.renderer.rendering.RenderStage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectManager {
    private final AnimationController animationController = new AnimationController();
    private final Map<MultiRenderObject, List<ObjectInstance>> objects = new HashMap<>();
    private final List<MultiRenderObject> objectsToRemove = new ArrayList<>();

    public void update(double secondsPassed) {
        for (var entries : objects.entrySet()) {
            var value = entries.getValue();
            var object = entries.getKey();
            object.update();

            for (var objectInstance : value) {
                if (objectInstance instanceof AnimatedObjectInstance animatedObjectInstance) {
                    if (animatedObjectInstance.currentAnimation != null) {
                        animatedObjectInstance.currentAnimation.ensureRegistered(animationController);
                    }
                }

                objectInstance.update(secondsPassed);
            }

            value.removeIf(a -> !a.isLinked());

            if (value.isEmpty()) {
                objectsToRemove.add(object);
            }
        }

        if(!objectsToRemove.isEmpty()) {
            for(var object : objectsToRemove) {
                objects.remove(object);
            }
        }

        animationController.render(secondsPassed);
    }

    public void render(RenderStage stage) {
        for (var entry : objects.entrySet()) {
            var object = entry.getKey();
            if (object == null) continue;

            object.render(stage, entry.getValue());
        }
    }

    public void endFrame() {
        if(!objectsToRemove.isEmpty()) {
            for (var renderObject : objectsToRemove) {
                objects.remove(renderObject);
            }

            objectsToRemove.clear();
        }
    }

    public <T extends ObjectInstance> T add(@NotNull MultiRenderObject object, @NotNull T instance) {
        if(!instance.isLinked()) {
            List<ObjectInstance> list;

            if(objects.containsKey(object)) {
                list = objects.get(object);
            } else {
                list = new ArrayList<>();

                objects.put(object, list);
            }

            instance.link(object);
            list.add(instance);
        }

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
