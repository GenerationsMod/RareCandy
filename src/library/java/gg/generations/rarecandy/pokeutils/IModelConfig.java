package gg.generations.rarecandy.pokeutils;

import com.google.gson.*;
import gg.generations.rarecandy.pokeutils.resource.ResourceReader;
import gg.generations.rarecandy.pokeutils.util.JsonUtils;
import gg.generations.rarecandy.renderer.animation.ITransform;
import gg.generations.rarecandy.renderer.animation.ITransformSet;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public interface IModelConfig {
    Gson GSON = new GsonBuilder().setPrettyPrinting().setLenient().create();

    static IModelConfig deserialize(JsonElement element) {
        var jsonObject = element.getAsJsonObject();

        float scale = JsonUtils.extractIfPresent(jsonObject, "scale", 1.0f, JsonElement::getAsFloat);
        Map<String, IMaterialReference> materials = JsonUtils.extractMapIfPresent(jsonObject, "materials", IMaterialReference::deserialize);;
        Map<String, IVariantDetails> defaultVariant = JsonUtils.extractMapIfPresent(jsonObject, "defaultVariant", IVariantDetails::deserialize);
        Map<String, IVariantParent> variants = JsonUtils.extractMapIfPresent(jsonObject, "variants", IVariantParent::deserialize);
        Map<String, IHideDuringAnimation> hideDuringAnimation = JsonUtils.extractMapIfPresent(jsonObject, "hideDuringAnimation", IHideDuringAnimation::deserialize);
        Map<String, Integer> animationFpsOverride = JsonUtils.extractMapIfPresent(jsonObject, "animationFpsOverride", JsonElement::getAsInt);
        Map<String, Boolean> animationLoopsOverride = JsonUtils.extractMapIfPresent(jsonObject, "animationLoopsOverride", JsonElement::getAsBoolean);
        Map<String, ISkeletalTransform> offsets = JsonUtils.extractMapIfPresent(jsonObject, "offsets", ISkeletalTransform::deserialize);
        Map<String, List<String>> materialsWithSameMaterialAnimation = JsonUtils.extractMapIfPresent(jsonObject, "materialsWithSameMaterialAnimation", JsonUtils::deserializeStringList);
        List<String> ignoreScaleInAnimation = JsonUtils.extractListIfPresent(jsonObject, "ignoreScaleInAnimation", JsonElement::getAsString);
        Map<String, IMeshOptions> modelOptions = JsonUtils.extractMapIfPresent(jsonObject, "modelOptions", IMeshOptions::deserialize);;
        List<String> meshesToRenderFirst = JsonUtils.extractListIfPresent(jsonObject, "meshesToRenderFirst", JsonElement::getAsString);
        Map<String, List<String>> aliases = JsonUtils.extractMapIfPresent(jsonObject, "aliases", JsonUtils::deserializeStringList);
        boolean excludeMeshNamesFromSkeleton = JsonUtils.extractIfPresent(jsonObject, "excludeMeshNamesFromSkeleton", false, JsonElement::getAsBoolean);;
        Integer resolution = JsonUtils.extractIfPresent(jsonObject, "resolution", null, JsonElement::getAsInt);

        return Factory.ACTIVE_FACTORY.create(
                scale, materials,
                defaultVariant, variants,
                hideDuringAnimation, animationFpsOverride,
                animationLoopsOverride, offsets,
                materialsWithSameMaterialAnimation, ignoreScaleInAnimation,
                modelOptions, meshesToRenderFirst,
                aliases, excludeMeshNamesFromSkeleton,
                resolution
        );
    }

    static JsonElement serialize(IModelConfig config) {
        var obj = new JsonObject();
        JsonUtils.putIf(value -> value != 1.0f, obj, "scale", config.scale(), JsonPrimitive::new);
        JsonUtils.putMapIf(obj, "materials", config.materials(), IMaterialReference::serialize);
        JsonUtils.putMapIf(obj, "defaultVariant", config.defaultVariant(), IVariantDetails::serialize);
        JsonUtils.putMapIf(obj, "variants", config.variants(), IVariantParent::serialize);
        JsonUtils.putMapIf(obj, "hideDuringAnimation", config.hideDuringAnimation(), IHideDuringAnimation::serialize);
        JsonUtils.putMapIf(obj, "animationFpsOverride", config.animationFpsOverride(), JsonPrimitive::new);
        JsonUtils.putMapIf(obj, "animationLoopsOverride", config.animationLoopsOverride(), JsonPrimitive::new);
        JsonUtils.putMapIf(obj, "offsets", config.offsets(), ISkeletalTransform::serialize);
        JsonUtils.putMapIf(obj, "materialsWithSameMaterialAnimation", config.materialsWithSameMaterialAnimation(), JsonUtils::serializeStringList);
        JsonUtils.putListIf(obj, "ignoreScaleInAnimation", config.ignoreScaleInAnimation(), JsonPrimitive::new);
        JsonUtils.putMapIf(obj, "modelOptions", config.modelOptions(), IMeshOptions::serialize);
        JsonUtils.putListIf(obj, "meshesToRenderFirst", config.meshesToRenderFirst(), JsonPrimitive::new);
        JsonUtils.putMapIf(obj, "aliases", config.aliases(), JsonUtils::serializeStringList);
        JsonUtils.putIf(value -> value, obj, "excludeMeshNamesFromSkeleton", config.excludeMeshNamesFromSkeleton(), JsonPrimitive::new);
        JsonUtils.putIf(Objects::nonNull, obj, "resolution", config.resolution(), JsonPrimitive::new);
        return obj;
    }

    static IModelConfig from(ResourceReader asset) throws IOException {
        var obj = GSON.fromJson(new InputStreamReader(asset.getInputStream("config.json")), JsonElement.class);

        return IModelConfig.deserialize(obj);
    }

    float scale();
    Map<String, IMaterialReference> materials();
    Map<String, IVariantDetails> defaultVariant();
    Map<String, IVariantParent> variants();
    Map<String, IHideDuringAnimation> hideDuringAnimation();
    Map<String, Integer> animationFpsOverride();
    Map<String, Boolean> animationLoopsOverride();
    Map<String, ISkeletalTransform> offsets();
    Map<String, List<String>> materialsWithSameMaterialAnimation();
    List<String> ignoreScaleInAnimation();
    Map<String, IMeshOptions> modelOptions();
    List<String> meshesToRenderFirst();
    Map<String, List<String>> aliases();
    boolean excludeMeshNamesFromSkeleton();
    Integer resolution();

    public abstract class Factory implements IMaterialReference.Factory {
        public static Factory ACTIVE_FACTORY = ModelConfig.FACTORY;

        public abstract ITransform createTransform(Vector2f scale, Vector2f offset);
        public abstract ITransformSet createTransformSet(ITransform diffuse, ITransform layer, ITransform mask, ITransform emission);
        public abstract IVariantDetails createVariantDetails(String material, String effect, Boolean paradox, Boolean hide, ITransformSet offset);
        public abstract IVariantParent createVariantParent(String inherits, Map<String, IVariantDetails> details);
        public abstract ISkeletalTransform createSkeletalTransform(Vector3f position, Quaternionf rotation);
        public abstract IHideDuringAnimation createHideDuringAnimation(boolean blackList, List<String> animations);

        public abstract IMeshOptions createMeshOptions(boolean invert, List<String> aliases);

        public abstract IModelConfig create(float scale,
                            Map<String, IMaterialReference> materials,
                            Map<String, IVariantDetails> defaultVariant,
                            Map<String, IVariantParent> variants,
                            Map<String, IHideDuringAnimation> hideDuringAnimation,
                            Map<String, Integer> animationFpsOverride,
                            Map<String, Boolean> animationLoopsOverride,
                            Map<String, ISkeletalTransform> offsets,
                            Map<String, List<String>> materialsWithSameMaterialAnimation,
                            List<String> ignoreScaleInAnimation,
                            Map<String, IMeshOptions> modelOptions,
                            List<String> meshesToRenderFirst,
                            Map<String, List<String>> aliases,
                            boolean excludeMeshNamesFromSkeleton,
                            Integer resolution
        );
    }

    default List<String> getMaterialsForAnimation(String trackName) {
        var list = new ArrayList<String>();
        list.add(trackName);

        if(materialsWithSameMaterialAnimation() != null) {
            if(materialsWithSameMaterialAnimation().containsKey(trackName)) {
                list.addAll(materialsWithSameMaterialAnimation().get(trackName));
            }
        }

        return list;
    }
}
