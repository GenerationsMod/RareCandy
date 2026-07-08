package gg.generations.rarecandy.renderer.loading;

import gg.generations.rarecandy.pokeutils.*;
import gg.generations.rarecandy.pokeutils.ModelNode;
import gg.generations.rarecandy.pokeutils.resource.ResourceReader;
import gg.generations.rarecandy.pokeutils.util.ExceptionThrowingBiFunction;
import gg.generations.rarecandy.pokeutils.util.ExceptionThrowingTriConsumer;
import gg.generations.rarecandy.pokeutils.util.ExceptionThrowingTriFunction;
import gg.generations.rarecandy.renderer.animation.Animation;
import gg.generations.rarecandy.renderer.animation.ITransformSet;
import gg.generations.rarecandy.renderer.animation.Skeleton;
import gg.generations.rarecandy.renderer.components.InstanceDetails;
import gg.generations.rarecandy.renderer.components.MultiRenderObject;
import gg.generations.rarecandy.renderer.model.Variant;
import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.rendering.RenderStage;
import gg.generations.rarecandy.renderer.storage.DrawBuffer;
import gg.generations.rarecandy.renderer.storage.SSBOBuffer;
import gg.generations.rarecandy.renderer.textures.Texture;
import gg.generations.rarecandy.renderer.textures.TextureArray;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.assimp.AIBone;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.Assimp;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;
import static org.lwjgl.opengl.GL30C.glGetInteger;
import static org.lwjgl.opengl.GL43C.*;

public final class ModelObjectCompiler {
    private static final int MATERIAL_SIZE = 208;
    private static final int VERTEX_BYTES = 96;
    private static final int DRAW_INFO_ENTRY_BYTES = Float.BYTES * 4 + Integer.BYTES * 4;

    private static final int[] NORMAL_FACE = {0, 1, 2};
    private static final int[] INVERT_FACE = {2, 1, 0};

    private static final Vector3f TANGENT_NORMAL = new Vector3f();
    private static final Vector3f TANGENT_VALUE = new Vector3f();
    private static final Vector3f TANGENT_BITANGENT = new Vector3f();
    private static final Vector3f TANGENT_TEMP = new Vector3f();

    private ModelObjectCompiler() {}

    public static <T extends MultiRenderObject> void buildObject(
            Function<Names, T> objectBuilder,
            Supplier<IModelConfig> configSupplier,
            Supplier<ResourceReader> assetSupplier,
            ExceptionThrowingTriFunction<ResourceReader, List<String>, Integer, TextureArray> imageFactory,
            ExceptionThrowingBiFunction<IMaterialReference, List<String>, Material> materialFactory,
            ExceptionThrowingTriConsumer<MultiRenderObject, Skeleton, Map<String, AnimResource>> onFinish) throws Exception {
        requireNonNull(objectBuilder, "objectBuilder");
        requireNonNull(configSupplier, "configSupplier");
        requireNonNull(assetSupplier, "assetSupplier");
        requireNonNull(imageFactory, "imageFactory");
        requireNonNull(materialFactory, "materialFactory");

        var assets = requireNonNull(
                assetSupplier.get(),
                "assetSupplier returned null"
        );

        var config = requireNonNull(
                configSupplier.get(),
                "configSupplier returned null"
        );

        var names = Names.generateNames(config);

        var object = requireNonNull(
                objectBuilder.apply(names),
                "objectBuilder returned null"
        );

        rebuildImages(
                object,
                assets,
                config,
                imageFactory,
                ignored -> {}
        );

        rebuildMaterials(
                object,
                config,
                materialFactory
        );

        rebuildVariants(
                object,
                config
        );

        updateScale(
                object,
                config
        );

        var animationResources = readAnimationResources(assets);

        var skeleton = rebuildGeometry(
                object,
                assets,
                config,
                null,
                true
        );

        rebuildAnimations(
                object,
                skeleton,
                animationResources,
                config
        );

        rebuildAnimationVisibility(
                object,
                config
        );

        initializeRuntimeBuffers(object);

        if (onFinish != null) {
            onFinish.accept(object, skeleton, animationResources);
        }
    }

    /**
     * Tests whether the target object's existing name-to-ID layout can represent the
     * supplied configuration.
     *
     * <p>A {@code false} result means at least one mesh, material, variant, or image name
     * was added, removed, or reordered. Since {@link MultiRenderObject#names} is final and
     * its arrays are constructed from that layout, the safe response is to create a new
     * object rather than mutate the existing one.</p>
     */
    public static boolean isLayoutCompatible(MultiRenderObject object, IModelConfig config) {
        requireNonNull(object, "object");
        requireNonNull(config, "config");
        return object.names.equals(Names.generateNames(config));
    }

    /**
     * Loads all supported animation resource formats into a caller-owned map.
     *
     * <p>The returned map can be retained and passed to animation-only updates without
     * reopening or reimporting the GLB.</p>
     */
    public static Map<String, AnimResource> readAnimationResources(ResourceReader assets)
            throws IOException {
        requireNonNull(assets, "assets");

        var resources = new LinkedHashMap<String, AnimResource>();
        SmdResource.read(assets, resources);
        GfbanmResource.read(assets, resources);
        TrAnimationResource.read(assets, resources);
        return resources;
    }

    /**
     * Rebuilds the complete texture array and installs it after loading succeeds.
     *
     * @param imageFactory creates the replacement texture array from the resource reader,
     *                     image-name layout, and configured resolution
     * @param disposer receives the old texture array after the replacement is installed;
     *                 pass a no-op consumer when texture lifetime is managed elsewhere
     */
    public static void rebuildImages(
            MultiRenderObject object,
            ResourceReader assets,
            IModelConfig config,
            ExceptionThrowingTriFunction<ResourceReader, List<String>, Integer, TextureArray> imageFactory,
            Consumer<TextureArray> disposer) throws Exception {
        requireCompatibleLayout(object, config);
        requireNonNull(assets, "assets");
        requireNonNull(imageFactory, "imageFactory");
        requireNonNull(disposer, "disposer");

        var replacement = imageFactory.apply(
                assets,
                object.names.images(),
                config.resolution() != null ? config.resolution() : 1024);

        var previous = object.images;
        object.images = replacement;

        if (previous != null && previous != replacement) {
            disposer.accept(previous);
        }
    }

    /**
     * Rebuilds every material and the complete material SSBO.
     *
     * <p>Material inheritance is resolved fresh from the supplied configuration. No
     * completed-material cache is retained, so editing a parent material cannot reuse a
     * stale inherited value.</p>
     *
     * <p>This method does not rebuild variants. Call {@link #refreshRenderStages} after
     * this method only when the edit can change {@link RenderStage#from(Material)}.</p>
     */
    public static void rebuildMaterials(
            MultiRenderObject object,
            IModelConfig config,
            ExceptionThrowingBiFunction<IMaterialReference, List<String>, Material> materialFactory)
    {
        requireCompatibleLayout(object, config);
        requireNonNull(materialFactory, "materialFactory");

        var replacementMaterials = new Material[object.names.materials().size()];
        var created = new ArrayList<Material>(replacementMaterials.length);
        SSBOBuffer replacementBuffer = null;

        try {
            for (int materialId = 0; materialId < replacementMaterials.length; materialId++) {
                var materialName = object.names.materials().get(materialId);
                if (config.materials() == null || !config.materials().containsKey(materialName)) {
                    throw new IllegalArgumentException("Missing material: " + materialName);
                }

                var reference = IMaterialReference.complete(materialName, config.materials());
                var material = materialFactory.apply(reference, object.names.images());
                replacementMaterials[materialId] = material;
                created.add(material);
            }

            replacementBuffer = new SSBOBuffer(
                    Math.max(1, Math.multiplyExact(replacementMaterials.length, MATERIAL_SIZE)));
            for (var material : replacementMaterials) {
                material.put(replacementBuffer);
            }
            replacementBuffer.upload();
        } catch (Exception | Error failure) {
            if (replacementBuffer != null) {
                replacementBuffer.delete();
            }
            created.forEach(ModelObjectCompiler::closeQuietly);
        }

        var previousMaterials = object.materials;
        var previousBuffer = object.material;

        object.materials = replacementMaterials;
        object.material = replacementBuffer;

        if (previousMaterials != null) {
            for (var material : previousMaterials) {
                if (!containsIdentity(replacementMaterials, material)) {
                    closeQuietly(material);
                }
            }
        }
        deleteIfReplaced(previousBuffer, replacementBuffer);
    }

    /**
     * Re-resolves all variants, rebuilds their relationship matrices, recreates the
     * required draw buffers, and uploads a replacement variant SSBO.
     *
     * <p>The entire variant section is rebuilt because variant values are deduplicated and
     * relationship matrices store indices into that packed array. Changing one variant can
     * therefore change more than one compiled variant ID.</p>
     */
    public static void rebuildVariants(MultiRenderObject object, IModelConfig config) {
        requireCompatibleLayout(object, config);

        var replacement = compileVariants(object.names, object.materials, config);
        var previousVariantBuffer = object.variant;
        var previousDrawBuffers = object.drawBuffer;

        object.variants = replacement.variants();
        object.variantRelationships = replacement.relationships();
        object.stageRelationships = replacement.stages();
        object.drawBuffer = replacement.drawBuffers();
        object.variant = replacement.buffer();

        deleteIfReplaced(previousVariantBuffer, replacement.buffer());
        if (previousDrawBuffers != null && previousDrawBuffers != replacement.drawBuffers()) {
            previousDrawBuffers.values().forEach(SSBOBuffer::delete);
        }
    }

    /**
     * Recalculates render-stage relationships and recreates only the draw buffers.
     *
     * <p>Use this after a material edit that changes stage selection. Existing variants,
     * variant IDs, variant relationships, and the variant SSBO remain untouched.</p>
     */
    public static void refreshRenderStages(MultiRenderObject object) {
        requireNonNull(object, "object");

        var stages = new RenderStage[object.variantRelationships.length][];
        var drawBuffers = new EnumMap<RenderStage, DrawBuffer>(RenderStage.class);

        for (int meshId = 0; meshId < object.variantRelationships.length; meshId++) {
            var relationshipRow = object.variantRelationships[meshId];
            stages[meshId] = new RenderStage[relationshipRow.length];

            for (int variantSlot = 0; variantSlot < relationshipRow.length; variantSlot++) {
                var compiledVariantId = relationshipRow[variantSlot];
                if (compiledVariantId < 0 || compiledVariantId >= object.variants.length) {
                    throw new IllegalStateException(
                            "Invalid compiled variant ID " + compiledVariantId
                                    + " at mesh " + meshId + ", slot " + variantSlot);
                }

                var variant = object.variants[compiledVariantId];
                if (variant.material() < 0 || variant.material() >= object.materials.length) {
                    throw new IllegalStateException(
                            "Variant " + compiledVariantId + " references material "
                                    + variant.material());
                }

                var stage = RenderStage.from(object.materials[variant.material()]);
                stages[meshId][variantSlot] = stage;
                drawBuffers.computeIfAbsent(
                        stage,
                        ignored -> new DrawBuffer(
                                Integer.BYTES * 4 * Math.max(1, object.meshes.length)));
            }
        }

        var previous = object.drawBuffer;
        object.stageRelationships = stages;
        object.drawBuffer = drawBuffers;

        if (previous != null && previous != drawBuffers) {
            previous.values().forEach(SSBOBuffer::delete);
        }
    }

    /**
     * Rebuilds every animation while reusing an externally owned skeleton and animation
     * resource map.
     *
     * <p>This method does not rebuild animation visibility. Visibility only depends on
     * animation names and hide rules, so ordinary FPS, loop, offset, or scale-handling
     * edits do not require a visibility rebuild.</p>
     */
    public static void rebuildAnimations(
            MultiRenderObject object,
            Skeleton skeleton,
            Map<String, AnimResource> resources,
            IModelConfig config) {
        requireCompatibleLayout(object, config);
        requireNonNull(skeleton, "skeleton");
        requireNonNull(resources, "resources");

        var animations = new Animation[resources.size()];
        var animationNames = new String[resources.size()];
        var animationNameToId = new LinkedHashMap<String, Integer>();

        int animationId = 0;
        for (var entry : resources.entrySet()) {
            animations[animationId] = compileAnimation(
                    animationId,
                    entry.getKey(),
                    entry.getValue(),
                    object.names,
                    skeleton,
                    config);
            animationNames[animationId] = entry.getKey();
            animationNameToId.put(entry.getKey(), animationId);
            animationId++;
        }

        object.animations = animations;
        object.animationNames = animationNames;
        object.animationNameToId = Map.copyOf(animationNameToId);
    }

    /**
     * Rebuilds one animation in place without recreating the remaining animation objects.
     *
     * <p>This operation requires the animation name to already exist in the target object's
     * current animation layout. Adding, removing, or reordering animation resources requires
     * {@link #rebuildAnimations}.</p>
     */
    public static void rebuildAnimation(
            MultiRenderObject object,
            Skeleton skeleton,
            Map<String, AnimResource> resources,
            IModelConfig config,
            String animationName) {
        requireCompatibleLayout(object, config);
        requireNonNull(skeleton, "skeleton");
        requireNonNull(resources, "resources");
        requireNonNull(animationName, "animationName");

        var animationId = object.animationNameToId.get(animationName);
        if (animationId == null) {
            throw new IllegalArgumentException("Unknown animation: " + animationName);
        }

        var resource = resources.get(animationName);
        if (resource == null) {
            throw new IllegalArgumentException("Missing animation resource: " + animationName);
        }

        var replacements = object.animations.clone();
        replacements[animationId] = compileAnimation(
                animationId,
                animationName,
                resource,
                object.names,
                skeleton,
                config);
        object.animations = replacements;
    }

    /**
     * Rebuilds the complete mesh-by-animation visibility matrix.
     *
     * <p>Use this when animation names/counts change or when several mesh hide rules are
     * edited together.</p>
     */
    public static void rebuildAnimationVisibility(
            MultiRenderObject object,
            IModelConfig config) {
        requireCompatibleLayout(object, config);

        var hidden = new boolean[object.names.meshes().size()][object.animationNames.length];
        for (int meshId = 0; meshId < hidden.length; meshId++) {
            hidden[meshId] = compileAnimationVisibilityRow(
                    object.names.meshes().get(meshId),
                    object.animationNames,
                    config);
        }
        object.hideDuringAnimation = hidden;
    }

    /**
     * Rebuilds one mesh row of the animation visibility matrix.
     *
     * <p>This is the smallest update for editing one {@code hideDuringAnimation} entry.</p>
     */
    public static void rebuildAnimationVisibilityRow(
            MultiRenderObject object,
            IModelConfig config,
            String meshName) {
        requireCompatibleLayout(object, config);
        requireNonNull(meshName, "meshName");

        var meshId = object.meshNameToId.get(meshName);
        if (meshId == null) {
            throw new IllegalArgumentException("Unknown mesh: " + meshName);
        }

        var replacement = object.hideDuringAnimation != null
                && object.hideDuringAnimation.length == object.names.meshes().size()
                ? object.hideDuringAnimation.clone()
                : new boolean[object.names.meshes().size()][];

        replacement[meshId] = compileAnimationVisibilityRow(
                meshName,
                object.animationNames,
                config);
        object.hideDuringAnimation = replacement;
    }

    /**
     * Reimports {@code model.glb}, rebuilds the packed vertex/index/mesh-offset buffer, and
     * installs it on the target object.
     *
     * @param existingSkeleton skeleton retained by the caller from an earlier geometry
     *                         build; it may be reused when only mesh packing options changed
     * @param rebuildSkeleton when {@code true}, creates and returns a new skeleton from the
     *                        imported model; when {@code false}, reuses
     *                        {@code existingSkeleton}
     * @return the skeleton used to compile bone IDs; retain it externally for animation-only
     *         updates
     */
    public static Skeleton rebuildGeometry(
            MultiRenderObject object,
            ResourceReader assets,
            IModelConfig config,
            Skeleton existingSkeleton,
            boolean rebuildSkeleton) throws IOException {
        requireCompatibleLayout(object, config);
        requireNonNull(assets, "assets");

        var replacement = compileGeometry(
                object.names,
                assets,
                config,
                existingSkeleton,
                rebuildSkeleton);
        var previousModelBuffer = object.modelBuffer;

        object.meshes = replacement.meshDrawCounts();
        object.vertex = replacement.vertex();
        object.index = replacement.index();
        object.meshOffsets = replacement.meshOffsets();
        object.modelBuffer = replacement.modelBuffer();
        object.maxVertex = replacement.maxVertex();
        object.dimensions.set(replacement.dimensions());
        object.setRootTransformation(new Matrix4f(replacement.rootTransformation()));

        if (previousModelBuffer != 0 && previousModelBuffer != replacement.modelBuffer()) {
            GL43C.glDeleteBuffers(previousModelBuffer);
        }

        return replacement.skeleton();
    }

    /**
     * Initializes the runtime instance and draw-info buffers when they have not already
     * been created. These buffers are runtime infrastructure rather than config sections,
     * so ordinary targeted config updates should not recreate them.
     */
    public static void initializeRuntimeBuffers(MultiRenderObject object) {
        requireNonNull(object, "object");

        if (object.instance == null) {
            object.instance = new SSBOBuffer(InstanceDetails.size);
        }
        if (object.drawInfo == null) {
            object.drawInfo = new SSBOBuffer(
                    Math.max(1, object.meshes.length) * DRAW_INFO_ENTRY_BYTES);
        }
    }

    /**
     * Copies the config scale onto the render object.
     *
     * <p>Animation skeletal offsets are compiled using the model scale. After changing
     * scale, rebuild the affected animations as well.</p>
     */
    public static void updateScale(MultiRenderObject object, IModelConfig config) {
        requireCompatibleLayout(object, config);
        object.scale = config.scale();
    }

    private static VariantBuild compileVariants(
            Names names,
            Material[] materials,
            IModelConfig config) {
        var aliases = config.aliases() != null
                ? config.aliases()
                : Collections.<String, List<String>>emptyMap();
        var slotCount = Math.max(1, names.variants().size());
        var relationships = new int[names.meshes().size()][slotCount];
        var stages = new RenderStage[names.meshes().size()][slotCount];
        var drawBuffers = new EnumMap<RenderStage, DrawBuffer>(RenderStage.class);
        var compiledVariants = new ArrayList<Variant>();
        var cache = new HashMap<String, Map<String, IVariantDetails>>();
        SSBOBuffer variantBuffer = null;

        try {
            for (int variantSlot = 0; variantSlot < slotCount; variantSlot++) {
                Map<String, IVariantDetails> resolved;
                if (names.variants().isEmpty()) {
                    resolved = expandAliases(config.defaultVariant(), aliases);
                } else {
                    resolved = resolveVariant(
                            names.variants().get(variantSlot),
                            config.defaultVariant(),
                            config.variants(),
                            aliases,
                            cache,
                            new HashSet<>());
                }

                for (int meshId = 0; meshId < names.meshes().size(); meshId++) {
                    var meshName = names.meshes().get(meshId);
                    var details = resolved.get(meshName);
                    if (details == null) {
                        throw new IllegalArgumentException(
                                "No variant details resolved for mesh '" + meshName
                                        + "' in slot " + variantSlot);
                    }

                    var variant = createVariant(details, names.materials());
                    var compiledVariantId = addOrGet(compiledVariants, variant);
                    if (variant.material() < 0 || variant.material() >= materials.length) {
                        throw new IllegalArgumentException(
                                "Variant for mesh '" + meshName
                                        + "' references an unknown material.");
                    }

                    var stage = RenderStage.from(materials[variant.material()]);
                    relationships[meshId][variantSlot] = compiledVariantId;
                    stages[meshId][variantSlot] = stage;
                    drawBuffers.computeIfAbsent(
                            stage,
                            ignored -> new DrawBuffer(
                                    Integer.BYTES * 4 * Math.max(1, names.meshes().size())));
                }
            }

            var variants = compiledVariants.toArray(Variant[]::new);
            variantBuffer = new SSBOBuffer(Math.max(1, variants.length * Variant.SIZE));
            for (var variant : variants) {
                variant.put(variantBuffer);
            }
            variantBuffer.upload();

            return new VariantBuild(
                    variants,
                    relationships,
                    stages,
                    drawBuffers,
                    variantBuffer);
        } catch (RuntimeException | Error failure) {
            if (variantBuffer != null) {
                variantBuffer.delete();
            }
            drawBuffers.values().forEach(SSBOBuffer::delete);
            throw failure;
        }
    }

    private static Map<String, IVariantDetails> resolveVariant(
            String key,
            Map<String, ? extends IVariantDetails> defaults,
            Map<String, ? extends IVariantParent> variants,
            Map<String, List<String>> aliases,
            Map<String, Map<String, IVariantDetails>> cache,
            Set<String> resolving) {
        var cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        if (!resolving.add(key)) {
            throw new IllegalArgumentException("Variant inheritance cycle involving: " + key);
        }

        var resolved = new LinkedHashMap<String, IVariantDetails>();
        mergeMissing(resolved, expandAliases(defaults, aliases));

        var parent = variants != null ? variants.get(key) : null;
        if (parent != null && parent.inherits() != null) {
            mergeOverride(resolved, resolveVariant(
                    parent.inherits(),
                    defaults,
                    variants,
                    aliases,
                    cache,
                    resolving));
        }
        if (parent != null) {
            mergeOverride(resolved, expandAliases(parent.details(), aliases));
        }

        resolving.remove(key);
        var immutable = Map.copyOf(resolved);
        cache.put(key, immutable);
        return immutable;
    }

    private static Map<String, IVariantDetails> expandAliases(
            Map<String, ? extends IVariantDetails> source,
            Map<String, List<String>> aliases) {
        var expanded = new LinkedHashMap<String, IVariantDetails>();
        if (source == null) {
            return expanded;
        }

        source.forEach((meshName, details) -> {
            if (details == null) {
                return;
            }

            var aliasNames = aliases.get(meshName);
            if (aliasNames == null || aliasNames.isEmpty()) {
                expanded.put(meshName, details);
            } else {
                aliasNames.forEach(alias -> expanded.put(alias, details));
            }
        });
        return expanded;
    }

    private static void mergeMissing(
            Map<String, IVariantDetails> target,
            Map<String, IVariantDetails> source) {
        source.forEach(target::putIfAbsent);
    }

    private static void mergeOverride(
            Map<String, IVariantDetails> target,
            Map<String, IVariantDetails> source) {
        source.forEach((mesh, details) -> target.merge(
                mesh,
                details,
                ModelObjectCompiler::overlayVariant));
    }

    private static IVariantDetails overlayVariant(
            IVariantDetails lowerPriority,
            IVariantDetails higherPriority) {
        ITransformSet transform;
        if (higherPriority.transform() == null) {
            transform = lowerPriority.transform();
        } else if (lowerPriority.transform() == null) {
            transform = higherPriority.transform();
        } else {
            transform = higherPriority.transform().fillIn(lowerPriority.transform());
        }

        return new ResolvedVariantDetails(
                higherPriority.material() != null
                        ? higherPriority.material()
                        : lowerPriority.material(),
                higherPriority.effect() != null
                        ? higherPriority.effect()
                        : lowerPriority.effect(),
                higherPriority.paradox() != null
                        ? higherPriority.paradox()
                        : lowerPriority.paradox(),
                higherPriority.hide() != null
                        ? higherPriority.hide()
                        : lowerPriority.hide(),
                transform);
    }

    private static Variant createVariant(
            IVariantDetails details,
            List<String> materialNames) {
        var effect = switch (details.effect() != null ? details.effect() : "") {
            case "galaxy" -> 1;
            case "pastel" -> 2;
            case "shadow" -> 3;
            case "sketch" -> 4;
            case "vintage" -> 5;
            default -> 0;
        };

        return new Variant(
                materialNames.indexOf(details.material()),
                effect,
                Boolean.TRUE.equals(details.paradox()),
                Boolean.TRUE.equals(details.hide()),
                (details.transform() != null ? details.transform() : ITransformSet.DEFAULT).array());
    }

    private static int addOrGet(List<Variant> variants, Variant variant) {
        var existing = variants.indexOf(variant);
        if (existing >= 0) {
            return existing;
        }
        variants.add(variant);
        return variants.size() - 1;
    }

    private static Animation compileAnimation(
            int animationId,
            String animationName,
            AnimResource resource,
            Names names,
            Skeleton skeleton,
            IModelConfig config) {
        var fps = resource.fps();
        if (config.animationFpsOverride() != null) {
            fps = config.animationFpsOverride().getOrDefault(animationName, (int) fps);
        }

        var loops = resource.loops();
        if (config.animationLoopsOverride() != null) {
            loops = config.animationLoopsOverride().getOrDefault(animationName, loops);
        }

        var resolvedOffsets = new HashMap<String, Animation.Offset>(resource.getOffsets());
        for (var entry : resource.getOffsets().entrySet()) {
            for (var materialName : config.getMaterialsForAnimation(entry.getKey())) {
                resolvedOffsets.put(materialName, entry.getValue());
            }
        }

        var offsetsByMaterial = new Animation.Offset[names.materials().size()];
        resolvedOffsets.forEach((materialName, offset) -> {
            var materialId = names.materials().indexOf(materialName);
            if (materialId >= 0) {
                offsetsByMaterial[materialId] = offset;
            }
        });

        var ignoreScaling = config.ignoreScaleInAnimation() != null
                && (config.ignoreScaleInAnimation().contains(animationName)
                || config.ignoreScaleInAnimation().contains("all"));

        var configuredTransform = config.offsets() != null
                ? config.offsets().getOrDefault(animationName, ISkeletalTransform.DEFAULT)
                : ISkeletalTransform.DEFAULT;

        return new Animation(
                animationId,
                (int) fps,
                loops,
                skeleton,
                resource.getNodes(),
                offsetsByMaterial,
                ignoreScaling,
                scaledTransformCopy(configuredTransform, config.scale()));
    }

    private static ISkeletalTransform scaledTransformCopy(
            ISkeletalTransform source,
            float modelScale) {
        var divisor = modelScale == 0.0f ? 1.0f : modelScale;
        var position = new Vector3f(source.position()).div(divisor);
        var rotation = new Quaternionf(source.rotation());

        return new ISkeletalTransform() {
            @Override
            public Vector3f position() {
                return position;
            }

            @Override
            public Quaternionf rotation() {
                return rotation;
            }
        };
    }

    private static boolean[] compileAnimationVisibilityRow(
            String meshName,
            String[] animationNames,
            IModelConfig config) {
        var rule = config.hideDuringAnimation() != null
                ? config.hideDuringAnimation().getOrDefault(meshName, IHideDuringAnimation.NONE)
                : IHideDuringAnimation.NONE;
        var row = new boolean[animationNames.length];

        for (int animationId = 0; animationId < animationNames.length; animationId++) {
            row[animationId] = rule.check(animationNames[animationId]);
        }
        return row;
    }

    private static GeometryBuild compileGeometry(
            Names names,
            ResourceReader assets,
            IModelConfig config,
            Skeleton existingSkeleton,
            boolean rebuildSkeleton) throws IOException {
        AIScene scene = ModelObjectCompiler.read(assets);

        try {
            var rootNode = ModelNode.create(scene.mRootNode());
            var importedMeshes = IntStream.range(0, scene.mNumMeshes())
                    .mapToObj(index -> AIMesh.create(scene.mMeshes().get(index)))
                    .toArray(AIMesh[]::new);
            var skeleton = rebuildSkeleton || existingSkeleton == null
                    ? new Skeleton(rootNode, importedMeshes, config.excludeMeshNamesFromSkeleton())
                    : existingSkeleton;

            var meshNameToId = new HashMap<String, Integer>();
            for (int meshId = 0; meshId < names.meshes().size(); meshId++) {
                meshNameToId.put(names.meshes().get(meshId), meshId);
            }

            var selectedMeshes = Arrays.stream(importedMeshes)
                    .filter(mesh -> meshNameToId.containsKey(mesh.mName().dataString()))
                    .sorted(Comparator.comparingInt(
                            mesh -> meshNameToId.get(mesh.mName().dataString())))
                    .toList();

            if (selectedMeshes.size() != names.meshes().size()) {
                throw new IllegalArgumentException(
                        "Configured mesh names do not match imported meshes. Expected "
                                + names.meshes() + ", found "
                                + selectedMeshes.stream()
                                .map(mesh -> mesh.mName().dataString())
                                .toList());
            }

            int vertexCount = 0;
            int indexCount = 0;
            int maxVertex = 0;
            for (var mesh : selectedMeshes) {
                vertexCount = Math.addExact(vertexCount, mesh.mNumVertices());
                indexCount = Math.addExact(
                        indexCount,
                        Math.multiplyExact(mesh.mNumFaces(), 3));
                maxVertex = Math.max(maxVertex, mesh.mNumFaces() * 3);
            }

            var alignment = glGetInteger(GL_SHADER_STORAGE_BUFFER_OFFSET_ALIGNMENT);
            var vertexBytes = Math.multiplyExact(vertexCount, VERTEX_BYTES);
            var indexBytes = Math.multiplyExact(indexCount, Integer.BYTES);
            var drawBytes = Math.multiplyExact(selectedMeshes.size(), Integer.BYTES);
            var indexOffset = alignUp(vertexBytes, alignment);
            var drawOffset = Math.addExact(indexOffset, alignUp(indexBytes, alignment));
            var totalBytes = Math.addExact(drawOffset, drawBytes);

            ByteBuffer vertexBuffer = null;
            ByteBuffer indexBuffer = null;
            ByteBuffer drawBuffer = null;
            ByteBuffer packedBuffer = null;
            int modelBuffer = 0;

            try {
                vertexBuffer = MemoryUtil.memAlloc(Math.max(1, vertexBytes));
                indexBuffer = MemoryUtil.memAlloc(Math.max(1, indexBytes));
                drawBuffer = MemoryUtil.memAlloc(Math.max(1, drawBytes));

                var meshDrawCounts = new int[selectedMeshes.size()];
                var counters = new int[2];
                var dimensions = new Vector3f();
                var options = config.modelOptions() != null
                        ? config.modelOptions()
                        : Collections.<String, IMeshOptions>emptyMap();

                for (int meshId = 0; meshId < selectedMeshes.size(); meshId++) {
                    meshDrawCounts[meshId] = processPrimitive(
                            vertexBuffer,
                            indexBuffer,
                            drawBuffer,
                            counters,
                            skeleton,
                            selectedMeshes.get(meshId),
                            options,
                            dimensions);
                }

                var vertex = new SbboOffset(0, vertexBytes);
                var index = new SbboOffset(indexOffset, indexBytes);
                var meshOffsets = new SbboOffset(drawOffset, drawBytes);

                packedBuffer = MemoryUtil.memCalloc(Math.max(1, totalBytes));
                if (vertexBytes > 0) {
                    vertex.put(packedBuffer, vertexBuffer.flip());
                }
                if (indexBytes > 0) {
                    index.put(packedBuffer, indexBuffer.flip());
                }
                if (drawBytes > 0) {
                    meshOffsets.put(packedBuffer, drawBuffer.flip());
                }

                modelBuffer = GL43C.glGenBuffers();
                glBindBuffer(GL_SHADER_STORAGE_BUFFER, modelBuffer);
                glBufferData(GL_SHADER_STORAGE_BUFFER, packedBuffer, GL_STATIC_READ);
                glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);

                return new GeometryBuild(
                        skeleton,
                        meshDrawCounts,
                        vertex,
                        index,
                        meshOffsets,
                        modelBuffer,
                        maxVertex,
                        dimensions,
                        calculateRootTransformation(rootNode));
            } catch (RuntimeException | Error failure) {
                if (modelBuffer != 0) {
                    GL43C.glDeleteBuffers(modelBuffer);
                }
                throw failure;
            } finally {
                glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
                if (packedBuffer != null) MemoryUtil.memFree(packedBuffer);
                if (drawBuffer != null) MemoryUtil.memFree(drawBuffer);
                if (indexBuffer != null) MemoryUtil.memFree(indexBuffer);
                if (vertexBuffer != null) MemoryUtil.memFree(vertexBuffer);
            }
        } finally {
            Assimp.aiReleaseImport(scene);
        }
    }

    private static int processPrimitive(
            ByteBuffer vertexBuffer,
            ByteBuffer indexBuffer,
            ByteBuffer drawBuffer,
            int[] counters,
            Skeleton skeleton,
            AIMesh mesh,
            Map<String, IMeshOptions> options,
            Vector3f dimensions) {
        var meshName = mesh.mName().dataString();
        var faceOrder = options.getOrDefault(meshName, IMeshOptions.DEFAULT).invert()
                ? INVERT_FACE
                : NORMAL_FACE;
        var vertexAmount = mesh.mNumVertices();
        var indexOffset = counters[0];
        var vertexOffset = counters[1];
        var drawCount = Math.multiplyExact(mesh.mNumFaces(), 3);

        drawBuffer.putInt(indexOffset);
        counters[0] += drawCount;

        var faces = mesh.mFaces();
        for (int faceId = 0; faceId < mesh.mNumFaces(); faceId++) {
            var indices = faces.get(faceId).mIndices();
            indexBuffer
                    .putInt(vertexOffset + indices.get(faceOrder[0]))
                    .putInt(vertexOffset + indices.get(faceOrder[1]))
                    .putInt(vertexOffset + indices.get(faceOrder[2]));
        }
        counters[1] += vertexAmount;

        var positions = requireNonNull(mesh.mVertices(), "Mesh positions missing: " + meshName);
        var uvs = requireNonNull(mesh.mTextureCoords(0), "Mesh UVs missing: " + meshName);
        var normals = requireNonNull(mesh.mNormals(), "Mesh normals missing: " + meshName);
        var tangents = requireNonNull(mesh.mTangents(), "Mesh tangents missing: " + meshName);
        var bitangents = requireNonNull(mesh.mBitangents(), "Mesh bitangents missing: " + meshName);

        var boneIds = new int[vertexAmount * 4];
        var boneWeights = new float[vertexAmount * 4];

        if (mesh.mBones() != null) {
            var bones = requireNonNull(mesh.mBones());
            for (int boneIndex = 0; boneIndex < bones.capacity(); boneIndex++) {
                var bone = AIBone.create(bones.get(boneIndex));
                var skeletonId = skeleton.getId(bone.mName().dataString());
                var weights = bone.mWeights();

                for (int weightId = 0; weightId < weights.capacity(); weightId++) {
                    var weight = weights.get(weightId);
                    if (weight.mWeight() > 0.0f) {
                        addBoneData(
                                boneIds,
                                boneWeights,
                                weight.mVertexId(),
                                skeletonId,
                                weight.mWeight());
                    }
                }
            }
        }

        var hasNoWeights = IntStream.range(0, boneWeights.length)
                .allMatch(index -> boneWeights[index] == 0.0f);

        for (int vertexId = 0; vertexId < vertexAmount; vertexId++) {
            var position = positions.get(vertexId);
            var uv = uvs.get(vertexId);
            var normal = normals.get(vertexId);

            vertexBuffer
                    .putFloat(position.x()).putFloat(position.y()).putFloat(position.z()).putFloat(0.0f)
                    .putFloat(uv.x()).putFloat(1.0f - (uv.y() % 1.0f)).putFloat(0.0f).putFloat(0.0f)
                    .putFloat(normal.x()).putFloat(normal.y()).putFloat(normal.z()).putFloat(0.0f);

            putTangent(
                    vertexBuffer,
                    tangents.get(vertexId),
                    bitangents.get(vertexId),
                    normal);

            if (hasNoWeights) {
                vertexBuffer.putInt(1).putInt(0).putInt(0).putInt(0);
                vertexBuffer.putFloat(1.0f).putFloat(0.0f).putFloat(0.0f).putFloat(0.0f);
            } else {
                var base = vertexId * 4;
                for (int index = 0; index < 4; index++) {
                    vertexBuffer.putInt(boneIds[base + index]);
                }
                for (int index = 0; index < 4; index++) {
                    vertexBuffer.putFloat(boneWeights[base + index]);
                }
            }

            dimensions.x = Math.max(dimensions.x, position.x());
            dimensions.y = Math.max(dimensions.y, position.y());
            dimensions.z = Math.max(dimensions.z, position.z());
        }

        return drawCount;
    }

    private static void putTangent(
            ByteBuffer target,
            AIVector3D tangent,
            AIVector3D bitangent,
            AIVector3D normal) {
        TANGENT_NORMAL.set(normal.x(), normal.y(), normal.z());
        TANGENT_VALUE.set(tangent.x(), tangent.y(), tangent.z());
        TANGENT_BITANGENT.set(bitangent.x(), bitangent.y(), bitangent.z());

        TANGENT_NORMAL.mul(TANGENT_VALUE.dot(TANGENT_NORMAL), TANGENT_TEMP);
        TANGENT_VALUE.sub(TANGENT_TEMP).normalize();
        TANGENT_NORMAL.cross(TANGENT_VALUE, TANGENT_TEMP);
        var handedness = TANGENT_TEMP.dot(TANGENT_BITANGENT) < 0.0f ? -1.0f : 1.0f;

        target
                .putFloat(TANGENT_VALUE.x())
                .putFloat(TANGENT_VALUE.y())
                .putFloat(TANGENT_VALUE.z())
                .putFloat(handedness);
    }

    private static void addBoneData(
            int[] ids,
            float[] weights,
            int vertexId,
            int boneId,
            float weight) {
        var base = vertexId * 4;
        for (int influence = 0; influence < 4; influence++) {
            var index = base + influence;
            if (weights[index] == 0.0f) {
                ids[index] = boneId;
                weights[index] = weight;
                return;
            }
        }
    }

    private static Matrix4f calculateRootTransformation(ModelNode rootNode) {
        var result = new Matrix4f();
        accumulateNodeTransforms(rootNode, result);
        return result;
    }

    private static void accumulateNodeTransforms(ModelNode node, Matrix4f result) {
        result.add(node.transform);
        for (var child : node.children) {
            accumulateNodeTransforms(child, result);
        }
    }

    private static int alignUp(int value, int alignment) {
        if (alignment <= 0) {
            throw new IllegalArgumentException("alignment must be positive");
        }
        return Math.multiplyExact((value + alignment - 1) / alignment, alignment);
    }

    private static void requireCompatibleLayout(
            MultiRenderObject object,
            IModelConfig config) {
        requireNonNull(object, "object");
        requireNonNull(config, "config");

        var generated = Names.generateNames(config);
        if (!object.names.equals(generated)) {
            throw new IllegalArgumentException(
                    "The edited config changes the object name/ID layout. Existing: "
                            + object.names + ", generated: " + generated);
        }
    }

    private static boolean containsIdentity(Material[] materials, Material target) {
        if (target == null) {
            return false;
        }
        for (var material : materials) {
            if (material == target) {
                return true;
            }
        }
        return false;
    }

    private static void closeQuietly(Material material) {
        if (material == null) {
            return;
        }
        try {
            material.close();
        } catch (Exception ignored) {
            // Cleanup must not replace the original compilation result or failure.
        }
    }

    private static void deleteIfReplaced(SSBOBuffer previous, SSBOBuffer replacement) {
        if (previous != null && previous != replacement) {
            previous.delete();
        }
    }

    private record VariantBuild(
            Variant[] variants,
            int[][] relationships,
            RenderStage[][] stages,
            EnumMap<RenderStage, DrawBuffer> drawBuffers,
            SSBOBuffer buffer) {}

    private record GeometryBuild(
            Skeleton skeleton,
            int[] meshDrawCounts,
            SbboOffset vertex,
            SbboOffset index,
            SbboOffset meshOffsets,
            int modelBuffer,
            int maxVertex,
            Vector3f dimensions,
            Matrix4f rootTransformation) {}

    private record ResolvedVariantDetails(
            String material,
            String effect,
            Boolean paradox,
            Boolean hide,
            ITransformSet transform) implements IVariantDetails {}

    public static AIScene read(ResourceReader asset) throws IOException {
        byte[] bytes = asset.getFile("model.glb");

        ByteBuffer buffer = MemoryUtil.memAlloc(bytes.length);
        buffer.put(bytes).flip();

        try {
            AIScene scene = Assimp.aiImportFileFromMemory(
                    buffer,
                    Assimp.aiProcess_Triangulate
                            | Assimp.aiProcess_OptimizeMeshes
                            | Assimp.aiProcess_ImproveCacheLocality
                            | Assimp.aiProcess_CalcTangentSpace,
                    "glb"
            );

            if (scene == null) {
                throw new RuntimeException(Assimp.aiGetErrorString());
            }

            return scene;
        } finally {
            MemoryUtil.memFree(buffer);
        }
    }

    public static TextureArray readImages(ResourceReader asset, List<String> imageNames, int resolution, boolean usesViews) throws IOException {
        var array = new TextureArray(resolution, resolution,imageNames.size(), usesViews);

        for (int i = 0; i < imageNames.size(); i++) {
            var image = Texture.getColorBuffer(asset.getFile(imageNames.get(i)), resolution);

            array.fillLayer(i, image);

            STBImage.stbi_image_free(image);
        }

        return array;
    }
}


