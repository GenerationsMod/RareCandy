package gg.generations.rarecandy.pokeutils;

import gg.generations.rarecandy.renderer.model.Variant;
import gg.generations.rarecandy.renderer.model.material.Material;
import org.joml.Vector2f;

import java.util.Map;

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

    public Variant process(Map<String, Material> materialMap) {
        return new Variant(materialMap.get(material), hide, offset);

    }
}

