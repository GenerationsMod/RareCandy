package gg.generations.rarecandy.pokeutils;

import com.google.gson.*;
import gg.generations.rarecandy.pokeutils.util.JsonUtils;

import java.util.Map;
import java.util.Objects;

public interface IVariantParent {
    String inherits();

    Map<String, IVariantDetails> details();

    static IVariantParent deserialize(JsonElement json) throws JsonParseException {
        var jsonObject = json.getAsJsonObject();

        String parent = JsonUtils.extractIfPresent(jsonObject, "inherits", null, JsonElement::getAsString, true);

        if (parent == null) {
            parent = JsonUtils.extractIfPresent(jsonObject, "parent", null, JsonElement::getAsString, true);
        }

        try {
            Map<String, IVariantDetails> details = JsonUtils.extractMapIfPresent(jsonObject, null, IVariantDetails::deserialize);
            return IModelConfig.Factory.ACTIVE_FACTORY.createVariantParent(parent, details);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static JsonElement serialize(IVariantParent src) {
        JsonObject object = new JsonObject();

        JsonUtils.putMapIf(object, null, src.details(), IVariantDetails::serialize);
        JsonUtils.putIf(Objects::nonNull, object, "parent", src.inherits(), JsonPrimitive::new);
        return object;
    }
}
