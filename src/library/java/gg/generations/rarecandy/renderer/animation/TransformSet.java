package gg.generations.rarecandy.renderer.animation;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import org.joml.Vector2f;

import javax.xml.crypto.dsig.TransformService;

public record TransformSet(
        Transform diffuse,
        Transform layer,
        Transform mask,
        Transform emission
) {
    public static final int SIZE = Transform.SIZE * 4;

    public static final TransformSet DEFAULT = new TransformSet(null, null, null, null);

    public boolean isUnit() {
        return (diffuse == null || diffuse.isUnit()) &&
                (layer == null || layer.isUnit()) &&
                        (mask == null || mask.isUnit()) &&
                                (emission == null || emission.isUnit());
    }

    public TransformSet fillIn(TransformSet filler) {
        var newDiffuse = diffuse;
        var newLayer = layer;
        var newMask = mask;
        var newEmission = emission;

        if (newDiffuse == null) newDiffuse = filler.diffuse;
        if (newLayer == null) newLayer = filler.layer;
        if (newMask == null) newMask = filler.mask;
        if (newEmission == null) newEmission = filler.emission;

        return new TransformSet(newDiffuse, newLayer, newMask, newEmission);
    }

    public static JsonElement serialize(TransformSet transformSet, JsonSerializationContext ctx) {
        var obj = new JsonObject();
        if (transformSet.diffuse != null) obj.add("diffuse", ctx.serialize(transformSet.diffuse));
        if (transformSet.layer != null) obj.add("layer", ctx.serialize(transformSet.layer));
        if (transformSet.mask != null) obj.add("mask", ctx.serialize(transformSet.mask));
        if (transformSet.emission != null) obj.add("emission", ctx.serialize(transformSet.emission));
        return obj;
    }

    public static TransformSet deserialize(JsonElement jsonElement, JsonDeserializationContext ctx) {
        var obj = jsonElement.getAsJsonObject();
        var diffuse = obj.has("diffuse") ? ctx.<Transform>deserialize(obj.get("diffuse"), Transform.class) : null;
        var layer = obj.has("layer") ? ctx.<Transform>deserialize(obj.get("layer"), Transform.class) : null;
        var mask = obj.has("mask") ? ctx.<Transform>deserialize(obj.get("mask"), Transform.class) : null;
        var emission = obj.has("emission") ? ctx.<Transform>deserialize(obj.get("emission"), Transform.class) : null;
        return new TransformSet(diffuse, layer, mask, emission);

    }

    public Transform[] array() {
        return new Transform[] {
                diffuse != null ? diffuse : Transform.DEFAULT,
                layer != null ? layer : Transform.DEFAULT,
                mask != null ? mask : Transform.DEFAULT,
                emission != null ? emission : Transform.DEFAULT
        };
    }
}