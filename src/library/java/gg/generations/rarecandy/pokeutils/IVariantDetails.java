package gg.generations.rarecandy.pokeutils;

import com.google.gson.*;
import gg.generations.rarecandy.pokeutils.util.JsonUtils;
import gg.generations.rarecandy.renderer.animation.ITransformSet;

import java.util.Objects;

public interface IVariantDetails {
    String material();
    String effect();
    Boolean paradox();
    Boolean hide();
    ITransformSet transform();

    static JsonElement serialize(IVariantDetails variantDetails) {
        var obj = new JsonObject();
        JsonUtils.putIf(Objects::nonNull, obj, "material", variantDetails.material(), JsonPrimitive::new);
        JsonUtils.putIf(Objects::nonNull, obj, "hide", variantDetails.hide(), JsonPrimitive::new);
        JsonUtils.putIf(Objects::nonNull, obj, "paradox", variantDetails.paradox(), JsonPrimitive::new);
        JsonUtils.putIf(transform -> transform != null && !transform.isUnit(), obj, "transform", variantDetails.transform(), ITransformSet::serialize);
        return obj;
    }

    static IVariantDetails deserialize(JsonElement jsonElement) {
        var obj = jsonElement.getAsJsonObject();
        var material = JsonUtils.extractIfPresent(obj, "material", null, JsonElement::getAsString);
        var effect = JsonUtils.extractIfPresent(obj, "effect", null, JsonElement::getAsString);
        var paradox = JsonUtils.extractIfPresent(obj, "paradox", null, JsonElement::getAsBoolean);
        var hide = JsonUtils.extractIfPresent(obj, "hide", null, JsonElement::getAsBoolean);
        var offset = JsonUtils.extractIfPresent(obj, "transform", null, ITransformSet::deserialize);
        return IModelConfig.Factory.ACTIVE_FACTORY.createVariantDetails(material, effect, paradox, hide, offset);
    }
}
