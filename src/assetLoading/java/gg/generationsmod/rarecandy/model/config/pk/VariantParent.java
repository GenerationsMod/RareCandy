package gg.generationsmod.rarecandy.model.config.pk;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public record VariantParent(String inherits, Map<String, VariantDetails> details) {
    public static TypeToken<Map<String, VariantDetails>> DETAILS_MAP = new TypeToken<>() {};
    public static class Serializer implements JsonDeserializer<VariantParent>, JsonSerializer<VariantParent> {

        @Override
        public VariantParent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            var jsonObject = json.getAsJsonObject();

            String inherits = null;

            if(jsonObject.has("inherits")) {
                inherits = jsonObject.remove("inherits").getAsJsonPrimitive().getAsString();
            }

//            System.out.println("Blep: " + jsonObject);

            Map<String, VariantDetails> details = new HashMap<>();
            if(!jsonObject.isEmpty()) {
                for(var entry : jsonObject.asMap().entrySet()) {
                    var key = entry.getKey();

                    var value = entry.getValue().getAsJsonObject();

                    var material = value.has("material") ? value.getAsJsonPrimitive("material").getAsString() : null;
                    var hide = value.has("hide") ? value.getAsJsonPrimitive("hid").getAsBoolean() : null;

                    details.put(key, new VariantDetails(material, hide));
                }
            }

            return new VariantParent(inherits, details);
        }

        @Override
        public JsonElement serialize(VariantParent src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();

            src.details().forEach((k, v) -> object.add(k, context.serialize(v)));

            if(src.inherits() != null) object.addProperty("inherits", src.inherits());

            return object;
        }
    }
}
