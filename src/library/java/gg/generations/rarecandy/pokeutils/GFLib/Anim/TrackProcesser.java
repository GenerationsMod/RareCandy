package gg.generations.rarecandy.pokeutils.GFLib.Anim;

import gg.generations.rarecandy.renderer.animation.TransformStorage;

public interface TrackProcesser<T> {
    void process(TransformStorage<T> rotationKeys);
}
