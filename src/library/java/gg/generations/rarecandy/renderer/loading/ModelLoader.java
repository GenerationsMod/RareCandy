package gg.generations.rarecandy.renderer.loading;

import gg.generations.rarecandy.pokeutils.*;
import gg.generations.rarecandy.pokeutils.resource.ResourceReader;
import gg.generations.rarecandy.pokeutils.util.ExceptionThrowingBiFunction;
import gg.generations.rarecandy.pokeutils.util.ExceptionThrowingConsumer;
import gg.generations.rarecandy.pokeutils.util.ExceptionThrowingFunction;
import gg.generations.rarecandy.pokeutils.util.ExceptionThrowingTriFunction;
import gg.generations.rarecandy.renderer.animation.Animation;
import gg.generations.rarecandy.renderer.animation.Skeleton;
import gg.generations.rarecandy.renderer.animation.TransformSet;
import gg.generations.rarecandy.renderer.components.InstanceDetails;
import gg.generations.rarecandy.renderer.components.MultiRenderObject;
import gg.generations.rarecandy.renderer.model.*;
import gg.generations.rarecandy.renderer.model.material.IMaterialImages;
import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.rendering.RenderStage;
import gg.generations.rarecandy.renderer.storage.DrawBuffer;
import gg.generations.rarecandy.renderer.storage.SSBOBuffer;
import gg.generations.rarecandy.renderer.textures.Texture;
import gg.generations.rarecandy.renderer.textures.TextureArray;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.assimp.*;
import org.lwjgl.opengl.*;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.*;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL43C.*;

public class ModelLoader {
    private static final Vector3f temp = new Vector3f();
    private static final int TRANSFORM_ENTRY_BYTES = Float.BYTES * 4 + Integer.BYTES * 4;
    private static final int MATERIAL_SIZE = 208;

    static int alignUp(int value, int alignment) {
        return (value + alignment - 1) / alignment * alignment;
    }

    public static void processModel(
            MultiRenderObject objects,
            Names names,
            ResourceReader asset,
            Map<String, AnimResource> animResources,
            IModelConfig config) throws IOException {
        if (config == null) throw new RuntimeException("config.json can't be null.");

        var scene = ModelLoader.read(asset);

        var rootNode = ModelNode.create(scene.mRootNode());

        var meshes = IntStream.range(0, scene.mNumMeshes()).mapToObj(i -> AIMesh.create(scene.mMeshes().get(i))).toArray(AIMesh[]::new);

        Skeleton skeleton = new Skeleton(rootNode, meshes, config.excludeMeshNamesFromSkeleton());

        processAnimations(objects, skeleton, animResources, names, config);

        var dimensions = new Vector3f();

        var vertexCount = 0;
        var indexCount = 0;


        int alignment = glGetInteger(GL_SHADER_STORAGE_BUFFER_OFFSET_ALIGNMENT);

        int maxVertex = 0;

        for (var mesh : meshes) {
            maxVertex = Math.max(maxVertex, mesh.mNumFaces() * 3);

            vertexCount += mesh.mNumVertices();
            indexCount += mesh.mNumFaces() * 3;
        }

        objects.maxVertex = maxVertex;



        var indexBytes = indexCount * Integer.BYTES;

        int drawBytes = meshes.length * Integer.BYTES;

        int vertexBytes = vertexCount * 96;

        int materialBytes = objects.materials.length * MATERIAL_SIZE;
        int variantBytes = objects.variants.length * Variant.SIZE;

        int indexOffset = alignUp(vertexBytes, alignment);
        int drawOffset  = indexOffset + alignUp(indexBytes, alignment);
        int totalBytes = drawOffset + drawBytes;

        var vertexBuffer = MemoryUtil.memAlloc(vertexBytes);
        var indexBuffer = MemoryUtil.memAlloc(indexBytes);
        var drawBuffer = MemoryUtil.memAlloc(drawBytes);

        objects.vertex = new SbboOffset(0, vertexBytes);
        objects.index = new SbboOffset(indexOffset, indexBytes);
        objects.meshOffsets = new SbboOffset(drawOffset, drawBytes);
        objects.material = new SSBOBuffer(materialBytes);
        objects.variant = new SSBOBuffer(variantBytes);

        int[] counters = new int[2]; // index Count

        var list = Arrays.stream(meshes).filter(a -> objects.meshNameToId.containsKey(a.mName().dataString())).sorted(Comparator.comparing(aiMesh -> objects.meshNameToId.get(aiMesh.mName().dataString()))).toList();

        for (int i = 0; i < names.meshes.size(); i++) {
            var mesh = list.get(i);

            objects.meshes[i] = processPrimitiveModel(
                    vertexBuffer,
                    indexBuffer,
                    drawBuffer,
                    counters, skeleton, mesh, config.modelOptions() != null ? config.modelOptions() : Collections.emptyMap(), dimensions);
        }

        for (int i = 0; i < objects.materials.length; i++) {
            var material = objects.materials[i];
            if (material != null) {
                material.put(objects.material);
            }
        }

        for(var variant : objects.variants) {
            variant.put(objects.variant);
        }

        objects.material.upload();
        objects.variant.upload();

        var buffer = MemoryUtil.memAlloc(totalBytes);
        objects.vertex.put(buffer, vertexBuffer.flip());
        objects.index.put(buffer, indexBuffer.flip());
        objects.meshOffsets.put(buffer, drawBuffer.flip());

        var bufferId = GL43C.glGenBuffers();
        GL43C.glBindBuffer(GL_SHADER_STORAGE_BUFFER, bufferId);
        glBufferData(GL43C.GL_SHADER_STORAGE_BUFFER, buffer, GL43C.GL_STATIC_READ);
        GL43C.glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
        objects.modelBuffer = bufferId;

        objects.drawInfo = new SSBOBuffer(TRANSFORM_ENTRY_BYTES * objects.meshes.length);
        objects.instance = new SSBOBuffer(InstanceDetails.size);

        MemoryUtil.memFree(buffer);
        MemoryUtil.memFree(vertexBuffer);
        MemoryUtil.memFree(indexBuffer);
        MemoryUtil.memFree(drawBuffer);

        var transform = new Matrix4f();

        traverseTree(transform, rootNode, objects);

        Assimp.aiReleaseImport(scene);

    }

    private static void processAnimations(MultiRenderObject obj, Skeleton skeleton, Map<String, AnimResource> animResources, Names names, IModelConfig config) {
        obj.animations = new Animation[animResources.size()];
        obj.hideDuringAnimation = new boolean[obj.meshes.length][obj.animations.length];
        obj.animationNameToId = new HashMap<>();
        obj.animationNames = new String[obj.animations.length];

        var offSetsToInsert = new HashMap<String, Animation.Offset>();

        int i = 0;
        for (Map.Entry<String, AnimResource> entry : animResources.entrySet()) {
            String name = entry.getKey();
            AnimResource animResource = entry.getValue();
            var fps = animResource.fps();
            fps = config.animationFpsOverride() != null && config.animationFpsOverride().containsKey(name) ? config.animationFpsOverride().get(name) : fps;

            var loops = animResource.loops();
            loops = config.animationLoopsOverride() != null && config.animationLoopsOverride().containsKey(name) ? config.animationLoopsOverride().get(name) : loops;

            var offsets = animResource.getOffsets();
            offsets.forEach((trackName, offset) -> config.getMaterialsForAnimation(trackName).forEach(a -> offSetsToInsert.put(a, offset)));
            offsets.putAll(offSetsToInsert);
            offSetsToInsert.clear();

            Animation.Offset[] offsetsArray = new Animation.Offset[names.materials.size()];

            offsets.forEach((s, offset) -> {
                var id = names.materials.indexOf(s);

                if(id != -1) {
                    offsetsArray[id] = offset;
                }
            });

            var nodes = animResource.getNodes();
            var ignoreScaling = config.ignoreScaleInAnimation() != null && (config.ignoreScaleInAnimation().contains(name) || config.ignoreScaleInAnimation().contains("all"));

            obj.animations[i] = new Animation(i, (int) fps, loops, skeleton, nodes, offsetsArray, ignoreScaling, config.offsets().getOrDefault(name, ISkeletalTransform.DEFAULT).scale(config.scale()));
            obj.animationNameToId.put(name, i);
            obj.animationNames[i] = name;

            i++;
        }

        for (int mesh = 0; mesh < names.meshes.size(); mesh++) {
            var meshName = names.meshes.get(mesh);

            var defaultAnimation = config.hideDuringAnimation().getOrDefault(meshName, IHideDuringAnimation.NONE);

            for (int animation = 0; animation < obj.animations.length; animation++) {
                var animationsName = obj.animationNames[animation];

                obj.hideDuringAnimation[mesh][animation] = defaultAnimation.check(animationsName);
            }
        }
    }

    private static void processVariants(
            MultiRenderObject object,
            IModelConfig config,
            Names names,
            Map<String, List<String>> aliases,
            Map<String, Map<String, IVariantDetails>> variantCache) {
        var defaultVariant = new int[names.meshes.size()];

        var variantList = new ArrayList<Variant>();

        var defaultVariants = config.defaultVariant();
        if(defaultVariants != null) {
            defaultVariants.forEach((k, v) -> {
                if(v == null) return;
                var mesh = names.meshes.indexOf(k);

                var variant = addOrGetIndex(variantList, createVariant(v, names));

                if(!aliases.isEmpty() && aliases.containsKey(k)) {
                    for (String s : aliases.get(k)) {
                        mesh = names.meshes.indexOf(s);

                        defaultVariant[mesh] = variant;
                    }
                }
                else defaultVariant[mesh] = variant;

            });
        }

        var variantsMap = config.variants();
        if(variantsMap != null) {
            variantsMap.forEach((variantKey, variantParent) -> {
                var variantIndex = names.variants.indexOf(variantKey);

                var map = resolvedVariant(variantKey, config, variantCache, new HashSet<>());

                applyVariant(object, variantIndex, map, aliases, names, variantList);
            });
        } else {
            for (int mesh = 0; mesh < defaultVariant.length; mesh++) {
                var variant = defaultVariant[mesh];

                set(object, mesh, 0, variant, variantList);
            }
        }

        object.variants = variantList.toArray(Variant[]::new);
    }

    private static void traverseTree(Matrix4f transform, ModelNode node, MultiRenderObject objects) {
        applyTransforms(transform, node);

        objects.setRootTransformation(objects.getRootTransformation().add(transform, new Matrix4f()));

        for (var child : node.children) {
            traverseTree(transform, child, objects);
        }
    }

    private static Map<String, IVariantDetails> resolvedVariant(
            String variantKey,
            IModelConfig config,
            Map<String, Map<String, IVariantDetails>> cache,
            Set<String> resolving) {
        var cached = cache.get(variantKey);
        if(cached != null) return cached;

        var map = new LinkedHashMap<String, IVariantDetails>();
        var variants = config.variants();
        var variantParent = variants != null ? variants.get(variantKey) : null;

        if(variantParent != null) {
            applyVariantDetails(variantParent.details(), map);

            var parent = variantParent.inherits();
            if(parent != null && !variantKey.equals(parent) && resolving.add(variantKey)) {
                applyVariantDetails(resolvedVariant(parent, config, cache, resolving), map);
                resolving.remove(variantKey);
            } else {
                applyVariantDetails(config.defaultVariant(), map);
            }
        } else {
            applyVariantDetails(config.defaultVariant(), map);
        }

        cache.put(variantKey, map);
        return map;
    }

    private static void applyVariantDetails(Map<String, ? extends IVariantDetails> applied, Map<String, IVariantDetails> appliee) {
        if(applied == null) return;

        for (Map.Entry<String, ? extends IVariantDetails> entry : applied.entrySet()) {
            String k = entry.getKey();
            IVariantDetails v = entry.getValue();
            if(v == null) continue;
            appliee.compute(k, (s, variantDetails) -> {
                if(variantDetails == null) return v;
                return fillIn(variantDetails, v);
            });
        }
    }

    private static IVariantDetails fillIn(IVariantDetails details, IVariantDetails filler) {
        if(filler == null) return details;

        var newMaterial = details.material();
        var newEffect = details.effect();
        var newParadox = details.paradox();
        var newHide = details.hide();
        var newTransform = details.transform();

        if (newMaterial == null) newMaterial = filler.material();
        if (newEffect == null) newEffect = filler.effect();
        if (newParadox == null) newParadox = filler.paradox() != null ? filler.paradox() : false;
        if (newHide == null) newHide = filler.hide() != null ? filler.hide() : false;
        if (newTransform == null) newTransform = filler.transform();
        else if(filler.transform() != null) newTransform = newTransform.fillIn(filler.transform());
        return new VariantDetails(newMaterial, newEffect, newParadox, newHide, newTransform);
    }

    private static Variant createVariant(IVariantDetails details, Names names) {
        int effect = 0;
        if (details.effect() != null) {
            effect = switch (details.effect()) {
                case "galaxy" -> 1;
                case "pastel" -> 2;
                case "shadow" -> 3;
                case "sketch" -> 4;
                case "vintage" -> 5;
                default -> 0;
            };
        }

        var paradox = details.paradox() != null && details.paradox();
        var hide = details.hide() != null && details.hide();
        return new Variant(names.materials.indexOf(details.material()), effect, paradox, hide, (details.transform() != null ? details.transform() : TransformSet.DEFAULT).array());
    }

    private static void applyVariant(
            MultiRenderObject object,
            int variantKey,
            Map<String, ? extends IVariantDetails> variantMap,
            Map<String, List<String>> aliases,
            Names names,
            List<Variant> variants) {
        variantMap.forEach((k, v) -> {
            var mesh = names.meshes.indexOf(k);

            var variant = addOrGetIndex(variants, createVariant(v, names));

            if(!aliases.isEmpty() && aliases.containsKey(k)) {
                for (String s : aliases.get(k)) {
                    mesh = names.meshes.indexOf(s);

                    set(object, mesh, variantKey, variant, variants);
                }
            }
            else {
                set(object, mesh, variantKey, variant, variants);
            }
        });
    }

    private static void set(MultiRenderObject object, int mesh, int variantKey, int variant, List<Variant> variants) {
        var material = object.materials[variants.get(variant).material()];
        var stage = RenderStage.from(material);

        object.drawBuffer.computeIfAbsent(stage, a -> new DrawBuffer(Integer.BYTES * 4 * object.meshes.length));
        object.stageRelationships[mesh][variantKey] = stage;
        object.variantRelationships[mesh][variantKey] = variant;
    }

    private static int addOrGetIndex(List<Variant> variants, Variant variant) {
        var index = variants.indexOf(variant);

        if(index == -1) {
            index = variants.size();
            variants.add(variant);
        }
        return index;
    }

    private static void applyTransforms(Matrix4f transform, ModelNode node) {
        transform.set(node.transform);
    }

    private static final int[] NORMAL_FACE = new int[] { 0,1,2 };
    private static final int[] INVERT_FACE = new int[] { 2,1,0 };

    private static int processPrimitiveModel(
            ByteBuffer vertexBuffer,
            ByteBuffer indexBuffer,
            ByteBuffer drawBuffer,
            int[] counters, Skeleton skeleton, AIMesh mesh, Map<String, IMeshOptions> options, Vector3f dimensions) {
        var name = mesh.mName().dataString();

        var faceArray = options.containsKey(name) && options.get(name).invert() ? INVERT_FACE : NORMAL_FACE;

        var amount = mesh.mNumVertices();

        var aiFaces = mesh.mFaces();

        var indexOffset = counters[0];
        var vertexOffset = counters[1];

        var numFaces = mesh.mNumFaces();
        var drawRecord = numFaces * 3;

        drawBuffer.putInt(indexOffset);

        counters[0] += drawRecord;

        for (int j = 0; j < numFaces; j++) {
            var aiFace = aiFaces.get(j).mIndices();
            indexBuffer
                    .putInt(vertexOffset + aiFace.get(faceArray[0]))
                    .putInt(vertexOffset + aiFace.get(faceArray[1]))
                    .putInt(vertexOffset + aiFace.get(faceArray[2]));
        }

        counters[1] += amount;

        var aiVert = mesh.mVertices();
        var aiUV = mesh.mTextureCoords(0);

        if (aiUV == null) {
            throw new RuntimeException("Error UV coordinates not found!");
        }

        var aiNormals = mesh.mNormals();

        if (aiNormals == null) {
            throw new RuntimeException("Error Normals not found!");
        }

        var aiTangents = mesh.mTangents();

        if (aiTangents == null) {
            throw new RuntimeException("Error Tangents not found!");
        }

        var aiBitangents = mesh.mBitangents();

        if (aiBitangents == null) {
            throw new RuntimeException("Error Bitangents not found!");
        }

        int[] ids = new int[amount * 4];
        float[] weights = new float[amount * 4];

        if (mesh.mBones() != null) {
            var aiBones = requireNonNull(mesh.mBones());

            for (int boneIndex = 0; boneIndex < aiBones.capacity(); boneIndex++) {
                var aiBone = AIBone.create(aiBones.get(boneIndex));

                var weight = aiBone.mWeights();

                var index = skeleton.getId(aiBone.mName().dataString());

                for (int weightId = 0; weightId < weight.capacity(); weightId++) {
                    var aiWeight = weight.get(weightId);
                    var vertexId = aiWeight.mVertexId();

                    if(aiWeight.mWeight() > 0f) {
                        addBoneData(ids, weights, vertexId, index, aiWeight.mWeight());
                    }
                }
            }
        }

        var isEmpty = IntStream.range(0, ids.length).allMatch(a -> ids[a] == 0);

        for (int i = 0; i < amount; i++) {

            var position = aiVert.get(i);
            var uv = aiUV.get(i);
            var normal = aiNormals.get(i);
            var tangent = aiTangents.get(i);
            var bitangent = aiBitangents.get(i);

            vertexBuffer.putFloat(position.x());
            vertexBuffer.putFloat(position.y());
            vertexBuffer.putFloat(position.z());
            vertexBuffer.putFloat(0);
            vertexBuffer.putFloat(uv.x());
            vertexBuffer.putFloat(1 - (uv.y() % 1.0f));
            vertexBuffer.putFloat(0);
            vertexBuffer.putFloat(0);
            vertexBuffer.putFloat(normal.x());
            vertexBuffer.putFloat(normal.y());
            vertexBuffer.putFloat(normal.z());
            vertexBuffer.putFloat(0);

            computeTangent(vertexBuffer, tangent, bitangent, normal);

            if(isEmpty) {
                vertexBuffer.putInt(1);
                vertexBuffer.putInt(0);
                vertexBuffer.putInt(0);
                vertexBuffer.putInt(0);

                vertexBuffer.putFloat(1);
                vertexBuffer.putFloat(0);
                vertexBuffer.putFloat(0);
                vertexBuffer.putFloat(0);
            } else {
                vertexBuffer.putInt(ids[i * 4]);
                vertexBuffer.putInt(ids[i * 4 + 1]);
                vertexBuffer.putInt(ids[i * 4 + 2]);
                vertexBuffer.putInt(ids[i * 4 + 3]);

                vertexBuffer.putFloat(weights[i * 4]);
                vertexBuffer.putFloat(weights[i * 4 + 1]);
                vertexBuffer.putFloat(weights[i * 4 + 2]);
                vertexBuffer.putFloat(weights[i * 4 + 3]);
            }

            dimensions.max(temp.set(position.x(), position.y(), position.z()));
        }

        return drawRecord;
    }

    private static final Vector3f N = new Vector3f(), T = new Vector3f(), B = new Vector3f(), TEMP = new Vector3f();

    private static void computeTangent(ByteBuffer vertexBuffer, AIVector3D tangent, AIVector3D bitangent, AIVector3D normal) {
        N.set(normal.x(),    normal.y(),    normal.z());
        T.set(tangent.x(),   tangent.y(),   tangent.z());
        B.set(bitangent.x(), bitangent.y(), bitangent.z());

        // Gram-Schmidt
        N.mul(T.dot(N), TEMP);
        T.sub(TEMP).normalize();

        // Handedness from original B
        N.cross(T, TEMP);
        float handedness = TEMP.dot(B) < 0f ? -1f : 1f;

        // Reconstruct clean B
        N.cross(T, B);
        B.mul(handedness);

        vertexBuffer.putFloat(T.x()); vertexBuffer.putFloat(T.y()); vertexBuffer.putFloat(T.z()).putFloat(handedness);
    }

    public static void addBoneData(int[] ids, float[] weights, int vertexId, int boneId, float weight) {
        var length = vertexId * 4;
        for (var i = 0 ; i < 4; i++) {
            var blep = length + i;

            if (weights[blep] == 0.0) {
                ids[blep] = boneId;
                weights[blep] = weight;

                return;
            }
        }
    }

    public record Names(List<String> meshes, List<String> variants, List<String> images, List<String> materials) {
        public Names() {
            this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }
    }

    public static Names generateNames(IModelConfig config) {
        var names = new Names();
        var aliases = config.aliases() != null ? config.aliases() : Collections.<String, List<String>>emptyMap();

        collectVariantNames(names, config.defaultVariant(), config.variants(), aliases, config.meshesToRenderFirst());
        collectMaterialImageNames(names, config.materials());

        return names;
    }

    private static Names generateNames(IModelConfig config, Map<String, IMaterialReference> completedMaterials) {
        var names = new Names();
        var aliases = config.aliases() != null ? config.aliases() : Collections.<String, List<String>>emptyMap();

        collectVariantNames(names, config, aliases);

        for (String materialName : names.materials()) {
            var reference = completedMaterial(materialName, config.materials(), completedMaterials);
            if(reference != null && reference.images() != null) {
                addMaterialImages(names, reference.images());
            }
        }

        return names;
    }

    private static void collectVariantNames(
            Names names,
            Map<String, ? extends IVariantDetails> defaultVariant,
            Map<String, ? extends IVariantParent> variants,
            Map<String, List<String>> aliases,
            List<String> meshesToRenderFirst) {
        if(defaultVariant != null) {
            defaultVariant.forEach((s, variantDetails) -> {
                if(variantDetails == null) return;
                addMeshNames(names, s, aliases, meshesToRenderFirst != null && meshesToRenderFirst.contains(s));
                checkIfAlreadyIn(names.materials(), variantDetails.material());
            });
        }

        if(variants != null) {
            variants.forEach((s, variantParent) -> {
                checkIfAlreadyIn(names.variants(), s);

                if(variantParent == null) return;
                if (variantParent.details() != null) {
                    variantParent.details().forEach((s1, variantDetails) -> {
                        if(variantDetails == null) return;
                        addMeshNames(names, s1, aliases, false);
                        if(variantDetails.material() != null) checkIfAlreadyIn(names.materials(), variantDetails.material());
                    });
                }
            });
        }
    }

    private static void collectVariantNames(Names names, IModelConfig config, Map<String, List<String>> aliases) {
        if(config.defaultVariant() != null) {
            config.defaultVariant().forEach((s, variantDetails) -> {
                if(variantDetails == null) return;
                addMeshNames(names, s, aliases, config.meshesToRenderFirst() != null && config.meshesToRenderFirst().contains(s));
                checkIfAlreadyIn(names.materials(), variantDetails.material());
            });
        }

        if(config.variants() != null) {
            config.variants().forEach((s, variantParent) -> {
                checkIfAlreadyIn(names.variants(), s);

                if(variantParent == null) return;
                if (variantParent.details() != null) {
                    variantParent.details().forEach((s1, variantDetails) -> {
                        if(variantDetails == null) return;
                        addMeshNames(names, s1, aliases, false);
                        if(variantDetails.material() != null) checkIfAlreadyIn(names.materials(), variantDetails.material());
                    });
                }
            });
        }
    }

    private static void addMeshNames(Names names, String mesh, Map<String, List<String>> aliases, boolean addFirst) {
        if(!aliases.isEmpty() && aliases.containsKey(mesh)) {
            for (String alias : aliases.get(mesh)) {
                checkIfAlreadyIn(names.meshes(), alias, addFirst);
            }
        } else {
            checkIfAlreadyIn(names.meshes(), mesh, addFirst);
        }
    }

    private static void collectMaterialImageNames(Names names, Map<String, ? extends IMaterialReference> materials) {
        if(materials == null) return;

        var cache = new HashMap<String, MaterialImageNames>();

        for (String materialName : names.materials()) {
            addMaterialImages(names, materialImageNames(materialName, materials, cache, new HashSet<>()));
        }
    }

    private static MaterialImageNames materialImageNames(
            String materialName,
            Map<String, ? extends IMaterialReference> materials,
            Map<String, MaterialImageNames> cache,
            Set<String> resolving) {
        var cached = cache.get(materialName);
        if(cached != null) return cached;

        var reference = materials.get(materialName);
        if(reference == null) return MaterialImageNames.EMPTY;

        var images = reference.images();
        var imageNames = images != null
                ? new MaterialImageNames(images.diffuse(), images.layer(), images.mask(), images.emission())
                : MaterialImageNames.EMPTY;

        var parent = reference.parent();
        if(parent != null && resolving.add(materialName)) {
            imageNames = imageNames.fill(materialImageNames(parent, materials, cache, resolving));
            resolving.remove(materialName);
        }

        cache.put(materialName, imageNames);
        return imageNames;
    }

    private static IMaterialReference completedMaterial(
            String materialName,
            Map<String, IMaterialReference> materials,
            Map<String, IMaterialReference> cache) {
        if(materials == null) return null;

        var cached = cache.get(materialName);
        if(cached != null) return cached;

        var reference = materials.get(materialName);
        if(reference == null) return null;

        reference = IMaterialReference.complete(materialName, materials);
        cache.put(materialName, reference);
        return reference;
    }

    private static void addMaterialImages(Names names, MaterialImageNames images) {
        checkIfAlreadyIn(names.images(), images.diffuse());
        checkIfAlreadyIn(names.images(), images.layer());
        checkIfAlreadyIn(names.images(), images.emission());
        checkIfAlreadyIn(names.images(), images.mask());
    }

    private static void addMaterialImages(Names names, IMaterialImages images) {
        checkIfAlreadyIn(names.images(), images.diffuse());
        checkIfAlreadyIn(names.images(), images.layer());
        checkIfAlreadyIn(names.images(), images.emission());
        checkIfAlreadyIn(names.images(), images.mask());
    }

    private record MaterialImageNames(String diffuse, String layer, String mask, String emission) {
        private static final MaterialImageNames EMPTY = new MaterialImageNames(null, null, null, null);

        private MaterialImageNames fill(MaterialImageNames parent) {
            return new MaterialImageNames(
                    parent.diffuse != null ? parent.diffuse : diffuse,
                    parent.layer != null ? parent.layer : layer,
                    parent.mask != null ? parent.mask : mask,
                    parent.emission != null ? parent.emission : emission
            );
        }
    }

    public static void rebuildMaterials(
            MultiRenderObject object,
            IModelConfig config,
            BiFunction<IMaterialReference, List<String>, Material> materialProcess) {
        var completedMaterials = new HashMap<String, IMaterialReference>();
        var rebuiltMaterials = new Material[object.names.materials().size()];

        for (int materialId = 0; materialId < rebuiltMaterials.length; materialId++) {
            var materialName = object.names.materials().get(materialId);
            var reference = completedMaterial(materialName, config.materials(), completedMaterials);

            if (reference == null) {
                throw new IllegalArgumentException("Missing material: " + materialName);
            }

            rebuiltMaterials[materialId] = materialProcess.apply(reference, object.names.images());
        }

        var oldMaterials = object.materials;
        object.materials = rebuiltMaterials;

        object.material.ensureCapacity((long) rebuiltMaterials.length * MATERIAL_SIZE);
        object.material.reset();

        for (var material : rebuiltMaterials) {
            material.put(object.material);
        }

        object.material.upload();

        for (int meshId = 0; meshId < object.variantRelationships.length; meshId++) {
            for (int variantId = 0; variantId < object.variantRelationships[meshId].length; variantId++) {
                var resolvedVariant = object.variants[object.variantRelationships[meshId][variantId]];
                var stage = RenderStage.from(object.materials[resolvedVariant.material()]);

                object.stageRelationships[meshId][variantId] = stage;
                object.drawBuffer.computeIfAbsent(
                        stage,
                        ignored -> new DrawBuffer(Integer.BYTES * 4 * object.meshes.length));
            }
        }

        for (var material : oldMaterials) {
            if (material != null) {
                material.close();
            }
        }
    }

    public static void rebuildVariants(MultiRenderObject object, IModelConfig config) {
        var aliases = config.aliases() != null
                ? config.aliases()
                : Collections.<String, List<String>>emptyMap();

        object.variantRelationships = new int[object.meshes.length][object.names.variants().size()];
        object.stageRelationships = new RenderStage[object.meshes.length][object.names.variants().size()];

        processVariants(object, config, object.names, aliases, new HashMap<>());

        object.variant.ensureCapacity((long) object.variants.length * Variant.SIZE);
        object.variant.reset();

        for (var variant : object.variants) {
            variant.put(object.variant);
        }

        object.variant.upload();
    }

    public static MultiRenderObject createObject(Function<Names, MultiRenderObject> objBuilder, Supplier<IModelConfig> configSupplier, @NotNull Supplier<ResourceReader> is, ExceptionThrowingTriFunction<ResourceReader, List<String>, Integer, TextureArray> imageConsumer, ExceptionThrowingBiFunction<IMaterialReference, List<String>, Material> materialProcess, ExceptionThrowingConsumer<MultiRenderObject> onFinish) throws Exception {
        var asset = is.get();

        var config = configSupplier.get();

        Map<String, List<String>> aliases = config.aliases() != null ? config.aliases() : Collections.emptyMap();
        var completedMaterials = new HashMap<String, IMaterialReference>();
        var resolvedVariants = new HashMap<String, Map<String, IVariantDetails>>();

        var names = generateNames(config, completedMaterials);

        var obj = objBuilder.apply(names);
        obj.images = imageConsumer.apply(asset, names.images, config.resolution() != null ? config.resolution() : 1024);

        for (String name : names.materials) {
            IMaterialReference reference = completedMaterials.get(name);
            var id = obj.materialNameToId.getOrDefault(name, -1);

            if (id != -1 && reference != null) {
                var material = materialProcess.apply(reference, names.images);

                obj.materials[id] = material;
            }
        }

        processVariants(obj, config, names, aliases, resolvedVariants);

        obj.scale = config.scale();

        var aninResouces = new HashMap<String, AnimResource>();

        SmdResource.read(asset, aninResouces);
        GfbanmResource.read(asset, aninResouces);
        TrAnimationResource.read(asset, aninResouces);

        ModelLoader.processModel(obj, names, asset, aninResouces, config);
        if (onFinish != null) onFinish.accept(obj);

        return obj;
    }

    private static void checkIfAlreadyIn(List<String> list, String entry) {
        if(entry == null) return;
        checkIfAlreadyIn(list, entry, false);
    }

    private static void checkIfAlreadyIn(List<String> list, String entry, boolean addFirst) {
        if(!list.contains(entry)) if(addFirst) list.addFirst(entry); else list.add(entry);
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

    public void close() {
//        modelLoadingPool.shutdown();
    }

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

    public static Matrix4f from(Matrix4f transform, AIMatrix4x4 aiMat4) {
        return transform.set(aiMat4.a1(), aiMat4.a2(), aiMat4.a3(), aiMat4.a4(),
                aiMat4.b1(), aiMat4.b2(), aiMat4.b3(), aiMat4.b4(),
                aiMat4.c1(), aiMat4.c2(), aiMat4.c3(), aiMat4.c4(),
                aiMat4.d1(), aiMat4.d2(), aiMat4.d3(), aiMat4.d4());
    }
}
