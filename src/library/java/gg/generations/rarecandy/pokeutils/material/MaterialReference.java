package gg.generations.rarecandy.pokeutils.material;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import gg.generations.rarecandy.pokeutils.util.Codecs;
import gg.generations.rarecandy.pokeutils.util.Util;
import gg.generations.rarecandy.renderer.model.material.Material;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

public class MaterialReference {
    public static DataResult<Dynamic<?>> encodeValue(Object o) {
        if (o instanceof Boolean value)
            return DataResult.success(JsonOps.INSTANCE.createBoolean(value))
                    .map(json -> new Dynamic<>(JsonOps.INSTANCE, json));
        else if (o instanceof Float value)
            return DataResult.success(JsonOps.INSTANCE.createFloat(value))
                    .map(json -> new Dynamic<>(JsonOps.INSTANCE, json));
        else if (o instanceof Vector3f vec)
            return COLOR_CODEC.encodeStart(JsonOps.INSTANCE, vec)
                    .map(json -> new Dynamic<>(JsonOps.INSTANCE, json));
        else return DataResult.error(() -> "Unsupported value type for encoding: " + o.getClass().getName());
    }

    public static <T> DataResult<Object> decodeValue(Dynamic<T> dynamic) {
        DataResult<Object> boolResult = Codec.BOOL.parse(dynamic).map(Object.class::cast);
        if (boolResult.result().isPresent())
            return boolResult;


        DataResult<Object> floatResult = Codec.FLOAT.parse(dynamic).map(Object.class::cast);
        if (floatResult.result().isPresent())
            return floatResult;

        DataResult<Object> colorResult = COLOR_CODEC.parse(dynamic).map(Object.class::cast);
        if (colorResult.result().isPresent())
            return colorResult;

        return DataResult.error(() -> "Unknown raw value type: " + dynamic);
    }

    public static Codec<Object> VALUE_CODEC = Codec.PASSTHROUGH.flatXmap(MaterialReference::decodeValue, MaterialReference::encodeValue);

    public static Codec<Vector3f> COLOR_CODEC = Codec.STRING.flatXmap(string -> {
        var value = string.replace("#", "");

        try {
            int colorValue = Integer.parseInt(value, 16);
            int r = (colorValue >> 16) & 0xFF;
            int g = (colorValue >> 8) & 0xFF;
            int b = colorValue & 0xFF;
            return DataResult.success(new Vector3f(r / 255f, g / 255f, b / 255f));
        } catch (NumberFormatException ignored) {
            return DataResult.error(() -> "Couldn't parse string.");
        }
    }, vector3f -> DataResult.success(colorToString(vector3f)));

    private static float clamp(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    public static String colorToString(Vector3f color) {
        int r = Math.round(clamp(color.x()) * 255);
        int g = Math.round(clamp(color.y()) * 255);
        int b = Math.round(clamp(color.z()) * 255);
        return String.format("#%02X%02X%02X", r, g, b);
    }

    public static Codec<MaterialReference> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codecs.nullable(Codec.STRING, "parent", a -> a.parent),
            Codecs.nullable(Codec.STRING, "shader", a -> a.shader),
            Codecs.nullable(Codec.STRING, "effect", a -> a.effect),
            Codecs.nullable(Codec.STRING.xmap(Function.identity(), String::toLowerCase).xmap(CullType::from, Enum::name), "cull", a -> a.cull),
            Codecs.nullable(Codec.STRING.xmap(Function.identity(), String::toLowerCase).xmap(BlendType::from, Enum::name), "blend", a -> a.blend),
            Codecs.nullable(Codecs.map(Codec.STRING, Codec.STRING), "images", a -> a.images == null || a.images.isEmpty() ? null : a.images),
            Codecs.nullable(Codecs.map(Codec.STRING, VALUE_CODEC), "values", a -> a.values == null || a.values.isEmpty() ? null : a.values)
    ).apply(instance, (Optional<String> parent, Optional<String> shader, Optional<String> effect, Optional<CullType> cull, Optional<BlendType> blend, Optional<Map<String, String>> images, Optional<Map<String, Object>> values) -> {
        return new MaterialReference(parent.orElse(null), shader.orElse(null), effect.orElse(null), cull.orElse(null), blend.orElse(null), images.orElse(null), values.orElse(null));
    }));


    public String parent;
    public String shader;
    public String effect;

    public CullType cull;

    public BlendType blend;

    public Map<String, String> images;

    public Map<String, Object> values;

    public MaterialReference(String parent, String shader, String effect, CullType cull, BlendType blend, Map<String, String> images, Map<String, Object> values) {
        this.parent = parent;
        this.shader = shader;
        this.effect = effect;
        this.cull = cull;
        this.blend = blend;
        this.images = images;
        this.values = values;
    }

    public static Material process(String name, @NotNull Map<String, MaterialReference> materialreferences, @NotNull Map<String, String> imageMap) {
        var reference = materialreferences.get(name);

        CullType cull = null;
        BlendType blend = null;
        String shader = reference.shader;
        String effect = reference.effect;
        var images = reference.images;
        var values = reference.values;
        var parent = reference.parent;

        if (parent != null) {
            while (parent != null) {
                reference = materialreferences.get(parent);

                if (reference == null) {
                    parent = null;
                } else {
                    shader = Util.useOrDefault(shader, reference.shader, "solid");
                    effect = Util.useOrDefault(effect, reference.effect, "");
                    cull = Util.useOrDefault(cull, reference.cull, CullType.None);
                    blend = Util.useOrDefault(blend, reference.blend, BlendType.None);
                    images = Util.mergeMaps(images, reference.images);
                    values = Util.mergeMaps(values, reference.values);

                    parent = reference.parent;
                }
            }
        } else {
            cull = Util.defaultIfNull(reference.cull, CullType.None);
            blend = Util.defaultIfNull(reference.blend, BlendType.None);
            images = Util.defaultIfNull(reference.images, Collections.emptyMap());
            values = Util.defaultIfNull(reference.values, Collections.emptyMap());
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

        return new Material(name, map, values, cull, blend, shader + (effect != null ? "_" + effect : ""));
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
        if (!compareImages(images, that.images)) return false;

        // Compare the values map
        if (!compareValues(values, that.values)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(parent, shader, effect, cull, blend);
        result = 31 * result + hashImages(images);
        result = 31 * result + hashValues(values);
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
            return Arrays.hashCode(new float[]{((Vector3f) o).x, ((Vector3f) o).y, ((Vector3f) o).z});
        }
        if (o instanceof Vector4f) {
            return Arrays.hashCode(new float[]{((Vector4f) o).x, ((Vector4f) o).y, ((Vector4f) o).z, ((Vector4f) o).w});
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
