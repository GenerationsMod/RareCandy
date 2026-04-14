package gg.generations.rarecandy.renderer.animation;

import com.google.gson.*;
import gg.generations.rarecandy.renderer.storage.SSBOBuffer;
import org.joml.Vector2f;

import java.nio.ByteBuffer;

public record Transform(Vector2f scale, Vector2f offset) {
    public static final Transform DEFAULT = new Transform();
    public static final int SIZE = Float.BYTES * 4;
    private static final Transform TEMP = new Transform();

    public Transform() {
        this(new Vector2f());
    }

    public Transform(Vector2f offset) {
        this(new Vector2f(1f, 1f), offset);
    }

    public boolean isUnit() {
        return offset.x == 0f && offset.y == 0f && scale.x == 1f && scale.y == 1f;
    }

    public void upload(SSBOBuffer buffer) {
        buffer.put(scale);
        buffer.put(offset);
    }

    public void upload(ByteBuffer buffer) {
        buffer.putFloat(scale.x()).putFloat(scale.y()).putFloat(offset.x()).putFloat(offset.x());
    }

    public static JsonElement serialize(Transform transform, JsonSerializationContext ctx) {
        var array = new JsonArray();
        array.add(transform.scale.x());
        array.add(transform.scale.y());
        array.add(transform.offset.x());
        array.add(transform.offset.y());
        return array;
    }

    public static Transform deserialize(JsonElement jsonElement, JsonDeserializationContext ctx) {
        if (jsonElement.isJsonArray()) {
            var array = jsonElement.getAsJsonArray();
            return new Transform(new Vector2f(
                    array.get(0).getAsFloat(),
                    array.get(1).getAsFloat()
            ), new Vector2f(
                    array.get(2).getAsFloat(),
                    array.get(3).getAsFloat()
            ));
        }
        else {
            var obj = jsonElement.getAsJsonObject();
            Vector2f scale = ctx.deserialize(obj.get("scale"), Vector2f.class);
            Vector2f offset = ctx.deserialize(obj.get("offset"), Vector2f.class);
            return new Transform(scale, offset);
        }
    }
}





















