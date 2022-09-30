package com.pixelmongenerations.rarecandy.components;

import com.pixelmongenerations.rarecandy.rendering.InstanceState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Used if a .glb has multiple models inside it
 */
public class RenderObjects<T extends MeshRenderObject> extends RenderObject implements Iterable<T> {
    private final List<T> renderObjects = new ArrayList<>();
    public boolean allObjectsAdded = false;

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

        return ready && allObjectsAdded;
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

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return renderObjects.listIterator();
    }
}
