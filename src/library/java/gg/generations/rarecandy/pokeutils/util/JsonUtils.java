package gg.generations.rarecandy.pokeutils.util;

import com.google.gson.*;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.lang.reflect.Type;

public class JsonUtils {
    public static JsonElement serializeQuaterion(Quaternionf json, JsonSerializationContext ctx) {
        var array = new JsonArray();
        array.add(json.x);
        array.add(json.y);
        array.add(json.z);
        array.add(json.w);
        return array;
    }

    public static Quaternionf deserializeQuaterion(JsonElement json, JsonDeserializationContext ctx) {
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

    public static JsonElement serializeVector3f(Vector3f json, JsonSerializationContext ctx) {
        int r = Math.clamp((int) (json.x * 255), 0, 255);
        int g = Math.clamp((int) (json.y * 255), 0, 255);
        int b = Math.clamp((int) (json.z * 255), 0, 255);
        var string = String.format("#%02X%02X%02X", r, g, b);

        return new JsonPrimitive(string);
    }

    public static Vector3f deserializeVector3f(JsonElement json, JsonDeserializationContext ctx) {
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

    public static Vector2f deserializeVector2f(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
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
}
