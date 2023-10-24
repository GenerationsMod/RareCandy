package gg.generations.rarecandy.pokeutils;

import com.google.gson.*;
import gg.generations.rarecandy.pokeutils.reader.TextureReference;
import gg.generations.rarecandy.renderer.model.material.Material;

import java.lang.reflect.Type;
import java.util.Map;

public abstract class MaterialReference {
    public abstract Material process(String name, Map<String, TextureReference> images);
    public static final class Serializer implements JsonDeserializer<MaterialReference> {
        @Override
        public MaterialReference deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            var jsonObject = json.getAsJsonObject();

            var type = jsonObject.getAsJsonPrimitive("type").getAsString();

            if (jsonObject.has("texture")) {
                var texture = jsonObject.getAsJsonPrimitive("texture").getAsString();
                switch (type) {
                    case "solid" -> {
                        return new SolidReferenceMaterial(texture);
                    }
                    case "transparent" -> {
                        return new TransparentMaterialReference(texture);
                    }
                    case "cull" -> {
                        var cull = jsonObject.has("cull") ? CullType.from(jsonObject.getAsJsonPrimitive("cull").getAsString()) : CullType.Forward;
                        return new CullMaterialReference(texture, cull);
                    }
                    case "unlit_cull" -> {
                        return new UnlitCullMaterialReference(texture);
                    }
                    case "unlit" -> {
                        return new UnlitMaterialReference(texture);
                    }
                    default -> throw new JsonParseException("Material type %s invalid".formatted(type));
                }

            }

            throw new JsonParseException("Material type %s invalid".formatted(type));
        }
    }
}
