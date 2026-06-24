package gg.generations.rarecandy.renderer.animation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gg.generations.rarecandy.pokeutils.IModelConfig;
import gg.generations.rarecandy.pokeutils.util.JsonUtils;

import java.util.Objects;

public interface ITransformSet {
    int SIZE = ITransform.SIZE * 4;

    ITransformSet DEFAULT = new ITransformSet() {
        @Override public ITransform diffuse() { return null; }
        @Override public ITransform layer() { return null; }
        @Override public ITransform mask() { return null; }
        @Override public ITransform emission() { return null; }
    };


    ITransform diffuse();
    ITransform layer();
    ITransform mask();
    ITransform emission();

    default boolean isUnit() {
        return (diffuse() == null || diffuse().isUnit()) &&
                (layer() == null || layer().isUnit()) &&
                (mask() == null || mask().isUnit()) &&
                (emission() == null || emission().isUnit());
    }


    default ITransformSet fillIn(ITransformSet filler) {
        var newDiffuse = diffuse();
        var newLayer = layer();
        var newMask = mask();
        var newEmission = emission();

        if (newDiffuse == null) newDiffuse = filler.diffuse();
        if (newLayer == null) newLayer = filler.layer();
        if (newMask == null) newMask = filler.mask();
        if (newEmission == null) newEmission = filler.emission();

        return IModelConfig.Factory.ACTIVE_FACTORY.createTransformSet(newDiffuse, newLayer, newMask, newEmission);
    }

    default ITransform[] array() {
        return new ITransform[] {
                diffuse() != null ? diffuse() : ITransform.DEFAULT,
                layer() != null ? layer() : ITransform.DEFAULT,
                mask() != null ? mask() : ITransform.DEFAULT,
                emission() != null ? emission() : ITransform.DEFAULT
        };
    }

    static JsonElement serialize(ITransformSet transformSet) {
        var obj = new JsonObject();
        JsonUtils.putIf(Objects::nonNull, obj, "diffuse", transformSet.diffuse(), ITransform::serialize);
        JsonUtils.putIf(Objects::nonNull, obj, "layer", transformSet.layer(), ITransform::serialize);
        JsonUtils.putIf(Objects::nonNull, obj, "mask", transformSet.mask(), ITransform::serialize);
        JsonUtils.putIf(Objects::nonNull, obj, "emission", transformSet.emission(), ITransform::serialize);
        return obj;
    }

    static ITransformSet deserialize(JsonElement jsonElement) {
        var obj = jsonElement.getAsJsonObject();
        var diffuse = JsonUtils.extractIfPresent(obj, "diffuse", null, ITransform::deserialize);
        var layer = JsonUtils.extractIfPresent(obj, "layer", null, ITransform::deserialize);
        var mask = JsonUtils.extractIfPresent(obj, "mask", null, ITransform::deserialize);
        var emission = JsonUtils.extractIfPresent(obj, "emission", null, ITransform::deserialize);
        return IModelConfig.Factory.ACTIVE_FACTORY.createTransformSet(diffuse, layer, mask, emission);
    }
}


