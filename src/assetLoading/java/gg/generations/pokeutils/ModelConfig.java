package gg.generations.pokeutils;

import com.google.gson.*;
import gg.generations.pokeutils.util.ResourceLocation;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ModelConfig {

    public float scale = 1.0f;
    public Map<String, MaterialReference> materials;

    public Map<String, VariantReference> defaultVariant;
    public Map<String, Map<String, VariantReference>> variants;

    public interface MaterialReference {
        JsonObject toJson();

        String type();
    }

    public static class Serializer implements JsonDeserializer<MaterialReference>, JsonSerializer<MaterialReference> {
        @Override
        public MaterialReference deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();

            var type = obj.getAsJsonPrimitive("type").getAsString();

            return switch (type) {
                case "solid" -> new SoliddMaterialReference(obj.getAsJsonPrimitive("texture").getAsString());
                default -> null;
            };
        }

        @Override
        public JsonElement serialize(MaterialReference src, Type typeOfSrc, JsonSerializationContext context) {
            var obj = src.toJson();
            obj.addProperty("type", src.type());

            return obj;
        }
    }
}
