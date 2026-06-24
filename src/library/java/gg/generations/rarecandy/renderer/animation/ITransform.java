package gg.generations.rarecandy.renderer.animation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import gg.generations.rarecandy.pokeutils.IModelConfig;
import gg.generations.rarecandy.pokeutils.util.JomlConstants;
import gg.generations.rarecandy.pokeutils.util.JsonUtils;
import gg.generations.rarecandy.renderer.storage.SSBOBuffer;
import org.joml.Vector2f;

import java.nio.ByteBuffer;

public interface ITransform {
    int SIZE = Float.BYTES * 4;

    Vector2f scale();
    Vector2f offset();

    ITransform DEFAULT = new ITransform() {
        @Override public Vector2f scale() { return JomlConstants.VECTOR2F_ONE; }
        @Override public Vector2f offset() { return JomlConstants.VECTOR2F_ZERO; }
    };

    default boolean isUnit() {
        return offset().x == 0f && offset().y == 0f && scale().x == 1f && scale().y == 1f;
    }

    default void upload(SSBOBuffer buffer) {
        buffer.put(scale());
        buffer.put(offset());
    }

    default void upload(ByteBuffer buffer) {
        buffer.putFloat(scale().x()).putFloat(scale().y()).putFloat(offset().x()).putFloat(offset().y());
    }

    static JsonElement serialize(ITransform transform) {
        var array = new JsonArray();
        array.add(transform.scale().x());
        array.add(transform.scale().y());
        array.add(transform.offset().x());
        array.add(transform.offset().y());
        return array;
    }

    static ITransform deserialize(JsonElement jsonElement) {
        if (jsonElement.isJsonArray()) {
            var array = jsonElement.getAsJsonArray();
            return IModelConfig.Factory.ACTIVE_FACTORY.createTransform(new Vector2f(
                    array.get(0).getAsFloat(),
                    array.get(1).getAsFloat()
            ), new Vector2f(
                    array.get(2).getAsFloat(),
                    array.get(3).getAsFloat()
            ));
        }
        else {
            var obj = jsonElement.getAsJsonObject();
            var scale = JsonUtils.extractIfPresent(obj, "scale", JomlConstants.VECTOR2F_ONE, JsonUtils::deserializeVector2f);
            var offset = JsonUtils.extractIfPresent(obj, "offset", JomlConstants.VECTOR2F_ZERO, JsonUtils::deserializeVector2f);
            return IModelConfig.Factory.ACTIVE_FACTORY.createTransform(scale, offset);
        }
    }
}
