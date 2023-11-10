package gg.generations.rarecandy.pokeutils;

import com.google.gson.*;
import gg.generations.rarecandy.pokeutils.reader.TextureReference;
import gg.generations.rarecandy.renderer.model.material.Material;
import org.joml.Vector3f;

import java.lang.reflect.Type;
import java.util.Map;

public abstract class MaterialReference {
    public abstract Material process(String name, Map<String, String> images);
    public static final class Serializer implements JsonDeserializer<MaterialReference> {
        @Override
        public MaterialReference deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            var jsonObject = json.getAsJsonObject();

            var type = jsonObject.getAsJsonPrimitive("type").getAsString();

            if (jsonObject.has("texture")) {
                var texture = jsonObject.getAsJsonPrimitive("texture").getAsString();

                if(type.equals("masked")) {
                    var color = jsonObject.has("color") ? color(jsonObject.get("color")) : new Vector3f(1.0f, 1.0f, 1.0f);

                    return new MaskReferenceMaterial(texture, jsonObject.getAsJsonPrimitive("mask").getAsString(), color);
                }

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

    public static Vector3f color(JsonElement element) {
        if(element.isJsonArray()) {
            var array = element.getAsJsonArray();
            return new Vector3f(array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat());
        } else if(element.isJsonPrimitive()){
            int colorValue = Integer.parseInt(element.getAsString().replace("#", ""), 16);

            // Extract individual R, G, and B components
            int red = (colorValue >> 16) & 0xFF;
            int green = (colorValue >> 8) & 0xFF;
            int blue = colorValue & 0xFF;

            // Normalize the RGB components to a range of 0.0 to 1.0 for Vector3f
            float r = red / 255.0f;
            float g = green / 255.0f;
            float b = blue / 255.0f;

            // Create and return the Vector3f object
            return new Vector3f(r, g, b);
        } else {
            return new Vector3f(1.0f, 1.0f, 1.0f);
        }
    }
}
