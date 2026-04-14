package gg.generations.rarecandy.pokeutils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import gg.generations.rarecandy.renderer.animation.TransformSet;

public record VariantDetails(String material, String effect, Boolean paradox, Boolean hide, TransformSet transform) {
    public VariantDetails fillIn(VariantDetails filler) {
        var newMaterial = material;
        var newEffect = effect;
        var newParadox = paradox;
        var newHide = hide;
        var newTransform = transform;

        if (newMaterial == null) newMaterial = filler.material;
        if (newEffect == null) newEffect = filler.effect;
        if (newParadox == null) newParadox = filler.paradox != null ? filler.paradox : false;
        if (newHide == null) newHide = filler.hide != null ? filler.hide : false;
        if (newTransform == null) newTransform = filler.transform != null ? filler.transform : null;
        else if(filler != null) newTransform = newTransform.fillIn(filler.transform);
        return new VariantDetails(newMaterial, newEffect, newParadox, newHide, newTransform);
    }

    public VariantDetails fillIn() {
        return fillIn(this);
    }

    public static JsonElement serialize(VariantDetails variantDetails, JsonSerializationContext ctx) {
        var obj = new JsonObject();
        if (variantDetails.material() != null) obj.addProperty("material", variantDetails.material());
        if (variantDetails.hide() != null) obj.addProperty("hide", variantDetails.hide());
        if (variantDetails.paradox() != null) obj.addProperty("paradox", variantDetails.paradox());
        if (variantDetails.transform() != null && !variantDetails.transform().isUnit()) obj.add("transform", ctx.serialize(variantDetails.transform()));
        return obj;
    }

    public static VariantDetails deserialize(JsonElement jsonElement, JsonDeserializationContext ctx) {
        var obj = jsonElement.getAsJsonObject();
        var material = obj.has("material") ? obj.getAsJsonPrimitive("material").getAsString() : null;
        var effect = obj.has("effect") ? obj.getAsJsonPrimitive("effect").getAsString() : null;
        var paradox = obj.has("paradox") ? obj.getAsJsonPrimitive("paradox").getAsBoolean() : null;
        var hide = obj.has("hide") ? obj.getAsJsonPrimitive("hide").getAsBoolean() : null;
        TransformSet offset = obj.has("transform") ? ctx.deserialize(obj.get("transform"), TransformSet.class) : null;
        return new VariantDetails(material, effect, paradox, hide, offset);
    }
}

