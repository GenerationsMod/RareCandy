package gg.generations.rarecandy.pokeutils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gg.generations.rarecandy.pokeutils.util.JomlConstants;
import gg.generations.rarecandy.pokeutils.util.JsonUtils;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public interface ISkeletalTransform {
    Vector3f position();
    Quaternionf rotation();

    ISkeletalTransform DEFAULT = new ISkeletalTransform() {
        @Override public Vector3f position() { return JomlConstants.VECTOR3F_ZERO; }
        @Override public Quaternionf rotation() { return JomlConstants.QUATERIONF_ZERO; }
    };

    static JsonElement serialize(ISkeletalTransform skeletalTransform) {
        var obj = new JsonObject();
        JsonUtils.putIf(value -> value != null && !JomlConstants.VECTOR3F_ZERO.equals(value), obj, "position", skeletalTransform.position(), JsonUtils::serializeVector3f);
        JsonUtils.putIf(value -> value != null && !JomlConstants.QUATERIONF_ZERO.equals(value), obj, "rotation", skeletalTransform.rotation(), JsonUtils::serializeQuaterion);
        return obj;
    }

    static ISkeletalTransform deserialize(JsonElement element) {
        var obj = element.getAsJsonObject();
        Vector3f position = JsonUtils.extractIfPresent(obj, "position", JomlConstants.VECTOR3F_ZERO, JsonUtils::deserializeVector3f);
        Quaternionf rotation = JsonUtils.extractIfPresent(obj, "rotation", JomlConstants.QUATERIONF_ZERO, JsonUtils::deserializeQuaterion);
        return IModelConfig.Factory.ACTIVE_FACTORY.createSkeletalTransform(position, rotation);
    }

    default ISkeletalTransform scale(float scale) {
        position().div(scale);
        return this;
    }
}
