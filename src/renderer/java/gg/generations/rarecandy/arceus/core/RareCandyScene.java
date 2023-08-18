package gg.generations.rarecandy.arceus.core;

import gg.generations.rarecandy.arceus.model.RenderingInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * Pretty simple but effective scene. Keeps track of the added and removed objects for caching in the Render Graph
 */
public class RareCandyScene {

    private boolean dirty;
    public List<RenderingInstance> addedInstances = new ArrayList<>();
    public List<RenderingInstance> removedInstances = new ArrayList<>();

    public void addInstance(RenderingInstance instance) {
        addedInstances.add(instance);
        this.dirty = true;
    }

    public void removeInstance(RenderingInstance instance) {
        removedInstances.add(null);
        this.dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void markClean() {
        this.dirty = false;
    }
}
