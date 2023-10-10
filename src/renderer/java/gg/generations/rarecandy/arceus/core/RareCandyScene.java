package gg.generations.rarecandy.arceus.core;

import gg.generations.rarecandy.arceus.model.RenderingInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * Pretty simple but effective scene. Keeps track of the added and removed objects for caching in the Render Graph
 */
public class RareCandyScene<T extends RenderingInstance> {

    private boolean dirty;
    public List<T> addedInstances = new ArrayList<>();
    public List<T> removedInstances = new ArrayList<>();

    public void addInstance(T instance) {
        addedInstances.add(instance);
        this.dirty = true;
    }

    public void removeInstance(T instance) {
        if(instance == null) return;
        removedInstances.add(instance);
        this.dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void markClean() {
        this.dirty = false;
    }
}
