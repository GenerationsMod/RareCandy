package com.pixelmongenerations.rarecandy.components;

import com.pixelmongenerations.rarecandy.rendering.InstanceState;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Used if a .glb has multiple models inside it
 */
public class RenderObjects<T extends RenderObject> extends RenderObject implements Iterable<T> {
    private final List<T> renderObjects = new ArrayList<>();

    @Override
    public void render(List<InstanceState> instances) {
        for (var renderObject : this.renderObjects) {
            renderObject.render(instances);
        }
    }

    public boolean isReady() {
        if (!ready) {
            for (var renderObject : this.renderObjects) {
                if (!renderObject.isReady()) break;
                ready = true;
            }
        }

        return ready;
    }

    @Override
    public void update() {
        for (var renderObject : this.renderObjects) {
            renderObject.update();
        }
    }

    public void add(T object) {
        this.renderObjects.add(object);
    }

    public void remove(T object) {
        this.renderObjects.remove(object);
    }

    @Override
    public Set<String> availableVariants() {
        return renderObjects.stream().flatMap(a -> a.availableVariants().stream()).collect(Collectors.toSet());
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return renderObjects.listIterator();
    }
}
