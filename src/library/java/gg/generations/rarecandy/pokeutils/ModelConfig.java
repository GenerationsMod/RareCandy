package gg.generations.rarecandy.pokeutils;

import com.google.gson.*;
import gg.generations.rarecandy.pokeutils.resource.ResourceReader;
import gg.generations.rarecandy.pokeutils.util.JsonUtils;
import gg.generations.rarecandy.renderer.animation.Transform;
import gg.generations.rarecandy.renderer.animation.TransformSet;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class ModelConfig {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().setLenient()
            .registerTypeAdapter(VariantParent.class, new VariantParent.Serializer())
            .registerTypeAdapter(MaterialReference.class, new MaterialReference.Serializer())
            .registerTypeAdapter(Vector2f.class, (JsonDeserializer<Vector2f>) JsonUtils::deserializeVector2f)
            .registerTypeAdapter(VariantDetails.class, new GenericJsonThing<>(VariantDetails::serialize, VariantDetails::deserialize))
            .registerTypeAdapter(Transform.class, new GenericJsonThing<>(Transform::serialize, Transform::deserialize))
            .registerTypeAdapter(TransformSet.class, new GenericJsonThing<>(TransformSet::serialize, TransformSet::deserialize))
            .registerTypeAdapter(Vector3f.class, new GenericJsonThing<>(JsonUtils::serializeVector3f, JsonUtils::deserializeVector3f))
            .registerTypeAdapter(Quaternionf.class, new GenericJsonThing<>(JsonUtils::serializeQuaterion, JsonUtils::deserializeQuaterion))
            .registerTypeAdapter(MeshOptions.class, new GenericJsonThing<>(MeshOptions::serialize, MeshOptions::deserialzie))
            .registerTypeAdapter(SkeletalTransform.class, new GenericJsonThing<>(SkeletalTransform::serialize, SkeletalTransform::deserialize))
            .create();
    public float scale = 1.0f;
    public Map<String, MaterialReference> materials;

    public Map<String, VariantDetails> defaultVariant;
    public Map<String, VariantParent> variants;
    public Map<String, HideDuringAnimation> hideDuringAnimation = Collections.emptyMap();

    public Map<String, Integer> animationFpsOverride;
    public Map<String, Boolean> animationLoopsOverride; //TODO: Collaspse into a animationOverride map when have time

    public Map<String, SkeletalTransform> offsets = new HashMap<>();

    public Map<String, List<String>> materialsWithSameMaterialAnimation;

    public List<String> ignoreScaleInAnimation;

    public Map<String, MeshOptions> modelOptions;

    public List<String> meshesToRenderFirst;

    public Map<String, List<String>> aliases;

    public boolean excludeMeshNamesFromSkeleton = false;

    public Integer resolution;

    public static ModelConfig read(ResourceReader reader) throws IOException {
        return GSON.fromJson(new InputStreamReader(reader.getInputStream("config.json")), ModelConfig.class);
    }

    public List<String> getMaterialsForAnimation(String trackName) {
        var list = new ArrayList<String>();
        list.add(trackName);

        if(materialsWithSameMaterialAnimation != null) {
            if(materialsWithSameMaterialAnimation.containsKey(trackName)) {
                list.addAll(materialsWithSameMaterialAnimation.get(trackName));
            }
        }

        return list;
    }

    public record HideDuringAnimation(boolean blackList, List<String> animations) {
        public static final HideDuringAnimation NONE = new HideDuringAnimation();

        public HideDuringAnimation() {
            this(false, null);
        }

//        public boolean check(Animation animation) {
//            return check(animation != null ? animation.name : null);
//        }

        public boolean check(String animation) {
            if (animations != null)
                return animations.contains(animation) == blackList;
            return false;
        }
    }
}
