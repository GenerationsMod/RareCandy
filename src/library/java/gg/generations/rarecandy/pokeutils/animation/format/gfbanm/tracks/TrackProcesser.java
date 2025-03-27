package gg.generations.rarecandy.pokeutils.animation.format.gfbanm.tracks;

import gg.generations.rarecandy.renderer.animation.TransformStorage;

public interface TrackProcesser<T> {
    void process(TransformStorage<T> rotationKeys);
}
