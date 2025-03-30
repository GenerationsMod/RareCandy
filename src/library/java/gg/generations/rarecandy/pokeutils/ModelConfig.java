package gg.generations.rarecandy.pokeutils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import gg.generations.rarecandy.pokeutils.codec.ModelConfigCodecs;
import gg.generations.rarecandy.pokeutils.material.MaterialReference;
import gg.generations.rarecandy.pokeutils.material.MeshOptions;
import gg.generations.rarecandy.pokeutils.material.VariantDetails;
import gg.generations.rarecandy.pokeutils.material.VariantParent;
import gg.generations.rarecandy.pokeutils.util.Codecs;
import gg.generations.rarecandy.renderer.animation.Animation;
import gg.generations.rarecandy.renderer.model.material.Material;

import java.util.*;

/**
 * ModelConfig class responsible for storing model configurations,
 * including materials, variants, offsets, animation settings, and rendering options.
 */
public class ModelConfig {

    /**
     * Scale factor applied to the model. Default is 1.0f.
     */
    public float scale = 1.0f;

    /**
     * Map of material names to their references.
     */
    public Map<String, MaterialReference> materials;

    /**
     * Default variant details mapped by variant names.
     */
    public Map<String, VariantDetails> defaultVariant;

    /**
     * Map of variant names to their parent definitions.
     */
    public Map<String, VariantParent> variants;

    /**
     * Rules to hide meshes during certain animations.
     */
    public Map<String, HideDuringAnimation> hideDuringAnimation = Collections.emptyMap();

    /**
     * Overrides for frames per second for specific animations.
     */
    public Map<String, Integer> animationFpsOverride;

    /**
     * Offsets applied to skeletal transforms by bone name.
     */
    public Map<String, SkeletalTransform> offsets = new HashMap<>();

    /**
     * Map associating materials with other materials that use the same animation tracks.
     */
    public Map<String, List<String>> materialsWithSameMaterialAnimation;

    /**
     * List of animation names that ignore scale transformations.
     */
    public List<String> ignoreScaleInAnimation;

    /**
     * Additional mesh-specific rendering options.
     */
    public Map<String, MeshOptions> modelOptions;

    /**
     * List of mesh names prioritized for rendering.
     */
    public List<String> meshesToRenderFirst;

    /**
     * Aliases for material and mesh names.
     */
    public Map<String, List<String>> aliases;

    /**
     * Flag indicating if mesh names should be excluded from the skeleton data.
     */
    public boolean excludeMeshNamesFromSkeleton = false;

    /**
     * Retrieves all materials associated with the given animation track name.
     *
     * @param trackName The name of the animation track.
     * @return List of material names associated with the track.
     */
    public List<String> getMaterialsForAnimation(String trackName) {
        var list = new ArrayList<String>();
        list.add(trackName);

        if (materialsWithSameMaterialAnimation != null) {
            var additional = materialsWithSameMaterialAnimation.get(trackName);
            if (additional != null) {
                list.addAll(additional);
            }
        }

        return list;
    }

    /**
     * Processes and prepares materials based on provided image references.
     *
     * @param images A map of image names to their file paths.
     * @return A map of material names to their prepared Material instances.
     */
    public Map<String, Material> prepMaterials(Map<String, String> images) {
        var map = new HashMap<String, Material>();

        for (var entry : materials.entrySet()) {
            String name = entry.getKey();
            Material material = MaterialReference.process(name, materials, images);
            map.put(name, material);

        }

        return map;
    }

    /**
     * Record representing rules for hiding meshes during animations.
     */
    public record HideDuringAnimation(boolean blackList, List<String> animations) {

        /**
         * Default instance indicating no hiding rules.
         */
        public static final HideDuringAnimation NONE = new HideDuringAnimation();
        public static final Codec<HideDuringAnimation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("blackList", false).forGetter(HideDuringAnimation::blackList),
                Codec.STRING.listOf().optionalFieldOf("animations", List.of()).forGetter(HideDuringAnimation::animations)
        ).apply(instance, HideDuringAnimation::new));

        /**
         * Default constructor creating an instance with no rules.
         */
        public HideDuringAnimation() {
            this(false, null);
        }

        /**
         * Checks whether a mesh should be hidden during the given animation.
         *
         * @param animation Animation object to check against.
         * @return True if the mesh should be hidden, false otherwise.
         */
        public boolean check(Animation animation) {
            return check(animation != null ? animation.name : null);
        }

        /**
         * Checks whether a mesh should be hidden based on the animation name.
         *
         * @param animation Name of the animation to check.
         * @return True if the mesh should be hidden, false otherwise.
         */
        public boolean check(String animation) {
            if (animations != null) {
                return animations.contains(animation) == blackList;
            }
            return false;
        }
    }

    /**
     * Codec for ModelConfig class
     */
    public static final Codec<ModelConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("scale", 1.0f).forGetter(c -> c.scale),
            Codecs.processing(Codecs.map(Codec.STRING, MaterialReference.CODEC), a -> a, ModelConfigCodecs::editEffects).fieldOf("materials").forGetter(c -> c.materials),
            Codecs.map(Codec.STRING, VariantDetails.CODEC).optionalFieldOf("defaultVariant",Map.of()).forGetter(c -> c.defaultVariant),
            Codecs.map(Codec.STRING, VariantParent.CODEC).optionalFieldOf("variants", Map.of()).forGetter(c -> c.variants),
            Codecs.map(Codec.STRING, HideDuringAnimation.CODEC).optionalFieldOf("hideDuringAnimation", Map.of()).forGetter(c -> c.hideDuringAnimation),
            Codecs.map(Codec.STRING, Codec.INT).optionalFieldOf("animationFpsOverride", Map.of()).forGetter(c -> c.animationFpsOverride),
            Codecs.map(Codec.STRING, SkeletalTransform.CODEC).optionalFieldOf("offsets", Map.of()).forGetter(c -> c.offsets),
            Codecs.map(Codec.STRING, Codec.STRING.listOf()).optionalFieldOf("materialsWithSameMaterialAnimation", Map.of()).forGetter(c -> c.materialsWithSameMaterialAnimation),
            Codec.STRING.listOf().optionalFieldOf("ignoreScaleInAnimation", List.of()).forGetter(c -> c.ignoreScaleInAnimation),
            Codecs.map(Codec.STRING, MeshOptions.CODEC).optionalFieldOf("modelOptions", Map.of()).forGetter(c -> c.modelOptions),
            Codec.STRING.listOf().optionalFieldOf("meshesToRenderFirst", List.of()).forGetter(c -> c.meshesToRenderFirst),
            Codecs.map(Codec.STRING, Codec.STRING.listOf()).optionalFieldOf("aliases", Map.of()).forGetter(c -> c.aliases),
            Codec.BOOL.optionalFieldOf("excludeMeshNamesFromSkeleton", false).forGetter(c -> c.excludeMeshNamesFromSkeleton)
    ).apply(instance, (scale, materials, defaultVariant, variants, hideDuringAnimation, animationFpsOverride, offsets, materialsWithSameMaterialAnimation, ignoreScaleInAnimation, modelOptions, meshesToRenderFirst, aliases, excludeMeshNamesFromSkeleton) -> {
        var config = new ModelConfig();
        config.scale = scale;
        config.materials = materials;
        config.defaultVariant = defaultVariant;
        config.variants = variants;
        config.hideDuringAnimation = hideDuringAnimation;
        config.animationFpsOverride = animationFpsOverride;
        config.offsets = offsets;
        config.materialsWithSameMaterialAnimation = materialsWithSameMaterialAnimation;
        config.ignoreScaleInAnimation = ignoreScaleInAnimation;
        config.modelOptions = modelOptions;
        config.meshesToRenderFirst = meshesToRenderFirst;
        config.aliases = aliases;
        config.excludeMeshNamesFromSkeleton = excludeMeshNamesFromSkeleton;
        return config;
    }));

}