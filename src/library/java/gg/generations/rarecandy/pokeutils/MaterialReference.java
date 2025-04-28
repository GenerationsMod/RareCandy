package gg.generations.rarecandy.pokeutils;

import com.google.gson.*;
import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.model.material.MaterialImages;
import gg.generations.rarecandy.renderer.model.material.MaterialValues;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
    public String effect;

    public CullType cull;

    public BlendType blend;

    public MaterialImages images;

    public MaterialValues values;

    public boolean useDepthTest = true;

    public MaterialReference(String parent, String shader, String effect, CullType cull, BlendType blend, MaterialImages images, MaterialValues values, boolean useDepthTest) {
        this.parent = parent;
        this.shader = shader;
        this.effect = effect;
        this.cull = cull;
        this.blend = blend;
        this.images = images;
        this.values = values;
        this.useDepthTest = useDepthTest;
    }

    public static Material process(String name, @NotNull Map<String, MaterialReference> materialreferences, @NotNull Map<String, String> imageMap) {
        var reference = materialreferences.get(name);

        var cull = reference.cull;
        var blend = reference.blend;
        var shader = reference.shader;
        var effect = reference.effect;
        var images = new MaterialImages().fill(reference.images);
        var values = new MaterialValues().fill(reference.values);
        var useDepthTest = reference.useDepthTest;
        var parent = reference.parent;

        while (parent != null) {
            reference = materialreferences.get(parent);

            if(reference == null) parent = null;
            else {

                if (!Objects.equals(shader, reference.shader)) shader = reference.shader;
                if (!Objects.equals(effect, reference.effect)) effect = reference.effect;
                if (!Objects.equals(cull, reference.cull)) cull = reference.cull;
                if (!Objects.equals(blend, reference.blend)) blend = reference.blend;
                if(useDepthTest != reference.useDepthTest) useDepthTest = reference.useDepthTest;


                //TODO: Check if the parent's values are overriden vs it overriding child.
                images.fill(reference.images);
                values.fill(reference.values);

                parent = reference.parent;
            }
        }

        values.complete();
        images = images.complete().processWithImageMap(imageMap);

        if(shader == null) shader = "solid";

        return new Material(name, images, values, useDepthTest, cull, blend, shader + (effect != null ? "_" + effect : ""));
    }

    public static final class Serializer implements JsonDeserializer<MaterialReference> {
        @Override
        public MaterialReference deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            String shader = null;

            String effect = null;

            CullType cull = CullType.None;

            BlendType blend = BlendType.None;

            MaterialImages images = new MaterialImages();

            MaterialValues values = new MaterialValues();

            boolean useDepthTest = false;

            var jsonObject = json.getAsJsonObject();

            String parent = jsonObject.has("inherits") ? jsonObject.get("inherits").getAsString() : jsonObject.has("parent") ? jsonObject.get("parent").getAsString() : null;

            if(jsonObject.has("type")) {

                var type = jsonObject.getAsJsonPrimitive("type").getAsString();

                if (jsonObject.has("texture")) {
                    var texture = jsonObject.getAsJsonPrimitive("texture").getAsString();

                    images.setDiffuse(texture);

                    if (type.equals("masked")) {
                        var color = jsonObject.has("color") ? color(jsonObject.get("color")) : new Vector3f(1.0f, 1.0f, 1.0f);
                        shader = "masked";
                        values.setBaseColor1(color);
                        images.setMask(jsonObject.getAsJsonPrimitive("mask").getAsString());
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
                                values.setUseLight(false);
                            }
                            case "unlit" -> {
                                values.setUseLight(false);
                            }
                        }
                    }
                }
            } else {
                if(jsonObject.has("shader")) shader = jsonObject.getAsJsonPrimitive("shader").getAsString();
                if(jsonObject.has("effect")) effect = jsonObject.getAsJsonPrimitive("effect").getAsString();
                if(jsonObject.has("cull")) cull = CullType.from(jsonObject.getAsJsonPrimitive("cull").getAsString());
                if(jsonObject.has("blend")) blend = BlendType.from(jsonObject.getAsJsonPrimitive("blend").getAsString());
                if(jsonObject.has("images")) images.fill(jsonObject.getAsJsonObject("images"));

                if(jsonObject.has("values")) {
                    var valuesObj = jsonObject.getAsJsonObject("values");
                    values.fill(valuesObj);
                    if(valuesObj.has("useDepthTest")) useDepthTest = valuesObj.getAsJsonPrimitive("useDepthTest").getAsBoolean();
                }
            }

            return new MaterialReference(parent, shader, effect, cull, blend, images, values, useDepthTest);
        }
    }

//        object.asMap().forEach((key, value) -> {
//            if(value.isJsonObject()) {
//                if(object.has("type")) {
//
//                    var obj = value.getAsJsonObject();
//                    var val = obj.get("value");
//
//                    switch (obj.getAsJsonPrimitive("type").getAsString()) {
//                        case "boolean" -> {
//                            values.put(key, val.getAsBoolean());
//                        }
//                        case "color" -> {
//                            values.put(key, MaterialReference.color(val));
//                        }
//                        case "float" -> {
//                            values.put(key, val.getAsFloat());
//                        }
//                    }
//                } else {
//                    if(object.has("x") && object.has("y") && object.has("z")) {
//                        values.put(key, new Vector3f(object.getAsJsonPrimitive("x").getAsFloat(), object.getAsJsonPrimitive("y").getAsFloat(), object.getAsJsonPrimitive("z").getAsFloat()));
//                    }
//                }
//            } else if(value.isJsonPrimitive()) {
//                if (value.getAsJsonPrimitive().isBoolean()) values.put(key, value.getAsBoolean());
//                else if (value.getAsJsonPrimitive().isNumber()) values.put(key, value.getAsFloat());
//                else if (value.getAsJsonPrimitive().isString()) values.put(key, color(value));
//            } else if(value.isJsonArray()) values.put(key, color(value));
//        });
//
//        return values;
//    }


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

    // Tolerance for floating-point comparison (epsilon)
    private static final float EPSILON = 1e-6f;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MaterialReference that = (MaterialReference) o;

        // Compare simple fields
        if (!Objects.equals(parent, that.parent)) return false;
        if (!Objects.equals(shader, that.shader)) return false;
        if (!Objects.equals(effect, that.effect)) return false;
        if (!Objects.equals(cull, that.cull)) return false;
        if (!Objects.equals(blend, that.blend)) return false;

        // Compare the images map based on byte content
        if (!Objects.equals(images, that.images)) return false;

        // Compare the values map
        if (!Objects.equals(values, that.values)) return false;

        return useDepthTest != that.useDepthTest;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(parent, shader, effect, cull, blend, values, images, useDepthTest);
        return result;
    }

    // Helper method to compare images based on exact byte content of key-value pairs
    private boolean compareImages(Map<String, String> images1, Map<String, String> images2) {
        if (images1 == images2) return true;
        if (images1 == null || images2 == null || images1.size() != images2.size()) return false;

        for (Map.Entry<String, String> entry : images1.entrySet()) {
            String key1 = entry.getKey();
            String value1 = entry.getValue();
            String value2 = images2.get(key1);

            // Compare key and value byte arrays
            if (value2 == null ||
                    !Arrays.equals(key1.getBytes(StandardCharsets.UTF_8), key1.getBytes(StandardCharsets.UTF_8)) ||
                    !Arrays.equals(value1.getBytes(StandardCharsets.UTF_8), value2.getBytes(StandardCharsets.UTF_8))) {
                return false;
            }
        }
        return true;
    }

    // Helper method to hash images based on byte content
    private int hashImages(Map<String, String> images) {
        if (images == null) return 0;
        return images.entrySet().stream()
                .mapToInt(e -> Arrays.hashCode(e.getKey().getBytes(StandardCharsets.UTF_8)) ^
                        Arrays.hashCode(e.getValue().getBytes(StandardCharsets.UTF_8)))
                .sum();
    }

    // Helper method to compare values map
    private boolean compareValues(Map<String, Object> values1, Map<String, Object> values2) {
        if (values1 == values2) return true;
        if (values1 == null || values2 == null || values1.size() != values2.size()) return false;

        for (Map.Entry<String, Object> entry : values1.entrySet()) {
            String key1 = entry.getKey();
            Object value1 = entry.getValue();
            Object value2 = values2.get(key1);

            if (value2 == null || !deepEquals(value1, value2)) {
                return false;
            }
        }
        return true;
    }

    // Helper method to deeply compare objects in values map
    private boolean deepEquals(Object o1, Object o2) {
        if (o1 == o2) return true;
        if (o1 == null || o2 == null) return false;

        // Handle specific types
        if (o1 instanceof Vector3f && o2 instanceof Vector3f) {
            return compareVector3f((Vector3f) o1, (Vector3f) o2);
        }
        if (o1 instanceof Vector4f && o2 instanceof Vector4f) {
            return compareVector4f((Vector4f) o1, (Vector4f) o2);
        }
        if (o1 instanceof Boolean && o2 instanceof Boolean) {
            return o1.equals(o2); // Booleans can be compared directly
        }
        if (o1 instanceof Number && o2 instanceof Number) {
            return ((Number) o1).doubleValue() == ((Number) o2).doubleValue(); // Compare numbers as doubles
        }
        if (o1 instanceof String && o2 instanceof String) {
            // Compare strings by byte content
            return Arrays.equals(((String) o1).getBytes(StandardCharsets.UTF_8),
                    ((String) o2).getBytes(StandardCharsets.UTF_8));
        }

        // Default to regular equals for other types
        return Objects.equals(o1, o2);
    }

    // Helper method to hash the values map
    private int hashValues(Map<String, Object> values) {
        if (values == null) return 0;
        return values.entrySet().stream()
                .mapToInt(e -> Objects.hash(e.getKey(), deepHashCode(e.getValue())))
                .sum();
    }

    // Helper method to deeply hash objects in values map
    private int deepHashCode(Object o) {
        if (o == null) return 0;

        // Handle specific types
        if (o instanceof Vector3f) {
            return Arrays.hashCode(new float[] { ((Vector3f) o).x, ((Vector3f) o).y, ((Vector3f) o).z });
        }
        if (o instanceof Vector4f) {
            return Arrays.hashCode(new float[] { ((Vector4f) o).x, ((Vector4f) o).y, ((Vector4f) o).z, ((Vector4f) o).w });
        }
        if (o instanceof Boolean) {
            return Boolean.hashCode((Boolean) o);
        }
        if (o instanceof Number) {
            return Double.hashCode(((Number) o).doubleValue());
        }
        if (o instanceof String) {
            // Hash the byte content of the string
            return Arrays.hashCode(((String) o).getBytes(StandardCharsets.UTF_8));
        }

        // Default to regular hashCode for other types
        return o.hashCode();
    }

    // Helper method to compare Vector3f components with an epsilon tolerance
    private boolean compareVector3f(Vector3f v1, Vector3f v2) {
        return Math.abs(v1.x - v2.x) < EPSILON &&
                Math.abs(v1.y - v2.y) < EPSILON &&
                Math.abs(v1.z - v2.z) < EPSILON;
    }

    // Helper method to compare Vector4f components with an epsilon tolerance
    private boolean compareVector4f(Vector4f v1, Vector4f v2) {
        return Math.abs(v1.x - v2.x) < EPSILON &&
                Math.abs(v1.y - v2.y) < EPSILON &&
                Math.abs(v1.z - v2.z) < EPSILON &&
                Math.abs(v1.w - v2.w) < EPSILON;
    }
}
