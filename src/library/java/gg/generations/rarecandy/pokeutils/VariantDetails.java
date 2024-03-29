package gg.generations.rarecandy.pokeutils;

import org.joml.Vector2f;

public record VariantDetails(String material, Boolean hide, Vector2f offset) {
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

