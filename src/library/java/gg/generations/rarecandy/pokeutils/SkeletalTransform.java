package gg.generations.rarecandy.pokeutils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public record SkeletalTransform(Vector3f position, Quaternionf rotation) {
    public SkeletalTransform() {
        this(new Vector3f(), new Quaternionf());
    }

    static JsonElement serialize(SkeletalTransform skeletalTransform, JsonSerializationContext jsonSerializationContext) {
        var obj = new JsonObject();
        var position = skeletalTransform.position();
        if (position.x != 0 || position.y != 0 || position.z != 0) {
            obj.add("position", jsonSerializationContext.serialize(position));
        }
        var rotation = skeletalTransform.rotation();
        if (rotation.x != 0 || rotation.y != 0 || rotation.z != 0 || rotation.w != 0) {
            obj.add("rotation", jsonSerializationContext.serialize(rotation));
        }
        return obj;
    }

    static SkeletalTransform deserialize(JsonElement element, JsonDeserializationContext context) {
        var obj = element.getAsJsonObject();

        var position = new Vector3f();

        if (obj.has("position")) {
            position = context.deserialize(obj.get("position"), Vector3f.class);
        }

        var rotation = new Quaternionf();

        if (obj.has("rotation")) {
            rotation = context.deserialize(obj.get("rotation"), Quaternionf.class);
        }

        return new SkeletalTransform(position, rotation);
    }

    public SkeletalTransform scale(float scale) {
        position().div(scale);
        return this;
    }
}
