package gg.generations.rarecandy.renderer.rendering;

import gg.generations.rarecandy.renderer.components.MultiRenderObject;
import gg.generations.rarecandy.renderer.pipeline.traditional.TraditionalPipeline;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RareCandy {
    private static final Queue<Runnable> TASKS = new ConcurrentLinkedQueue<>();

    private final List<MultiRenderObject> objects = new ArrayList<>();
    private final Set<MultiRenderObject> objectsToRemove = new HashSet<>();

    public RareCandy() {}

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
            if (object.isEmpty()) {
                objectsToRemove.add(object);
            } else {
                object.update(secondsPassed);
            }
        }

        if(!objectsToRemove.isEmpty()) {
            for(var object : objectsToRemove) {
                objects.remove(object);
            }
        }
    }

    public void render(TraditionalPipeline pipeline, StateManager manager) {
        for (var object : objects) {
            if (object == null) continue;

            pipeline.bindModel(object, null);

            render(manager, pipeline, object, RenderStage.SOLID_DEPTH_CULL);
            render(manager, pipeline, object, RenderStage.SOLID_DEPTH_NOCULL);
            render(manager, pipeline, object, RenderStage.SOLID_NODEPTH_CULL);
            render(manager, pipeline, object, RenderStage.SOLID_NODEPTH_NOCULL);
            render(manager, pipeline, object, RenderStage.TRANSPARENT_DEPTH_CULL);
            render(manager, pipeline, object, RenderStage.TRANSPARENT_DEPTH_NOCULL);
            render(manager, pipeline, object, RenderStage.TRANSPARENT_NODEPTH_CULL);
            render(manager, pipeline, object, RenderStage.TRANSPARENT_NODEPTH_NOCULL);
        }
    }

    private void render(StateManager manager, TraditionalPipeline pipeline, MultiRenderObject object, RenderStage renderStage) {
        manager.toggle(renderStage);
        object.render(pipeline, renderStage);
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

    public List<MultiRenderObject> getObjects() {
        return objects;
    }

    public void remove(MultiRenderObject loadedModel) {
        objects.remove(loadedModel);
    }
}
