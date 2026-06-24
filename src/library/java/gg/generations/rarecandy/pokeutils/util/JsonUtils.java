package gg.generations.rarecandy.pokeutils.util;

import com.google.gson.*;
import gg.generations.rarecandy.pokeutils.CullType;
import gg.generations.rarecandy.pokeutils.IMaterialReference;
import gg.generations.rarecandy.pokeutils.IVariantDetails;
import gg.generations.rarecandy.pokeutils.tranm.Vec3f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class JsonUtils {
    public static JsonElement serializeQuaterion(Quaternionf json) {
        var array = new JsonArray();
        array.add(json.x);
        array.add(json.y);
        array.add(json.z);
        array.add(json.w);
        return array;
    }

    public static JsonElement serializeVector3f(Vector3f json) {
        var array = new JsonArray();
        array.add(json.x);
        array.add(json.y);
        array.add(json.z);
        return array;
    }


    public static Quaternionf deserializeQuaterion(JsonElement json) {
        var vec = new Quaternionf();
        if (json.isJsonArray()) {
            if (json.getAsJsonArray().size() == 3) {
                vec.rotationXYZ(json.getAsJsonArray().get(0).getAsFloat(), json.getAsJsonArray().get(1).getAsFloat(), json.getAsJsonArray().get(2).getAsFloat());
            } else if (json.getAsJsonArray().size() == 4) {
                vec.set(json.getAsJsonArray().get(0).getAsFloat(), json.getAsJsonArray().get(1).getAsFloat(), json.getAsJsonArray().get(2).getAsFloat(), json.getAsJsonArray().get(3).getAsFloat());
            }
        }

        return vec;
    }

    public static <T> List<T> deserializeList(JsonElement element, Function<JsonElement, T> function) {
        var list = new ArrayList<T>();
        element.getAsJsonArray().forEach(value -> list.add(function.apply(value)));
        return list;
    }
    public static List<String> deserializeStringList(JsonElement element) {
        return deserializeList(element, JsonElement::getAsString);
    }

    public static <T> JsonArray serializeList(List<T> list, Function<T, JsonElement> function) {
        var array = new JsonArray();
        list.stream().map(function).forEach(array::add);
        return array;
    }
    public static JsonArray serializeStringList(List<String> list) {
        return serializeList(list, JsonPrimitive::new);
    }


    public static JsonElement serializeColor(Vector3f json) {
        int r = Math.clamp((int) (json.x * 255), 0, 255);
        int g = Math.clamp((int) (json.y * 255), 0, 255);
        int b = Math.clamp((int) (json.z * 255), 0, 255);
        var string = String.format("#%02X%02X%02X", r, g, b);

        return new JsonPrimitive(string);
    }

    public static Vector3f deserializeVector3f(JsonElement json) {
        var vec = new Vector3f();
        if (json.isJsonArray()) {
            if (json.getAsJsonArray().size() == 3) {
                vec.set(json.getAsJsonArray().get(0).getAsFloat(), json.getAsJsonArray().get(1).getAsFloat(), json.getAsJsonArray().get(2).getAsFloat());
            }
        } else if (json.isJsonObject()) {
            var obj = json.getAsJsonObject();

            if (obj.has("x")) vec.x = obj.getAsJsonPrimitive("x").getAsFloat();
            if (obj.has("y")) vec.y = obj.getAsJsonPrimitive("y").getAsFloat();
            if (obj.has("z")) vec.y = obj.getAsJsonPrimitive("z").getAsFloat();
        }


        return vec;
    }

    public static JsonElement serializeVector2f(Vector2f json, JsonSerializationContext ctx) {
        var array = new JsonArray();
        array.add(json.x());
        array.add(json.y());
        return array;
    }

    public static Vector2f deserializeVector2f(JsonElement json) {
        var vec = new Vector2f();
        if (json.isJsonArray()) {
            if (json.getAsJsonArray().size() == 2) {
                vec.set(json.getAsJsonArray().get(0).getAsFloat(), json.getAsJsonArray().get(1).getAsFloat());
            }
        } else if (json.isJsonObject()) {
            var obj = json.getAsJsonObject();

            if (obj.has("x")) vec.x = obj.getAsJsonPrimitive("x").getAsFloat();
            if (obj.has("y")) vec.y = obj.getAsJsonPrimitive("y").getAsFloat();
        }

        return vec;
    }

    public static <T> T extractIfPresent(JsonObject object, String name, T original, Function<JsonElement, T> function) {
        return extractIfPresent(object, name, original, function, false);
    }

    public static <T> T extractIfPresent(JsonObject object, String name, T original, Function<JsonElement, T> function, boolean remove) {
        return object.has(name) ? function.apply((remove ? object.remove(name) : object.get(name))) : original;
    }

    public static <T> void putIf(Predicate<T> predicate, JsonObject object, String name, T value, Function<T, JsonElement> function) {
        if(predicate.test(value)) object.add(name, function.apply(value));
    }

    public static <T> Map<String, T> extractMapIfPresent(JsonObject jsonObject, String name, Function<JsonElement, T> function) {
        JsonObject obj = getJsonObject(jsonObject, name);

        if(obj != null) {

            var map = new LinkedHashMap<String, T>();

            obj.asMap().forEach((key, value) -> map.put(key, function.apply(value)));

            return map;
        } else {
            return new LinkedHashMap<>();
        }
    }

    private static JsonObject getJsonObject(JsonObject jsonObject, String name) {
        return name != null ? jsonObject.has(name) ? jsonObject.getAsJsonObject(name) : null : jsonObject;
    }

    private static JsonObject addJsonObject(JsonObject jsonObject, String name) {
        if (name != null) {
            var obj = new JsonObject();
            jsonObject.add(name, obj);
            return obj;
        } else return jsonObject;
    }



    public static <T> List<T> extractListIfPresent(JsonObject jsonObject, String name, Function<JsonElement, T> function) {
        if(jsonObject.has(name)) {
            var list = new ArrayList<T>();
            jsonObject.getAsJsonArray(name).forEach(value -> list.add(function.apply(value)));
            return list;
        } else {
            return new ArrayList<>();
        }
    }

    public static <T> void putMapIf(JsonObject object, String name, Map<String, T> value, Function<T, JsonElement> function) {
        JsonObject obj = addJsonObject(object, name);

        if(obj != null && !value.isEmpty()) {
            value.forEach((s, t) -> obj.add(s, function.apply(t)));
        }
    }

    public static <T> void putListIf(JsonObject object, String name, List<T> value, Function<T, JsonElement> function) {
        if(!value.isEmpty()) {
            var array = new JsonArray();
            value.forEach(t -> array.add(function.apply(t)));
            object.add(name, array);
        }
    }
}
