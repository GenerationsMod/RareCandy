package gg.generations.rarecandy.renderer.rendering;

import gg.generations.rarecandy.renderer.LoggerUtil;
import gg.generations.rarecandy.renderer.ThreadSafety;
import gg.generations.rarecandy.renderer.components.MultiRenderObject;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RareCandy {
    private static final Queue<Runnable> TASKS = new ConcurrentLinkedQueue<>();

    private final List<MultiRenderObject> objects = new ArrayList<>();
    private final java.util.Set<MultiRenderObject> objectsToRemove = new java.util.HashSet<>();

    public RareCandy() {
        ThreadSafety.initContextThread();
        var startLoad = System.currentTimeMillis();
        LoggerUtil.print("RareCandy Startup took " + (System.currentTimeMillis() - startLoad) + "ms");
    }

    public static void fatal(String message) {
        throw new RuntimeException("Fatal RareCandy Error! '" + message + "'");
    }

    public static void runLater(Runnable r) {
        TASKS.add(r);
    }

    public void update(double secondsPassed) {
        var task = TASKS.poll();
        while (task != null) {
            task.run();
            task = TASKS.poll();
        }

        for (var object : objects) {
            object.update(secondsPassed);

            if (object.isEmpty()) {
                objectsToRemove.add(object);
            }
        }

        if(!objectsToRemove.isEmpty()) {
            for(var object : objectsToRemove) {
                objects.remove(object);
            }
        }
    }

    public void render(RenderStage stage) {
        for (var object : objects) {
            if (object == null) continue;

            object.render(stage);
        }
    }

    public void clear() {
        for (var obj : objects) {
            try {
                obj.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        objects.clear();
    }

    public <T extends ObjectInstance> T add(@NotNull MultiRenderObject object, @NotNull T instance) {
        object.add(instance);

        if(!objects.contains(object)) objects.add(object);

        return instance;
    }


    public void end() {
        if(!objectsToRemove.isEmpty()) {
            for (var renderObject : objectsToRemove) {
                objects.remove(renderObject);
                try {
                    renderObject.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            objectsToRemove.clear();
        }
    }

    public void remove(MultiRenderObject loadedModel) {
        objects.remove(loadedModel);
    }
}
