package gg.generations.rarecandy.pokeutils;

import gg.generations.rarecandy.renderer.animation.ITransform;
import gg.generations.rarecandy.renderer.animation.ITransformSet;

final class ImTransformSet implements ITransformSet {
    private final NullableTransform diffuse;
    private final NullableTransform layer;
    private final NullableTransform mask;
    private final NullableTransform emission;

    ImTransformSet(ITransform diffuse, ITransform layer, ITransform mask, ITransform emission) {
        this.diffuse = new NullableTransform(diffuse);
        this.layer = new NullableTransform(layer);
        this.mask = new NullableTransform(mask);
        this.emission = new NullableTransform(emission);
    }

    boolean render() {
        var dirty = false;
        dirty |= diffuse.render("Diffuse");
        dirty |= layer.render("Layer");
        dirty |= mask.render("Mask");
        dirty |= emission.render("Emission");
        return dirty;
    }

    @Override public ITransform diffuse() { return diffuse.value(); }
    @Override public ITransform layer() { return layer.value(); }
    @Override public ITransform mask() { return mask.value(); }
    @Override public ITransform emission() { return emission.value(); }
}
