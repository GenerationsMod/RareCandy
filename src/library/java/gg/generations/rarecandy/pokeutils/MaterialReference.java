package gg.generations.rarecandy.pokeutils;

import com.google.gson.*;
import gg.generations.rarecandy.renderer.model.material.Material;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MaterialReference {
//    public static Codec<MaterialReference> CODEC = RecordCodecBuilder.create(instance -> {
//        instance.group(
//                Codec.STRING.optionalFieldOf("inherits", null).forGetter(a -> a.parent),
//                Codec.STRING.optionalFieldOf("shader", "solid").forGetter(a -> a.shader),
//                CullType.CODEC.optionalFieldOf("cull", CullType.None).forGetter(a -> a.cull),
//                BlendType.CODEC.optionalFieldOf("blend", BlendType.None).forGetter(a -> a.blend)
//
//        )
//    })

    public String parent;
    public String shader;

    public CullType cull;

    public BlendType blend;

    public Map<String, String> images;

    public Map<String, Object> values;

    public MaterialReference(String parent, String shader, CullType cull, BlendType blend, Map<String, String> images, Map<String, Object> values) {
        this.parent = parent;
        this.shader = shader;
        this.cull = cull;
        this.blend = blend;
        this.images = images;
        this.values = values;
    }

    public static Material process(String name, @NotNull Map<String, MaterialReference> materialreferences, @NotNull Map<String, String> imageMap) {
        var reference = materialreferences.get(name);

        var cull = reference.cull;
        var blend = reference.blend;
        var shader = reference.shader;
        var images = new HashMap<>(reference.images);
        var values = new HashMap<>(reference.values);
        var parent = reference.parent;

        while (parent != null) {
            reference = materialreferences.get(parent);

            if(reference == null) parent = null;
            else {

                if (!shader.equals(reference.shader)) {
                    shader = reference.shader;
                }

                if (!cull.equals(reference.cull)) {
                    cull = reference.cull;
                }

                if (!blend.equals(reference.blend)) {
                    blend = reference.blend;
                }

                reference.images.forEach((key, value) -> images.merge(key, value, (old, value1) -> old));
                reference.values.forEach((key, value) -> values.merge(key, value, (old, value1) -> old));

                parent = reference.parent;
            }
        }

        var map = new HashMap<String, String>();
        for (Map.Entry<String, String> a : images.entrySet()) {
            if (imageMap.containsKey(a.getValue())) {
                if (map.put(a.getKey(), imageMap.get(a.getValue())) != null) {
                    throw new IllegalStateException("Duplicate key");
                }
            } else {
                map.put(a.getKey(), a.getValue());
            }
        }
        return new Material(name, map, values, cull, blend, shader);
    }
    public static final class Serializer implements JsonDeserializer<MaterialReference> {
        @Override
        public MaterialReference deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            String shader = "solid";

            CullType cull = CullType.None;

            BlendType blend = BlendType.None;

            Map<String, String> images = new HashMap<>();

            Map<String, Object> values = new HashMap<>();

            var jsonObject = json.getAsJsonObject();

            String parent = jsonObject.has("inherits") ? jsonObject.get("inherits").getAsString() : null;

            if(jsonObject.has("type")) {

                var type = jsonObject.getAsJsonPrimitive("type").getAsString();

                if (jsonObject.has("texture")) {
                    var texture = jsonObject.getAsJsonPrimitive("texture").getAsString();

                    images.put("diffuse", texture);

                    if (type.equals("masked")) {
                        var color = jsonObject.has("color") ? color(jsonObject.get("color")) : new Vector3f(1.0f, 1.0f, 1.0f);
                        shader = "masked";
                        values.put("color", color);
                        images.put("mask", jsonObject.getAsJsonPrimitive("mask").getAsString());
//                        return new MaskReferenceMaterial(texture, jsonObject.getAsJsonPrimitive("mask").getAsString(), color);
                    } else {

                        switch (type) {
                            case "transparent" -> {
                                blend = BlendType.Regular;
                            }
                            case "cull" -> {
                                cull = CullType.Forward;
                            }
                            case "unlit_cull" -> {
                                cull = CullType.Forward;
                                values.put("useLight", false);
                            }
                            case "unlit" -> {
                                values.put("useLight", false);
                            }
                        }
                    }
                }
            } else {
                shader = jsonObject.has("shader") ? jsonObject.getAsJsonPrimitive("shader").getAsString() : "solid";
                cull = jsonObject.has("cull") ? CullType.from(jsonObject.getAsJsonPrimitive("cull").getAsString()) : CullType.None;
                blend = jsonObject.has("blend") ? BlendType.from(jsonObject.getAsJsonPrimitive("blend").getAsString()) : BlendType.None;
                images = jsonObject.has("images") ? images(jsonObject.getAsJsonObject("images")) : new HashMap<>();
                values = jsonObject.has("values") ? values(jsonObject.getAsJsonObject("values")) : new HashMap<>();
            }

            return new MaterialReference(parent, shader, cull, blend, images, values);

//            throw new JsonParseException("Material type %s invalid".formatted(type));
        }
    }

    private static Map<String, String> images(JsonObject images) {
        return images.asMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, a -> a.getValue().getAsString()));
    }

    private static Map<String, Object> values(JsonObject object) {
        var values = new HashMap<String, Object>();

        object.asMap().forEach((key, value) -> {
            if(value.isJsonObject()) {
                if(object.has("type")) {

                    var obj = value.getAsJsonObject();
                    var val = obj.get("value");

                    switch (obj.getAsJsonPrimitive("type").getAsString()) {
                        case "boolean" -> {
                            values.put(key, val.getAsBoolean());
                        }
                        case "color" -> {
                            values.put(key, MaterialReference.color(val));
                        }
                        case "float" -> {
                            values.put(key, val.getAsFloat());
                        }
                    }
                } else {
                    if(object.has("x") && object.has("y") && object.has("z")) {
                        values.put(key, new Vector3f(object.getAsJsonPrimitive("x").getAsFloat(), object.getAsJsonPrimitive("y").getAsFloat(), object.getAsJsonPrimitive("z").getAsFloat()));
                    }
                }
            } else if(value.isJsonPrimitive()) {
                if (value.getAsJsonPrimitive().isBoolean()) values.put(key, value.getAsBoolean());
                else if (value.getAsJsonPrimitive().isNumber()) values.put(key, value.getAsFloat());
                else if (value.getAsJsonPrimitive().isString()) values.put(key, color(value));
            } else if(value.isJsonArray()) values.put(key, color(value));
        });

        return values;
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
