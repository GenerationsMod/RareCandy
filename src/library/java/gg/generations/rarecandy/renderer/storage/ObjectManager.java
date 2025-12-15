package gg.generations.rarecandy.renderer.storage;

import gg.generations.rarecandy.renderer.animation.AnimationController;
import gg.generations.rarecandy.renderer.components.RenderObject;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import gg.generations.rarecandy.renderer.rendering.RenderStage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

public class ObjectManager {
    private final AnimationController animationController = new AnimationController();
    private final Map<RenderObject, List<ObjectInstance>> objects = new HashMap<>();
    private final List<RenderObject> objectsToRemove = new ArrayList<>();

    public void update(double secondsPassed) {


        for (var entries : objects.entrySet()) {
            var value = entries.getValue();
            var object = entries.getKey();
            if(object.isReady()) object.update();

            for (var objectInstance : value) {
                if (objectInstance instanceof AnimatedObjectInstance animatedObjectInstance) {
                    if (animatedObjectInstance.currentAnimation != null) {
                        animatedObjectInstance.currentAnimation.ensureRegistered(animationController);
                    }
                }
                objectInstance.update(secondsPassed);
            }

            value.removeIf(a -> !a.isLinked());

            if(value.isEmpty()) {
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

            if (object.isReady()) {
                object.render(stage, entry.getValue());
            }
        }
    }

    public void endFrame() {
        if(!objectsToRemove.isEmpty()) {
            for (RenderObject renderObject : objectsToRemove) {
                objects.remove(renderObject);
            }

            objectsToRemove.clear();
        }
    }

    public static void render(RenderObject object, ObjectInstance instance) {
        if (object == null) return;

        if (object.isReady()) {
            object.update();
            object.render(instance);
        }
    }

    public <T extends ObjectInstance> T add(@NotNull RenderObject object, @NotNull T instance) {
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
        return instance;
    }

    /**
     * Used within Minecraft to provide easier support for the rendering style they use
     */
    public void clearObjects() {
        objects.clear();
    }
}
