package gg.generations.rarecandy.pokeutils;

import gg.generations.rarecandy.renderer.animation.ITransform;
import gg.generations.rarecandy.renderer.animation.ITransformSet;
import gg.generations.rarecandy.renderer.animation.TransformSet;
import gg.generations.rarecandy.renderer.model.material.IMaterialImages;
import gg.generations.rarecandy.renderer.model.material.IMaterialValues;
import gg.generations.rarecandy.renderer.model.material.MaterialImages;
import gg.generations.rarecandy.renderer.model.material.MaterialValues;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.*;

public record ModelConfig(
        float scale,
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
) implements IModelConfig {

    public static final Factory FACTORY = new Factory() {
        @Override
        public ITransform createTransform(Vector2f scale, Vector2f offset) {
            return new Transform(scale, offset);
        }

        @Override
        public ITransformSet createTransformSet(ITransform diffuse, ITransform layer, ITransform mask, ITransform emission) {
            return new TransformSet(diffuse, layer, mask, emission);
        }

        @Override
        public IVariantDetails createVariantDetails(String material, String effect, Boolean paradox, Boolean hide, ITransformSet offset) {
            return new VariantDetails(material, effect, paradox, hide, offset);
        }

        @Override
        public IVariantParent createVariantParent(String inherits, Map<String, IVariantDetails> details) {
            return new VariantParent(inherits, details);
        }

        @Override
        public ISkeletalTransform createSkeletalTransform(Vector3f position, Quaternionf rotation) {
            return new SkeletalTransform(position, rotation);
        }

        @Override
        public IHideDuringAnimation createHideDuringAnimation(boolean blackList, List<String> animations) {
            return new HideDuringAnimation(blackList, animations);
        }

        @Override
        public IMeshOptions createMeshOptions(boolean invert, List<String> aliases) {
            return new MeshOptions(invert, aliases);
        }

        @Override
        public IMaterialImages createMaterialImages(String diffuse, String layer, String mask, String emission) {
            return new MaterialImages(diffuse, layer, mask, emission);
        }

        @Override
        public IMaterialValues createMaterialValues(Vector3f baseColor1, Vector3f baseColor2, Vector3f baseColor3, Vector3f baseColor4, Vector3f baseColor5, Vector3f emiColor1, Vector3f emiColor2, Vector3f emiColor3, Vector3f emiColor4, Vector3f emiColor5, float emiIntensity1, float emiIntensity2, float emiIntensity3, float emiIntensity4, float emiIntensity5, boolean useLight, boolean disableDepth) {
            return new MaterialValues(baseColor1, baseColor2, baseColor3, baseColor4, baseColor5, emiColor1, emiColor2, emiColor3, emiColor4, emiColor5, emiIntensity1, emiIntensity2, emiIntensity3, emiIntensity4, emiIntensity5, useLight, disableDepth);
        }
        @Override
        public IMaterialReference createMaterialReference(String parent, String shader, CullType cull, BlendType blend, IMaterialImages materialImages, IMaterialValues materialValues) {
            return new MaterialReference(parent, shader, cull, blend, materialImages, materialValues);
        }

        public IModelConfig create(
                float scale, Map<String, IMaterialReference> materials,
                Map<String, IVariantDetails> defaultVariant, Map<String, IVariantParent> variants,
                Map<String, IHideDuringAnimation> hideDuringAnimation, Map<String, Integer> animationFpsOverride,
                Map<String, Boolean> animationLoopsOverride, Map<String, ISkeletalTransform> offsets,
                Map<String, List<String>> materialsWithSameMaterialAnimation, List<String> ignoreScaleInAnimation,
                Map<String, IMeshOptions> modelOptions, List<String> meshesToRenderFirst,
                Map<String, List<String>> aliases, boolean excludeMeshNamesFromSkeleton,
                Integer resolution) {
            return new ModelConfig(
                    scale, materials,
                    defaultVariant, variants,
                    hideDuringAnimation, animationFpsOverride,
                    animationLoopsOverride,  offsets,
                    materialsWithSameMaterialAnimation, ignoreScaleInAnimation,
                    modelOptions, meshesToRenderFirst,
                    aliases, excludeMeshNamesFromSkeleton,
                    resolution
            );
        }
    };

    public record HideDuringAnimation(boolean blackList, List<String> animations) implements IHideDuringAnimation { }

    public record Transform(Vector2f scale, Vector2f offset) implements ITransform {
        public Transform(Vector2f offset) {
            this(new Vector2f(1f, 1f), offset);
        }
    }
}
