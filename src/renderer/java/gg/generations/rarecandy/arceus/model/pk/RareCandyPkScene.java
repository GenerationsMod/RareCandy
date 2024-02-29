package gg.generations.rarecandy.arceus.model.pk;

import java.util.ArrayList;
import java.util.List;

public class RareCandyPkScene<T extends MultiRenderingInstance<?>> {

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