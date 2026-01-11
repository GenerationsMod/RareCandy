package gg.generations.rarecandy.pokeutils;

import gg.generations.rarecandy.renderer.animation.Transform;

public record VariantDetails(String material, String effect, Boolean paradox, Boolean hide, Transform offset) {
    public VariantDetails fillIn(VariantDetails filler) {
        var newMaterial = material;
        var newEffect = effect;
        var newParadox = paradox;
        var newHide = hide;
        var newOffset = offset;

        if (newMaterial == null) newMaterial = filler.material;
        if (newEffect == null) newEffect = filler.effect;
        if (newParadox == null) newParadox = filler.paradox != null ? filler.paradox : false;
        if (newHide == null) newHide = filler.hide != null ? filler.hide : false;
        if (newOffset == null) newOffset = filler.offset != null ? filler.offset : null;

        return new VariantDetails(newMaterial, newEffect, newParadox, newHide, newOffset);
    }

    public VariantDetails fillIn() {
        return fillIn(this);
    }
}

