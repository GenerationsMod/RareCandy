package gg.generations.rarecandy.pokeutils;

import gg.generations.rarecandy.renderer.animation.Transform;

public record VariantDetails(String material, Boolean hide, Transform offset) {
    public VariantDetails fillIn(VariantDetails filler) {
        var newMaterial = material;
        var newHide = hide;
        var newOffset = offset;

        if (newMaterial == null) newMaterial = filler.material;
        if (newHide == null) newHide = filler.hide != null ? filler.hide : false;
        if (newOffset == null) newOffset = filler.offset != null ? filler.offset : null;

        return new VariantDetails(newMaterial, newHide, newOffset);
    }

    public VariantDetails fillIn() {
        return fillIn(this);
    }
}

