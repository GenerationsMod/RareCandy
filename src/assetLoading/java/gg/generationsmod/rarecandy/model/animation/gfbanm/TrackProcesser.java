package gg.generationsmod.rarecandy.model.animation.gfbanm;

import gg.generationsmod.rarecandy.model.animation.TransformStorage;

public interface TrackProcesser<T> {
    void process(TransformStorage<T> rotationKeys);
}
