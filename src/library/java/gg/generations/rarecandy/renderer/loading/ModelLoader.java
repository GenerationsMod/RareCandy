package gg.generations.rarecandy.renderer.loading;

import gg.generations.rarecandy.pokeutils.*;
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader;
import gg.generations.rarecandy.renderer.animation.Animation;
import gg.generations.rarecandy.renderer.animation.Skeleton;
import gg.generations.rarecandy.renderer.components.DrawRecord;
import gg.generations.rarecandy.renderer.components.InstanceDetails;
import gg.generations.rarecandy.renderer.components.MultiRenderObject;
import gg.generations.rarecandy.renderer.model.*;
import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.storage.SSBOBuffer;
import gg.generations.rarecandy.renderer.textures.Texture;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.assimp.*;
import org.lwjgl.opengl.*;
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
    public static int byteAmount;

    private static final Vector3f temp = new Vector3f();

    public static List<Attribute> ATTRIBUTES = List.of(
            Attribute.POSITION,
            Attribute.TEXCOORD,
            Attribute.NORMAL,
            Attribute.BONE_IDS,
            Attribute.BONE_WEIGHTS
    );

    static int alignUp(int value, int alignment) {
        return (value + alignment - 1) / alignment * alignment;
    }

    public static void processModel(
            MultiRenderObject objects,
            Names names,
            PixelAsset asset,
            Map<String, AnimResource> animResources,
            ModelConfig config) {
        if (config == null) throw new RuntimeException("config.json can't be null.");

        var scene = ModelLoader.read(asset);

        var rootNode = ModelNode.create(scene.mRootNode());

        var meshes = IntStream.range(0, scene.mNumMeshes()).mapToObj(i -> AIMesh.create(scene.mMeshes().get(i))).toArray(AIMesh[]::new);
        
        Skeleton skeleton = new Skeleton(rootNode, meshes, config.excludeMeshNamesFromSkeleton);
        
        processAnimations(objects, scene, skeleton, animResources, names, config);

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



        var indexBytes = indexCount *= Integer.BYTES;

        int drawBytes = meshes.length * Integer.BYTES * 2;

        int vertexBytes = vertexCount * 80;

        int indexOffset = alignUp(vertexBytes, alignment);
        int drawOffset  = indexOffset + alignUp(indexBytes, alignment);
        int totalBytes = drawOffset + drawBytes;

        var vertexBuffer = MemoryUtil.memAlloc(vertexBytes);

        var indexBuffer = MemoryUtil.memAlloc(indexBytes);

        var drawBuffer = MemoryUtil.memAlloc(drawBytes);

        objects.vertex = new SbboOffset(0, vertexBytes);
        objects.index = new SbboOffset(indexOffset, indexBytes);
        objects.draw = new SbboOffset(drawOffset, drawBytes);
        objects.target = new SbboOffset(0, objects.targetVertexStride() * indexCount);

        int[] counters = new int[2]; // index Count

        var list = Arrays.stream(meshes).filter(a -> objects.meshNameToId.containsKey(a.mName().dataString())).sorted(Comparator.comparing(aiMesh -> objects.meshNameToId.get(aiMesh.mName().dataString()))).toList();

        for (int i = 0; i < names.meshes.size(); i++) {
            var mesh = list.get(i);

            objects.meshes[i] = processPrimitiveModel(
                    vertexBuffer,
                    indexBuffer,
                    drawBuffer,
                    counters, skeleton, mesh, config.modelOptions != null ? config.modelOptions : Collections.emptyMap(), dimensions);
        }

        var buffer = MemoryUtil.memAlloc(totalBytes);
        objects.vertex.put(buffer, vertexBuffer.flip());
        objects.index.put(buffer, indexBuffer.flip());
        objects.draw.put(buffer, drawBuffer.flip());

        var bufferId = GL43C.glGenBuffers();
        GL43C.glBindBuffer(GL_SHADER_STORAGE_BUFFER, bufferId);
        glBufferData(GL43C.GL_SHADER_STORAGE_BUFFER, buffer, GL43C.GL_STATIC_READ);
        GL43C.glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
        objects.modelBuffer = bufferId;

        bufferId = GL43.glGenBuffers();
        GL43.glBindBuffer(GL_SHADER_STORAGE_BUFFER, bufferId);
        glBufferData(GL43.GL_SHADER_STORAGE_BUFFER, objects.target.size(), GL43.GL_DYNAMIC_DRAW);
        GL43.glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
        objects.destBuffer = bufferId;

        objects.uvTransformBuffer = new SSBOBuffer(Float.BYTES * 4 * objects.meshes.length);
        objects.instanceBuffer = new SSBOBuffer(InstanceDetails.size);

        MemoryUtil.memFree(buffer);
        MemoryUtil.memFree(vertexBuffer);
        MemoryUtil.memFree(indexBuffer);
        MemoryUtil.memFree(drawBuffer);


        var transform = new Matrix4f();

        traverseTree(transform, rootNode, objects);

        Assimp.aiReleaseImport(scene);

    }

    private static void processAnimations(MultiRenderObject obj, AIScene scene, Skeleton skeleton, Map<String, AnimResource> animResources, Names names, ModelConfig config) {
        extractAssimpAnimations(scene, skeleton, animResources);

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
            fps = config.animationFpsOverride != null && config.animationFpsOverride.containsKey(name) ? config.animationFpsOverride.get(name) : fps;

            var loops = animResource.loops();
            loops = config.animationLoopsOverride != null && config.animationLoopsOverride.containsKey(name) ? config.animationLoopsOverride.get(name) : loops;

            var offsets = animResource.getOffsets();
            offsets.forEach((trackName, offset) -> config.getMaterialsForAnimation(trackName).forEach(a -> offSetsToInsert.put(a, offset)));
            offsets.putAll(offSetsToInsert);
            offSetsToInsert.clear();

            Animation.Offset[] offsetsArray = new Animation.Offset[names.materials.size()];

            offsets.forEach(new BiConsumer<String, Animation.Offset>() {
                @Override
                public void accept(String s, Animation.Offset offset) {
                    var id = names.materials.indexOf(s);
                    offsetsArray[id] = offset;
                }
            });

            var nodes = animResource.getNodes(skeleton);
            var ignoreScaling = config.ignoreScaleInAnimation != null && (config.ignoreScaleInAnimation.contains(name) || config.ignoreScaleInAnimation.contains("all"));

            obj.animations[i] = new Animation(i, (int) fps, loops, skeleton, nodes, offsetsArray, ignoreScaling, config.offsets.getOrDefault(name, new SkeletalTransform()).scale(config.scale));
            obj.animationNameToId.put(name, i);
            obj.animationNames[i] = name;

            i++;
        }

        for (int mesh = 0; mesh < names.meshes.size(); mesh++) {
            var meshName = names.meshes.get(mesh);

            var defaultAnimation = config.hideDuringAnimation.getOrDefault(meshName, ModelConfig.HideDuringAnimation.NONE);

            for (int animation = 0; animation < obj.animations.length; animation++) {
                var animationsName = obj.animationNames[animation];

                obj.hideDuringAnimation[mesh][animation] = defaultAnimation.check(animationsName);
            }
        }
    }

    private static void extractAssimpAnimations(AIScene scene, Skeleton skeleton, Map<String, AnimResource> animResources) {
        for (int i = 0; i < scene.mNumAnimations(); i++) {
            AIAnimation aiAnimation = AIAnimation.create(scene.mAnimations().get(i));
            var animName = aiAnimation.mName().dataString();

            var fps = aiAnimation.mTicksPerSecond();

            var animationNodes = new Animation.AnimationNode[skeleton.jointMap.size()];

            for (int channelIndex = 0; channelIndex < aiAnimation.mNumChannels(); channelIndex++) {
                var channel = AINodeAnim.create(aiAnimation.mChannels().get(channelIndex));

                var boneName = channel.mNodeName().dataString();

                if(!skeleton.boneIdMap.containsKey(boneName)) continue;

                var node = animationNodes[skeleton.boneIdMap.get(boneName)] = new Animation.AnimationNode();


                for (int posIndex = 0; posIndex < channel.mNumPositionKeys(); posIndex++) {
                    var posKey = channel.mPositionKeys().get(posIndex);

                    var time = posKey.mTime();
                    var pos = new Vector3f(posKey.mValue().x(), posKey.mValue().y(), posKey.mValue().z());

                    node.positionKeys.add(time, pos);
                }

                for (int rotIndex = 0; rotIndex < channel.mNumRotationKeys(); rotIndex++) {
                    var rotKey = channel.mRotationKeys().get(rotIndex);

                    var time = rotKey.mTime();
                    var rot = new Quaternionf(rotKey.mValue().x(), rotKey.mValue().y(), rotKey.mValue().z(), rotKey.mValue().w());

                    node.rotationKeys.add(time, rot);
                }

                for (int scaleIndex = 0; scaleIndex < channel.mNumScalingKeys(); scaleIndex++) {
                    var scaleKey = channel.mScalingKeys().get(scaleIndex);

                    var time = scaleKey.mTime();
                    var scale = new Vector3f(scaleKey.mValue().x(), scaleKey.mValue().y(), scaleKey.mValue().z());

                    node.scaleKeys.add(time, scale);
                }
            }

            for (int nodeIndex = 0; nodeIndex < animationNodes.length; nodeIndex++) {

                if(animationNodes[nodeIndex] == null) {
                    var node = new Animation.AnimationNode();
                    var joint = skeleton.jointMap.get(skeleton.bones[nodeIndex].name);

                    node.rotationKeys.add(0, joint.poseRotation);
                    node.rotationKeys.add(0, joint.poseRotation);
                    node.scaleKeys.add(0, joint.poseScale);
                }
            }

            animResources.putIfAbsent(animName, new GenericAnimResource((long) fps, false, animationNodes)); //TODO: Figure out if assimp derived anims can actually loop or I'm dumb. -Waterpicker
        }
    }


    private static void processVariants(MultiRenderObject object, ModelConfig config, Names names, Map<String, List<String>> aliases) {
        var defaultVariant = new int[names.meshes.size()];

        var variantList = new ArrayList<Variant>();

        config.defaultVariant.forEach((k, v) -> {
            var mesh = names.meshes.indexOf(k);

            var variant = addOrGetIndex(variantList, new Variant(names.materials.indexOf(v.material()), v.hide(), v.offset()));

            if(!aliases.isEmpty() && aliases.containsKey(k)) {
                for (String s : aliases.get(k)) {
                    mesh = names.meshes.indexOf(s);

                    defaultVariant[mesh] = variant;
                }
            }
            else defaultVariant[mesh] = variant;

        });

        var variants = object.variantRelationships;

        if(config.variants != null) {
            config.variants.forEach((variantKey, variantParent) -> {
                var variantIndex = names.variants.indexOf(variantKey);

                VariantParent child = config.variants.get(variantParent.inherits());

                var map = variantParent.details();

                while (child != null) {
                    var details = child.details();

                    applyVariantDetails(details, map);

                    child = config.variants.get(child.inherits());
                }

                applyVariantDetails(config.defaultVariant, map);

                applyVariant(variantIndex, variants, map, aliases, names, variantList);
            });
        } else {
            for (int mesh = 0; mesh < defaultVariant.length; mesh++) {
                var variant = defaultVariant[mesh];

                variants[mesh][0] = variant;

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

    private static void applyVariantDetails(Map<String, VariantDetails> applied, Map<String, VariantDetails> appliee) {
        for (Map.Entry<String, VariantDetails> entry : applied.entrySet()) {
            String k = entry.getKey();
            VariantDetails v = entry.getValue();
            appliee.compute(k, (s, variantDetails) -> {
                if(variantDetails == null) return v;
                return variantDetails.fillIn(v);
            });
        }
    }

    private static void applyVariant(
            int variantKey,
            int[][] variantsMap,
            Map<String, VariantDetails> variantMap,
            Map<String, List<String>> aliases,
            Names names,
            List<Variant> variants) {
        variantMap.forEach((k, v) -> {
            var mesh = names.meshes.indexOf(k);

            int mat = names.materials.indexOf(v.material());
            boolean hide = v.hide() != null && v.hide();
            var offset = v.offset() != null ? v.offset() : null;

            var variant = addOrGetIndex(variants, new Variant(mat, hide, offset));

            if(!aliases.isEmpty() && aliases.containsKey(k)) {
                for (String s : aliases.get(k)) {
                    mesh = names.meshes.indexOf(s);
                    variantsMap[mesh][variantKey] = variant;
                }
            }
            else {
                variantsMap[mesh][variantKey] = variant;
            }
        });
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

    private static DrawRecord processPrimitiveModel(
            ByteBuffer vertexBuffer,
            ByteBuffer indexBuffer,
            ByteBuffer drawBuffer,
            int[] counters, Skeleton skeleton, AIMesh mesh, Map<String, MeshOptions> options, Vector3f dimensions) {
        var name = mesh.mName().dataString();

        var faceArray = options.containsKey(name) && options.get(name).invert() ? INVERT_FACE : NORMAL_FACE;

        var amount = mesh.mNumVertices();

        var aiFaces = mesh.mFaces();

        var indexOffset = counters[0];
        var vertexOffset = counters[1];

        var numFaces = mesh.mNumFaces();
        var indexAmount = numFaces * 3;

        drawBuffer.putInt(indexOffset).putInt(indexAmount);

        var drawRecord = new DrawRecord(indexOffset, indexAmount);

        counters[0] += indexAmount;

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

            vertexBuffer.putFloat(position.x());
            vertexBuffer.putFloat(position.y());
            vertexBuffer.putFloat(position.z());
            vertexBuffer.putFloat(0);
            vertexBuffer.putFloat(uv.x());
            vertexBuffer.putFloat(1 - uv.y());
            vertexBuffer.putFloat(0);
            vertexBuffer.putFloat(0);
            vertexBuffer.putFloat(normal.x());
            vertexBuffer.putFloat(normal.y());
            vertexBuffer.putFloat(normal.z());
            vertexBuffer.putFloat(0);

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

    public static short floatToHalf(float fval) {
        int fbits = Float.floatToIntBits(fval);
        int sign = (fbits >>> 16) & 0x8000;
        int val = (fbits & 0x7fffffff) + 0x1000;
        if (val >= 0x47800000) {
            if ((fbits & 0x7fffffff) >= 0x47800000) {
                if (val < 0x7f800000) return (short) (sign | 0x7c00);
                return (short) (sign | 0x7c00 | ((fbits & 0x007fffff) >>> 13));
            }
            return (short) (sign | 0x7bff);
        }
        if (val >= 0x38800000) return (short) (sign | ((val - 0x38000000) >>> 13));
        if (val < 0x33000000) return (short) sign;
        val = (fbits & 0x7fffffff) >>> 23;
        return (short) (sign | ((((fbits & 0x7fffff) | 0x800000) + (0x800000 >>> (val - 102))) >>> (126 - val)));
    }

    public static int calculateVertexSize(List<Attribute> layout) {
        var size = 0;
        for (var attrib : layout) size += calculateAttributeSize(attrib);
        return size;
    }

    public static int calculateAttributeSize(Attribute attrib) {
        return switch (attrib.glType()) {
            case GL_FLOAT, GL_UNSIGNED_INT, GL_INT -> 4;
            case GL_BYTE, GL_UNSIGNED_BYTE -> 1;
            case GL_SHORT, GL_UNSIGNED_SHORT, GL_HALF_FLOAT -> 2;
            default -> throw new IllegalStateException("Unexpected OpenGL Attribute type: " + attrib.glType() + ". If this is wrong, please contact hydos");
        } * attrib.amount();
    }

    private static final List<Attribute> DEFAULT_ATTRIBUTES = List.of(
            Attribute.POSITION,
            Attribute.TEXCOORD,
            Attribute.NORMAL,
            Attribute.BONE_IDS,
            Attribute.BONE_WEIGHTS
    );


    public static void generateVao(GLModel model, ByteBuffer vertexBuffer, List<Attribute> layout) {
        model.vao = glGenVertexArrays();

        glBindVertexArray(model.vao);
        var stride = calculateVertexSize(layout);
        var attribPtr = 0;

        // I hate openGL. why cant I keep the vertex data and vertex layout separate :(
        model.vbo = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER, model.vbo);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        for (int i = 0; i < layout.size(); i++) {
            var attrib = layout.get(i);
            glEnableVertexAttribArray(i);
            glVertexAttribPointer(
                    i,
                    attrib.amount(),
                    attrib.glType(),
                    false,
                    stride,
                    attribPtr
            );
            attribPtr += calculateAttributeSize(attrib);
        }

        glBindVertexArray(0);
    }

    private static void vertexAttribPointer(Attribute data, int binding) {
        GL20.glEnableVertexAttribArray(binding);
        GL20.glVertexAttribPointer(
                binding,
                data.amount(),
                data.glType(),
                false,
                0,
                0);
    }

    public record Names(List<String> meshes, List<String> variants, List<String> images, List<String> materials) {
        public Names() {
            this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }
    }

    public static MultiRenderObject createObject(Function<Names, MultiRenderObject> objBuilder, @NotNull Supplier<PixelAsset> is, BiFunction<MaterialReference, List<String>, Material> materialProcess, Consumer<MultiRenderObject> onFinish) {
        var asset = is.get();
        var config = asset.getConfig();

        if (asset.getModelFile() == null) throw new RuntimeException("model.config not found");

        Map<String, List<String>> aliases = config.aliases != null ? config.aliases : Collections.emptyMap();

        var names = new Names();

        config.defaultVariant.forEach((s, variantDetails) -> {
            if(!aliases.isEmpty() && aliases.containsKey(s)) {
                var meshesToRenderFirst = config.meshesToRenderFirst != null ? config.meshesToRenderFirst.contains(s) : false;

                for (String s2 : aliases.get(s)) {
                    checkIfAlreadyIn(names.meshes(), s2, meshesToRenderFirst);
                }
            }
            else {
                checkIfAlreadyIn(names.meshes(), s, config.meshesToRenderFirst != null ? config.meshesToRenderFirst.contains(s) : false);
            }

            checkIfAlreadyIn(names.materials(), variantDetails.material());
        });

        config.variants.forEach((s, variantParent) -> {
            checkIfAlreadyIn(names.variants(), s);

            if (variantParent.details() != null) {
                variantParent.details().forEach((s1, variantDetails) -> {

                    if(!aliases.isEmpty() && aliases.containsKey(s1)) {
                        for (String s2 : aliases.get(s1)) {
                            checkIfAlreadyIn(names.meshes(), s2);
                        }
                    }
                    else checkIfAlreadyIn(names.meshes(), s1);
                    if(variantDetails.material() != null) checkIfAlreadyIn(names.materials(), variantDetails.material());
                });
            }
        });

        config.materials.forEach((s, reference) -> {
            if (names.materials.contains(s)) {
                reference.complete(config.materials);

                var images = reference.images;

                checkIfAlreadyIn(names.images, images.getDiffuse());
                checkIfAlreadyIn(names.images, images.getLayer());
                checkIfAlreadyIn(names.images, images.getEmission());
                checkIfAlreadyIn(names.images, images.getMask());
            }
        });

        readImages(asset, names.images);
        var obj = objBuilder.apply(names);

        config.materials.forEach((name, reference) -> {
            var id = obj.materialNameToId.getOrDefault(name, -1);

            if(id != -1) {
                var material = materialProcess.apply(reference, names.images);

                obj.materials[id] = material;
            }
        });

        processVariants(obj, config, names, aliases);

        obj.scale = config.scale;

        var aninResouces = new HashMap<String, AnimResource>();

        SmdResource.read(asset, aninResouces);
        GfbanmResource.read(asset, aninResouces);
        TrAnimationResource.read(asset, aninResouces);

        ModelLoader.processModel(obj, names, asset, aninResouces, config);
        obj.updateDimensions();
        if (onFinish != null) onFinish.accept(obj);

        return obj;
    }

//    private <V extends MultiRenderObject> Callable<V> threadedCreateObject(Function<Names, V> objBuilder, @NotNull Supplier<PixelAsset> is, GlCallSupplier<V> objectCreator, Consumer<MultiRenderObject> onFinish) {
//        return () -> {
//            var asset = is.get();
//            var config = asset.getConfig();
//
//            var names = new Names();
//
//            config.defaultVariant.forEach((s, variantDetails) -> {
//                checkIfAlreadyIn(names.meshes(), s, config.meshesToRenderFirst.contains(s));
//                checkIfAlreadyIn(names.materials(), variantDetails.material());
//            });
//
//            config.variants.forEach((s, variantParent) -> {
//                checkIfAlreadyIn(names.variants(), s);
//
//                if (variantParent.details() != null) {
//                    variantParent.details().forEach((s1, variantDetails) -> {
//                        checkIfAlreadyIn(names.meshes(), s1);
//                        checkIfAlreadyIn(names.materials(), variantDetails.material());
//                    });
//                }
//            });
//
//            config.materials.forEach((s, reference) -> {
//                if (names.materials.contains(s)) {
//                    reference.complete(config.materials);
//
//                    var images = reference.images;
//
//                    checkIfAlreadyIn(names.images, images.getDiffuse());
//                    checkIfAlreadyIn(names.images, images.getLayer());
//                    checkIfAlreadyIn(names.images, images.getEmission());
//                    checkIfAlreadyIn(names.images, images.getMask());
//                }
//            });
//
//            var obj = objBuilder.apply(names);
//
//            var images = readImages(asset, names.images);
//
//            var variants = processVariants(config, names);
//
//            if (asset.getModelFile() == null) return;
//
//            if (config != null) obj.scale = config.scale;
//
//            var aninResouces = new HashMap<String, AnimResource>();
//
//            SmdResource.read(asset, aninResouces);
//            GfbanmResource.read(asset, aninResouces);
//            TrAnimationResource.read(asset, aninResouces);
//
//            var meshes = new RenderModel[names.meshes.size()];
//
//
//            var glCalls = objectCreator.getCalls(asset, aninResouces, images, variants, names, config, obj);
//            ThreadSafety.runOnContextThread(() -> {
//                glCalls.forEach(Runnable::run);
//                obj.updateDimensions();
//                if (onFinish != null) onFinish.accept(obj);
//            });
//
//            return obj;
//
//        }
//    }

    private static void checkIfAlreadyIn(List<String> list, String entry) {
        checkIfAlreadyIn(list, entry, false);
    }

    private static void checkIfAlreadyIn(List<String> list, String entry, boolean addFirst) {
        if(!list.contains(entry)) if(addFirst) list.addFirst(entry); else list.add(entry);
    }


    public static void readImages(PixelAsset asset, List<String> imageNames) {
        var images = asset.getImageFiles();

        for (var entry : images) {
            var key = entry.getKey();

            var index = imageNames.contains(key);

            if(!index) continue;

            try {
                ITextureLoader.instance().register(entry.getKey(), Texture.read(entry.getValue(), entry.getKey()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void close() {
//        modelLoadingPool.shutdown();
    }

    public static AIScene read(PixelAsset asset) {
        var name = asset.modelName;

        var fileIo = AIFileIO.create()
                .OpenProc((pFileIO, pFileName, openMode) -> {
                    var fileName = MemoryUtil.memUTF8(pFileName);
                    var bytes = asset.get(fileName);
                    var data = BufferUtils.createByteBuffer(bytes.length);
                    data.put(bytes);
                    data.flip();

                    return AIFile.create()
                            .ReadProc((pFile, pBuffer, size, count) -> {
                                var max = Math.min(data.remaining() / size, count);
                                MemoryUtil.memCopy(MemoryUtil.memAddress(data), pBuffer, max * size);
                                data.position((int) (data.position() + max * size));
                                return max;
                            })
                            .SeekProc((pFile, offset, origin) -> {
                                switch (origin) {
                                    case Assimp.aiOrigin_CUR -> data.position(data.position() + (int) offset);
                                    case Assimp.aiOrigin_SET -> data.position((int) offset);
                                    case Assimp.aiOrigin_END -> data.position(data.limit() + (int) offset);
                                }

                                return 0;
                            })
                            .FileSizeProc(pFile -> data.limit())
                            .address();
                })
                .CloseProc((pFileIO, pFile) -> {
                    var aiFile = AIFile.create(pFile);
                    aiFile.ReadProc().free();
                    aiFile.SeekProc().free();
                    aiFile.FileSizeProc().free();
                });

        var scene = Assimp.aiImportFileEx(name,
                Assimp.aiProcess_Triangulate |
                        Assimp.aiProcess_OptimizeMeshes |
                        Assimp.aiProcess_ImproveCacheLocality, fileIo);

        if (scene == null) throw new RuntimeException(Assimp.aiGetErrorString());

        return scene;
    }

    public static <T> Map<String, Map<String, T>> reverseMap(Map<String, Map<String, T>> inputMap) {
        Map<String, Map<String, T>> reversedMap = new HashMap<>();

        for (Map.Entry<String, Map<String, T>> outerEntry : inputMap.entrySet()) {
            String outerKey = outerEntry.getKey();
            Map<String, T> innerMap = outerEntry.getValue();

            for (Map.Entry<String, T> innerEntry : innerMap.entrySet()) {
                String innerKey = innerEntry.getKey();
                T value = innerEntry.getValue();

                reversedMap.computeIfAbsent(innerKey, k -> new HashMap<>()).put(outerKey, value);
            }
        }

        return reversedMap;
    }

    public static Map<String, List<String>> reverseListMap(Map<String, List<String>> inputMap) {
        Map<String, List<String>> reversedMap = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : inputMap.entrySet()) {
            String outerKey = entry.getKey();
            List<String> innerList = entry.getValue();

            for (String innerKey : innerList) {
                reversedMap.computeIfAbsent(innerKey, k -> new ArrayList<>()).add(outerKey);
            }
        }

        return reversedMap;
    }

    public static Matrix4f from(Matrix4f transform, AIMatrix4x4 aiMat4) {
        return transform.set(aiMat4.a1(), aiMat4.a2(), aiMat4.a3(), aiMat4.a4(),
                aiMat4.b1(), aiMat4.b2(), aiMat4.b3(), aiMat4.b4(),
                aiMat4.c1(), aiMat4.c2(), aiMat4.c3(), aiMat4.c4(),
                aiMat4.d1(), aiMat4.d2(), aiMat4.d3(), aiMat4.d4());
    }
}