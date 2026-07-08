package gg.generations.rarecandy.pokeutils;

import gg.generations.rarecandy.renderer.animation.ITransform;
import gg.generations.rarecandy.renderer.animation.ITransformSet;
import gg.generations.rarecandy.renderer.model.material.IMaterialImages;
import gg.generations.rarecandy.renderer.model.material.IMaterialValues;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class ImModelConfig implements IModelConfig {
    public static final Factory FACTORY = new Factory() {
        @Override
        public ITransform createTransform(Vector2f scale, Vector2f offset) {
            return new ImTransform(scale, offset);
        }

        @Override
        public ITransformSet createTransformSet(ITransform diffuse, ITransform layer, ITransform mask, ITransform emission) {
            return new ImTransformSet(diffuse, layer, mask, emission);
        }

        @Override
        public IVariantDetails createVariantDetails(String material, String effect, Boolean paradox, Boolean hide, ITransformSet offset) {
            return new ImVariantDetails(material, effect, paradox, hide, offset);
        }

        @Override
        public IVariantParent createVariantParent(String inherits, Map<String, IVariantDetails> details) {
            return new ImVariantParent(inherits, details);
        }

        @Override
        public ISkeletalTransform createSkeletalTransform(Vector3f position, Quaternionf rotation) {
            return new ImSkeletalTransform(position, rotation);
        }

        @Override
        public IHideDuringAnimation createHideDuringAnimation(boolean blackList, List<String> animations) {
            return new ImHideDuringAnimation(blackList, animations);
        }

        @Override
        public IMeshOptions createMeshOptions(boolean invert, List<String> aliases) {
            return new ImMeshOptions(invert, aliases);
        }

        @Override
        public IMaterialImages createMaterialImages(String diffuse, String layer, String mask, String emission) {
            return new ImMaterialImages(diffuse, layer, mask, emission);
        }

        @Override
        public IMaterialValues createMaterialValues(Vector3f baseColor1, Vector3f baseColor2, Vector3f baseColor3, Vector3f baseColor4, Vector3f baseColor5, Vector3f emiColor1, Vector3f emiColor2, Vector3f emiColor3, Vector3f emiColor4, Vector3f emiColor5, float emiIntensity1, float emiIntensity2, float emiIntensity3, float emiIntensity4, float emiIntensity5, boolean useLight, boolean disableDepth) {
            return new ImMaterialValues(
                    baseColor1, baseColor2, baseColor3, baseColor4, baseColor5,
                    emiColor1, emiColor2, emiColor3, emiColor4, emiColor5,
                    emiIntensity1, emiIntensity2, emiIntensity3, emiIntensity4, emiIntensity5,
                    useLight, disableDepth
            );
        }

        @Override
        public IMaterialReference createMaterialReference(String parent, String shader, CullType cull, BlendType blend, IMaterialImages materialImages, IMaterialValues materialValues) {
            return new ImMaterialReference(parent, shader, cull, blend, materialImages, materialValues);
        }

        @Override
        public IModelConfig create(float scale, Map<String, IMaterialReference> materials, Map<String, IVariantDetails> defaultVariant, Map<String, IVariantParent> variants, Map<String, IHideDuringAnimation> hideDuringAnimation, Map<String, Integer> animationFpsOverride, Map<String, Boolean> animationLoopsOverride, Map<String, ISkeletalTransform> offsets, Map<String, List<String>> materialsWithSameMaterialAnimation, List<String> ignoreScaleInAnimation, Map<String, IMeshOptions> modelOptions, List<String> meshesToRenderFirst, Map<String, List<String>> aliases, boolean excludeMeshNamesFromSkeleton, Integer resolution) {
            return new ImModelConfig(
                    scale, materials, defaultVariant, variants, hideDuringAnimation,
                    animationFpsOverride, animationLoopsOverride, offsets,
                    materialsWithSameMaterialAnimation, ignoreScaleInAnimation,
                    modelOptions, meshesToRenderFirst, aliases,
                    excludeMeshNamesFromSkeleton, resolution
            );
        }
    };

    private final ImFloat scale;
    private final Map<String, IMaterialReference> materials;
    private final Map<String, IVariantDetails> defaultVariant;
    private final Map<String, IVariantParent> variants;
    private final Map<String, IHideDuringAnimation> hideDuringAnimation;
    private final Map<String, Integer> animationFpsOverride;
    private final Map<String, Boolean> animationLoopsOverride;
    private final Map<String, ISkeletalTransform> offsets;
    private final Map<String, List<String>> materialsWithSameMaterialAnimation;
    private final ImStringList ignoreScaleInAnimation;
    private final Map<String, IMeshOptions> modelOptions;
    private final ImStringList meshesToRenderFirst;
    private final Map<String, List<String>> aliases;
    private final ImBoolean excludeMeshNamesFromSkeleton;
    private final ImBoolean resolutionEnabled;
    private final ImInt resolution;
    private final ImConfigEditor editor = new ImConfigEditor();

    public ImModelConfig(float scale,
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
                         Integer resolution) {
        this.scale = new ImFloat(scale);
        this.materials = ImGuiConfigUtil.mutableMap(materials);
        this.defaultVariant = ImGuiConfigUtil.mutableMap(defaultVariant);
        this.variants = ImGuiConfigUtil.mutableMap(variants);
        this.hideDuringAnimation = ImGuiConfigUtil.mutableMap(hideDuringAnimation);
        this.animationFpsOverride = ImGuiConfigUtil.mutableMap(animationFpsOverride);
        this.animationLoopsOverride = ImGuiConfigUtil.mutableMap(animationLoopsOverride);
        this.offsets = ImGuiConfigUtil.mutableMap(offsets);
        this.materialsWithSameMaterialAnimation = ImGuiConfigUtil.mutableStringListMap(materialsWithSameMaterialAnimation);
        this.ignoreScaleInAnimation = new ImStringList(ignoreScaleInAnimation);
        this.modelOptions = ImGuiConfigUtil.mutableMap(modelOptions);
        this.meshesToRenderFirst = new ImStringList(meshesToRenderFirst);
        this.aliases = ImGuiConfigUtil.mutableStringListMap(aliases);
        this.excludeMeshNamesFromSkeleton = new ImBoolean(excludeMeshNamesFromSkeleton);
        this.resolutionEnabled = new ImBoolean(resolution != null);
        this.resolution = new ImInt(resolution != null ? resolution : 0);
    }

    public int render() {
        var dirty = 0;
        ImGui.begin("Config");

        if (ImGui.inputFloat("Scale", scale)) {
            dirty |= 1;
        }
//        dirty |= ImGui.checkbox("Exclude mesh names from skeleton", excludeMeshNamesFromSkeleton);
//        dirty |= ImGui.checkbox("Resolution enabled", resolutionEnabled);
//        if (resolutionEnabled.get()) dirty |= ImGui.inputInt("Resolution", resolution);

        ImGui.separator();

        if (editor.renderMap("Materials", "materials", materials, ImMaterialReference::empty, ImMaterialReference.class)) {
            dirty |= 2;
        }
        if (editor.renderMap("Default Variant", "defaultVariant", defaultVariant, ImVariantDetails::empty, value -> ((ImVariantDetails) value).render(materials.keySet()), false)) {
            dirty |= 4;
        }

        if (editor.renderMap("Variants", "variants", variants, ImVariantParent::empty, value -> ((ImVariantParent) value).render(materials.keySet()))) {
            dirty |= 4;
        }

        if(editor.renderMap("Hide During Animation", "hideDuringAnimation", hideDuringAnimation, ImHideDuringAnimation::empty, ImHideDuringAnimation.class, false)) {
            dirty |= 8;
        }
//        dirty |= editor.renderIntegerMap("Animation FPS Override", "animationFpsOverride", animationFpsOverride);
//        dirty |= editor.renderBooleanMap("Animation Loops Override", "animationLoopsOverride", animationLoopsOverride);
//        dirty |= editor.renderMap("Offsets", "offsets", offsets, ImSkeletalTransform::empty, ImSkeletalTransform.class);
//        dirty |= editor.renderStringListMap("Materials With Same Material Animation", "materialsWithSameMaterialAnimation", materialsWithSameMaterialAnimation);
//        dirty |= editor.renderStringList("Ignore Scale In Animation", "ignoreScaleInAnimation", ignoreScaleInAnimation);
//        dirty |= editor.renderMap("Model Options", "modelOptions", modelOptions, ImMeshOptions::empty, ImMeshOptions.class, false);
//        dirty |= editor.renderStringList("Meshes To Render First", "meshesToRenderFirst", meshesToRenderFirst);
//        dirty |= editor.renderStringListMap("Aliases", "aliases", aliases, false);

        ImGui.end();
        return dirty;
    }

    @Override public float scale() { return scale.get(); }
    @Override public Map<String, IMaterialReference> materials() { return materials; }
    @Override public Map<String, IVariantDetails> defaultVariant() { return defaultVariant; }
    @Override public Map<String, IVariantParent> variants() { return variants; }
    @Override public Map<String, IHideDuringAnimation> hideDuringAnimation() { return hideDuringAnimation; }
    @Override public Map<String, Integer> animationFpsOverride() { return animationFpsOverride; }
    @Override public Map<String, Boolean> animationLoopsOverride() { return animationLoopsOverride; }
    @Override public Map<String, ISkeletalTransform> offsets() { return offsets; }
    @Override public Map<String, List<String>> materialsWithSameMaterialAnimation() { return materialsWithSameMaterialAnimation; }
    @Override public List<String> ignoreScaleInAnimation() { return ignoreScaleInAnimation; }
    @Override public Map<String, IMeshOptions> modelOptions() { return modelOptions; }
    @Override public List<String> meshesToRenderFirst() { return meshesToRenderFirst; }
    @Override public Map<String, List<String>> aliases() { return aliases; }
    @Override public boolean excludeMeshNamesFromSkeleton() { return excludeMeshNamesFromSkeleton.get(); }
    @Override public Integer resolution() { return resolutionEnabled.get() ? resolution.get() : null; }
}
