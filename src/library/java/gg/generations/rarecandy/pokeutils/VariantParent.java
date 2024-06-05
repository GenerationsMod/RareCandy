package gg.generations.rarecandy.pokeutils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

public record VariantParent(String inherits, Map<String, VariantDetails> details) {
    public static TypeToken<Map<String, VariantDetails>> DETAILS_MAP = new TypeToken<>() {
    };

    public static class Serializer implements JsonDeserializer<VariantParent>, JsonSerializer<VariantParent> {

        @Override
        public VariantParent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            var jsonObject = json.getAsJsonObject();

            String inherits = null;

            if (jsonObject.has("inherits")) {
                inherits = jsonObject.remove("inherits").getAsJsonPrimitive().getAsString();
            }

            Map<String, VariantDetails> details = context.deserialize(jsonObject, DETAILS_MAP.getType());

            return new VariantParent(inherits, details);
        }

        @Override
        public JsonElement serialize(VariantParent src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();

            src.details().forEach((k, v) -> object.add(k, context.serialize(v)));

            if (src.inherits() != null) object.addProperty("inherits", src.inherits());

            return object;
        }
    }
}
