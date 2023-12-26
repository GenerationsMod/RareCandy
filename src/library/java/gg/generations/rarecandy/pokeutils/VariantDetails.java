package gg.generations.rarecandy.pokeutils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

public record VariantDetails(String material, Boolean hide) {
    public VariantDetails fillIn(VariantDetails filler) {
        var newMaterial = material;
        var newHide = hide;

        if (newMaterial == null) newMaterial = filler.material;
        if (newHide == null) newHide = filler.hide != null ? filler.hide : false;

        return new VariantDetails(newMaterial, newHide);
    }

    public VariantDetails fillIn() {
        return fillIn(this);
    }
}

