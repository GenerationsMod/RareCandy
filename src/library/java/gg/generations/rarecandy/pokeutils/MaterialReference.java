package gg.generations.rarecandy.pokeutils;

import com.google.gson.*;
import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.model.material.MaterialImages;
import gg.generations.rarecandy.renderer.model.material.MaterialValues;
import org.joml.Vector3f;

import java.lang.reflect.Type;
import java.util.*;

public class MaterialReference {
    public String parent;
    public String shader;

    public CullType cull;

    public BlendType blend;

    public MaterialImages images;

    public MaterialValues values;

    public MaterialReference(String parent, String shader, CullType cull, BlendType blend, MaterialImages images, MaterialValues values) {
        this.parent = parent;
        this.shader = shader;
        this.cull = cull;
        this.blend = blend;
        this.images = images;
        this.values = values;
    }

    public void complete(Map<String, MaterialReference> materialReferenceMap) {
        MaterialReference reference;

        while (parent != null) {
            reference = materialReferenceMap.get(parent);

            if(reference == null) parent = null;
            else {

                if (!Objects.equals(shader, reference.shader)) shader = reference.shader;
                if (!Objects.equals(cull, reference.cull)) cull = reference.cull;
                if (!Objects.equals(blend, reference.blend)) blend = reference.blend;

                //TODO: Check if the parent's values are overriden vs it overriding child.
                images.fill(reference.images);
                values.fill(reference.values);

                parent = reference.parent;
            }
        }

        values.complete();
//        images = images.complete();
    }

//    public static long getHandle(ITextureLoader loader, String name) {
//        ITexture tex = loader.getTexture(name);
//
//        return tex == null ? 0L : tex.getSamplerHandle(SamplerPresets.NEAREST_REPEAT);
//    }

    public static Material process(MaterialReference reference, List<String> imageNames) {
        var images = reference.images.toArray(imageNames);

        int method = 0;
        if (reference.shader != null) {
            method = switch (reference.shader) {
                case "layered" -> 1;
                case "masked" -> 2;
                default -> 0;
            };
        }

        return new Material(
                images,
                reference.values,
                reference.cull,
                reference.blend,
                method
        );
    }

    public static final class Serializer implements JsonDeserializer<MaterialReference> {
        @Override
        public MaterialReference deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            String shader = null;

            CullType cull = CullType.None;

            BlendType blend = BlendType.None;

            MaterialImages images = new MaterialImages();

            MaterialValues values = new MaterialValues();

            var jsonObject = json.getAsJsonObject();

            String parent = jsonObject.has("inherits") ? jsonObject.get("inherits").getAsString() : jsonObject.has("parent") ? jsonObject.get("parent").getAsString() : null;

            if(jsonObject.has("type")) {

                var type = jsonObject.getAsJsonPrimitive("type").getAsString();

                if (jsonObject.has("texture")) {
                    var texture = jsonObject.getAsJsonPrimitive("texture").getAsString();

                    images.setDiffuse(texture);

                    if (type.contains("masked")) {
                        var color = jsonObject.has("color") ? color(jsonObject.get("color")) : new Vector3f(1.0f, 1.0f, 1.0f);
                        shader = "masked";
                        values.setBaseColor1(color);
                        images.setMask(jsonObject.getAsJsonPrimitive("mask").getAsString());
//                        return new MaskReferenceMaterial(texture, jsonObject.getAsJsonPrimitive("mask").getAsString(), color);
                    } else {

                        switch (type) {
                            case "transparent" -> blend = BlendType.Regular;
                            case "cull" -> cull = CullType.Forward;
                            case "unlit_cull" -> {
                                cull = CullType.Forward;
                                values.setUseLight(false);
                            }
                            case "unlit" -> values.setUseLight(false);
                        }
                    }
                }
            } else {
                if(jsonObject.has("shader")) {
                    shader = jsonObject.getAsJsonPrimitive("shader").getAsString();

                    switch (shader) {
                        case "masked_paradox" -> {
                            shader = "masked";
                        }
                        case "paradox", "solid_paradox" -> {
                            shader = "solid";
                        }
                    }
                }

                if(jsonObject.has("cull")) cull = CullType.from(jsonObject.getAsJsonPrimitive("cull").getAsString());
                if(jsonObject.has("blend")) blend = BlendType.from(jsonObject.getAsJsonPrimitive("blend").getAsString());
                if(jsonObject.has("images")) images.fill(jsonObject.getAsJsonObject("images"));

                if(jsonObject.has("values")) {
                    var valuesObj = jsonObject.getAsJsonObject("values");
                    values.fill(valuesObj);
                }
            }

            return new MaterialReference(parent, shader, cull, blend, images, values);
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

    // Tolerance for floating-point comparison (epsilon)
//    private static final float EPSILON = 1e-6f;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MaterialReference that = (MaterialReference) o;

        // Compare simple fields
        if (!Objects.equals(parent, that.parent)) return false;
        if (!Objects.equals(shader, that.shader)) return false;
        if (!Objects.equals(cull, that.cull)) return false;
        if (!Objects.equals(blend, that.blend)) return false;

        // Compare the images map based on byte content
        if (!Objects.equals(images, that.images)) return false;

        return Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, shader, cull, blend, values, images);
    }

    // Helper method to compare images based on exact byte content of key-value pairs
//    private boolean compareImages(Map<String, String> images1, Map<String, String> images2) {
//        if (images1 == images2) return true;
//        if (images1 == null || images2 == null || images1.size() != images2.size()) return false;
//
//        for (Map.Entry<String, String> entry : images1.entrySet()) {
//            String key1 = entry.getKey();
//            String value1 = entry.getValue();
//            String value2 = images2.get(key1);
//
//            // Compare key and value byte arrays
//            if (value2 == null ||
//                    !Arrays.equals(key1.getBytes(StandardCharsets.UTF_8), key1.getBytes(StandardCharsets.UTF_8)) ||
//                    !Arrays.equals(value1.getBytes(StandardCharsets.UTF_8), value2.getBytes(StandardCharsets.UTF_8))) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    // Helper method to hash images based on byte content
//    private int hashImages(Map<String, String> images) {
//        if (images == null) return 0;
//        return images.entrySet().stream()
//                .mapToInt(e -> Arrays.hashCode(e.getKey().getBytes(StandardCharsets.UTF_8)) ^
//                        Arrays.hashCode(e.getValue().getBytes(StandardCharsets.UTF_8)))
//                .sum();
//    }
//
//    // Helper method to compare values map
//    private boolean compareValues(Map<String, Object> values1, Map<String, Object> values2) {
//        if (values1 == values2) return true;
//        if (values1 == null || values2 == null || values1.size() != values2.size()) return false;
//
//        for (Map.Entry<String, Object> entry : values1.entrySet()) {
//            String key1 = entry.getKey();
//            Object value1 = entry.getValue();
//            Object value2 = values2.get(key1);
//
//            if (value2 == null || !deepEquals(value1, value2)) {
//                return false;
//            }
//        }
//        return true;
//    }

    // Helper method to deeply compare objects in values map
//    private boolean deepEquals(Object o1, Object o2) {
//        if (o1 == o2) return true;
//        if (o1 == null || o2 == null) return false;
//
//        // Handle specific types
//        return switch (o1) {
//            case Vector3f vector3f when o2 instanceof Vector3f -> compareVector3f(vector3f, (Vector3f) o2);
//            case Vector4f vector4f when o2 instanceof Vector4f -> compareVector4f(vector4f, (Vector4f) o2);
//            case Boolean b when o2 instanceof Boolean -> o1.equals(o2); // Booleans can be compared directly
//
//            case Number number when o2 instanceof Number ->
//                    number.doubleValue() == ((Number) o2).doubleValue(); // Compare numbers as doubles
//
//            case String s when o2 instanceof String ->
//                // Compare strings by byte content
//                    Arrays.equals(s.getBytes(StandardCharsets.UTF_8),
//                            ((String) o2).getBytes(StandardCharsets.UTF_8));
//            default ->
//
//                // Default to regular equals for other types
//                    Objects.equals(o1, o2);
//        };
//
//    }

    // Helper method to deeply hash objects in values map
//    private int deepHashCode(Object o) {
//        return switch (o) {
//            case null -> 0;
//
//            // Handle specific types
//            case Vector3f vector3f -> Arrays.hashCode(new float[]{vector3f.x, vector3f.y, vector3f.z});
//            case Vector4f vector4f -> Arrays.hashCode(new float[]{vector4f.x, vector4f.y, vector4f.z, vector4f.w});
//            case Boolean b -> Boolean.hashCode(b);
//            case Number number -> Double.hashCode(number.doubleValue());
//            case String s ->
//                // Hash the byte content of the string
//                    Arrays.hashCode(s.getBytes(StandardCharsets.UTF_8));
//            default ->
//
//                // Default to regular hashCode for other types
//                    o.hashCode();
//        };
//
//    }

    // Helper method to compare Vector3f components with an epsilon tolerance
//    private boolean compareVector3f(Vector3f v1, Vector3f v2) {
//        return Math.abs(v1.x - v2.x) < EPSILON &&
//                Math.abs(v1.y - v2.y) < EPSILON &&
//                Math.abs(v1.z - v2.z) < EPSILON;
//    }

    // Helper method to compare Vector4f components with an epsilon tolerance
//    private boolean compareVector4f(Vector4f v1, Vector4f v2) {
//        return Math.abs(v1.x - v2.x) < EPSILON &&
//                Math.abs(v1.y - v2.y) < EPSILON &&
//                Math.abs(v1.z - v2.z) < EPSILON &&
//                Math.abs(v1.w - v2.w) < EPSILON;
//    }
}
