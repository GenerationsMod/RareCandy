package gg.generations.rarecandy.renderer.loading;

import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.MeshPrimitiveModel;
import dev.thecodewarrior.binarysmd.formats.SMDTextReader;
import dev.thecodewarrior.binarysmd.studiomdl.SMDFile;
import gg.generations.rarecandy.pokeutils.*;
import gg.generations.rarecandy.pokeutils.gfbanm.AnimationT;
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader;
import gg.generations.rarecandy.pokeutils.tracm.TRACM;
import gg.generations.rarecandy.pokeutils.tranm.TRANMT;
import gg.generations.rarecandy.renderer.ThreadSafety;
import gg.generations.rarecandy.renderer.animation.Animation;
import gg.generations.rarecandy.renderer.animation.Skeleton;
import gg.generations.rarecandy.renderer.components.AnimatedMeshObject;
import gg.generations.rarecandy.renderer.components.MeshObject;
import gg.generations.rarecandy.renderer.components.MultiRenderObject;
import gg.generations.rarecandy.renderer.components.RenderObject;
import gg.generations.rarecandy.renderer.model.GLModel;
import gg.generations.rarecandy.renderer.model.GlCallSupplier;
import gg.generations.rarecandy.renderer.model.MeshDrawCommand;
import gg.generations.rarecandy.renderer.model.Variant;
import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.rendering.Bone;
import gg.generations.rarecandy.renderer.rendering.RareCandy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.assimp.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;
import static org.lwjgl.opengl.GL30C.*;

public class ModelLoader {
    private final ExecutorService modelLoadingPool;

    private static Vector3f temp = new Vector3f();

    public ModelLoader() {
        this(4);
    }

    public ModelLoader(int numThreads) {
        this.modelLoadingPool = Executors.newFixedThreadPool(numThreads);
    }

    public interface NodeProvider {
        Animation.AnimationNode[] getNode(Animation animation, Skeleton skeleton);
    }


    public static List<Attribute> ATTRIBUTES = List.of(
            Attribute.POSITION,
            Attribute.TEXCOORD,
            Attribute.NORMAL,
            Attribute.BONE_IDS,
            Attribute.BONE_WEIGHTS
    );

    public static <T extends MeshObject> void create2(MultiRenderObject<T> objects, PixelAsset asset, Map<String, SMDFile> smdFileMap, Map<String, byte[]> gfbFileMap, Map<String, Pair<byte[], byte[]>> trFilesMap, Map<String, String> images, ModelConfig config, List<Runnable> glCalls, Supplier<T> supplier) {
        if (config == null) throw new RuntimeException("config.json can't be null.");

        Map<String, NodeProvider> animationNodeMap = new HashMap<>();
        Map<String, Integer> fpsMap = new HashMap<>();
        Map<String, Map<String, Animation.Offset>> offsetsMap = new HashMap<>();

        Set<String> animationNames = new HashSet<>();

        var gltfModel = ModelLoader.read(asset);

        var rootNode = ModelNode.create(gltfModel.mRootNode());

        Skeleton skeleton = new Skeleton(rootNode);

        for (var entry : trFilesMap.entrySet()) {
            var name = entry.getKey();
            var tranm = entry.getValue().a() != null ? TRANMT.deserializeFromBinary(entry.getValue().a()) : null;
            var tracm = entry.getValue().b() != null ? TRACM.getRootAsTRACM(ByteBuffer.wrap(entry.getValue().b())) : null;

            if(tranm != null || tracm != null) {
                animationNames.add(name);
                if (tranm != null) {
                    animationNodeMap.putIfAbsent(name, (animation, skeleton1) -> TranmUtils.getNodes(animation, skeleton1, tranm));
                    fpsMap.putIfAbsent(name, (int) tranm.getInfo().getAnimationRate());
                }

                if (tracm != null) {
                    offsetsMap.putIfAbsent(name, TracmUtils.getOffsets(tracm));
                    fpsMap.putIfAbsent(name, (int) tracm.config().framerate());
                }
            }
        }

        for (var entry : gfbFileMap.entrySet()) {
            var name = entry.getKey();

            try {
                var gfbAnim = AnimationT.deserializeFromBinary(entry.getValue());

                if(gfbAnim.getMaterial() != null || gfbAnim.getSkeleton() != null) {
                    animationNames.add(name);
                    fpsMap.put(name, (int) gfbAnim.getInfo().getFrameRate());

                    if(gfbAnim.getSkeleton() != null) {
                        animationNodeMap.put(name, (animation, skeleton13) -> GfbanmUtils.getNodes(animation, skeleton13, gfbAnim));
                    }

                    if(gfbAnim.getMaterial() != null) {
                        offsetsMap.put(name, GfbanmUtils.getOffsets(gfbAnim));
                    }
                }
            } catch (Exception e) {
                System.out.println("Failed to load animation %s due to the following exception: %s".formatted(name, e.getMessage()));
                e.printStackTrace();
            }
        }

        for (var entry : smdFileMap.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();

            if(value != null) {
                animationNames.add(key);
                animationNodeMap.putIfAbsent(key, (NodeProvider) (animation, skeleton12) -> SmdUtils.getNode(animation, skeleton12, value));
                fpsMap.putIfAbsent(key, 30);
            }
        }

        Map<String, Animation> animations = new HashMap<>();

        for(var name : animationNames) {
            var fps = fpsMap.get(name);
            fps = config.animationFpsOverride != null && config.animationFpsOverride.containsKey(name) ? config.animationFpsOverride.get(name) : fps;

            var offsets = offsetsMap.getOrDefault(name, new HashMap<>());
            offsets.forEach((trackName, offset) -> config.getMaterialsForAnimation(trackName).forEach(a -> offsets.put(a, offset)));

            var nodes = animationNodeMap.getOrDefault(name, (animation, skeleton14) -> null);
            var ignoreScaling = config.ignoreScaleInAnimation != null && (config.ignoreScaleInAnimation.contains(name) || config.ignoreScaleInAnimation.contains("all"));

            animations.put(name, new Animation(name, fps, skeleton, nodes, offsets, ignoreScaling));
        }

        Map<String, ModelConfig.HideDuringAnimation> hideDuringAnimation = new HashMap<>();

        var materials = new HashMap<String, Material>();

        config.materials.forEach((k, v) -> {
            var material = MaterialReference.process(k, config.materials, images);

            materials.put(k, material);
        });

        var defaultVariant = new HashMap<String, Variant>();
        config.defaultVariant.forEach((k, v) -> {
            var variant = new Variant(materials.get(v.material()), v.hide(), v.offset());
            defaultVariant.put(k, variant);
        });

        var variantMaterialMap = new HashMap<String, Map<String, Material>>();
        var variantHideMap = new HashMap<String, List<String>>();
        var variantOffsetMap = new HashMap<String, Map<String, Vector2f>>();


        if(config.hideDuringAnimation != null) {
            hideDuringAnimation = config.hideDuringAnimation;
        }

        if(config.variants != null) {
            for (Map.Entry<String, VariantParent> entry : config.variants.entrySet()) {
                String variantKey = entry.getKey();
                VariantParent variantParent = entry.getValue();

                VariantParent child = config.variants.get(variantParent.inherits());

                var map = variantParent.details();

                while (child != null) {
                     var details = child.details();

                     applyVariantDetails(details, map);

                    child = config.variants.get(child.inherits());
                }

                applyVariantDetails(config.defaultVariant, map);

                var matMap = variantMaterialMap.computeIfAbsent(variantKey, s3 -> new HashMap<>());
                var hideMap = variantHideMap.computeIfAbsent(variantKey, s3 -> new ArrayList<>());
                var offsetMap = variantOffsetMap.computeIfAbsent(variantKey, s3 -> new HashMap<>());


                applyVariant(materials, matMap, hideMap, offsetMap, map);
            }
        } else {
            var matMap = variantMaterialMap.computeIfAbsent("regular", s3 -> new HashMap<>());
            var hideMap = variantHideMap.computeIfAbsent("regular", s3 -> new ArrayList<>());
            var offsetMap = variantOffsetMap.computeIfAbsent("regular", s3 -> new HashMap<>());


            defaultVariant.forEach((s1, variant) -> {
                matMap.put(s1, variant.material());
                if (variant.hide()) hideMap.add(s1);
                if (variant.offset() != null) offsetMap.put(s1, variant.offset());
            });
        }

        var matMap = reverseMap(variantMaterialMap);
        var hidMap = reverseListMap(variantHideMap);
        var offsetMap = reverseMap(variantOffsetMap);

        var meshes = IntStream.range(0, gltfModel.mNumMeshes()).mapToObj(i -> AIMesh.create(gltfModel.mMeshes().get(i))).toArray(AIMesh[]::new);

        for (AIMesh aiMesh : meshes) {
            processBones(skeleton, aiMesh);
        }

        skeleton.calculateBoneData();

        for (var mesh : meshes) {
            processPrimitiveModels(objects, supplier, mesh, matMap, hidMap, offsetMap, glCalls, skeleton, animations, hideDuringAnimation, config.modelOptions != null ? config.modelOptions : Collections.emptyMap());
        }

        var transform = new Matrix4f();

        traverseTree(transform, rootNode, objects);

        Assimp.aiReleaseImport(gltfModel);

    }

    private static void processBones(Skeleton skeleton, AIMesh mesh) {

        if (mesh.mBones() != null) {
            var aiBones = requireNonNull(mesh.mBones());
            Bone[] bones = new Bone[aiBones.capacity()];

            for (int i = 0; i < aiBones.capacity(); i++) {
                bones[i] = Bone.from(AIBone.create(aiBones.get(i)));
            }

            skeleton.store(bones);
        }
    }

    private static <T extends MeshObject> void traverseTree(Matrix4f transform, ModelNode node, MultiRenderObject<T> objects) {
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

    private static void applyVariant(Map<String, Material> materials, Map<String, Material> matMap, List<String> hideMap, Map<String, Vector2f> offsetMap, Map<String, VariantDetails> variantMap) {
        variantMap.forEach((k, v) -> {
            matMap.put(k, materials.get(v.material()));
            if (v.hide() != null && v.hide()) hideMap.add(k);
            if (v.offset() != null) offsetMap.put(k, v.offset());
        });
    }

    private static void applyTransforms(Matrix4f transform, ModelNode node) {
        transform.set(node.transform);
    }

    private static <T extends MeshObject> void processPrimitiveModels(MultiRenderObject<T> objects, Supplier<T> objSupplier, AIMesh mesh, Map<String, Map<String, Material>> materialMap, Map<String, List<String>> hiddenMap, Map<String, Map<String, Vector2f>> offsetMap, List<Runnable> glCalls, @Nullable Skeleton skeleton, @Nullable Map<String, Animation> animations, Map<String, ModelConfig.HideDuringAnimation> hideDuringAnimations, Map<String, MeshOptions> meshOptions) {
        var name = mesh.mName().dataString();

        var map = materialMap.get(name);
        var list = hiddenMap.get(name);
        var offset = offsetMap.get(name);

        var renderObject = objSupplier.get();
        var glModel = processPrimitiveModel(skeleton, mesh, meshOptions, glCalls, objects.dimensions);

        if (animations != null && renderObject instanceof AnimatedMeshObject animatedMeshObject) {
            animatedMeshObject.setup(map, list, offset, glModel, name, skeleton, animations, hideDuringAnimations.getOrDefault(name, ModelConfig.HideDuringAnimation.NONE));
        } else {
            renderObject.setup(map, list, offset, glModel, name);
        }

        objects.add(renderObject);
    }

    private static GLModel processPrimitiveModel(Skeleton skeleton, AIMesh mesh, Map<String, MeshOptions> options, List<Runnable> glCalls, Vector3f dimensions) {
        var name = mesh.mName().dataString();

        var invertFace = options.containsKey(name) && options.get(name).invert();

        var model = new GLModel();

        var length = calculateVertexSize(ATTRIBUTES);
        var amount = mesh.mNumVertices();

        var vertexBuffer = MemoryUtil.memAlloc(length * amount);

        var aiFaces = mesh.mFaces();
        var indexBuffer = MemoryUtil.memAlloc(mesh.mNumFaces() * Integer.BYTES * 3);

        for (int j = 0; j < mesh.mNumFaces(); j++) {
            var aiFace = aiFaces.get(j).mIndices();
            indexBuffer.putInt(aiFace.get(invertFace ? 2 : 0)).putInt(aiFace.get(1)).putInt(aiFace.get(invertFace ? 0 : 2));
        }
        indexBuffer.flip();

        var aiVert = mesh.mVertices();
        var aiUV = mesh.mTextureCoords(0);

        if (aiUV == null) {
            throw new RuntimeException("Error UV coordinates not found!");
        }

        var aiNormals = mesh.mNormals();

        if (aiNormals == null) {
            throw new RuntimeException("Error Normals not found!");
        }

        byte[] ids = new byte[amount * 4];
        float[] weights = new float[amount * 4];

        if (mesh.mBones() != null) {
            var aiBones = requireNonNull(mesh.mBones());

            for (byte boneIndex = 0; boneIndex < aiBones.capacity(); boneIndex++) {
                var aiBone = AIBone.create(aiBones.get(boneIndex));

                var weight = aiBone.mWeights();

                var index = skeleton.getId(aiBone.mName().dataString());

                for (int weightId = 0; weightId < weight.capacity(); weightId++) {
                    var aiWeight = weight.get(weightId);
                    var vertexId = aiWeight.mVertexId();

                    if(aiWeight.mWeight() > 0f) {
                        addBoneData(ids, weights, vertexId, (byte) index, aiWeight.mWeight());
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
            vertexBuffer.putFloat(uv.x());
            vertexBuffer.putFloat(1 - uv.y());
            vertexBuffer.putFloat(normal.x());
            vertexBuffer.putFloat(normal.y());
            vertexBuffer.putFloat(normal.z());

            if(isEmpty) {
                vertexBuffer.put((byte) 1);
                vertexBuffer.put((byte) 0);
                vertexBuffer.put((byte) 0);
                vertexBuffer.put((byte) 0);

                vertexBuffer.putFloat(1);
                vertexBuffer.putFloat(0);
                vertexBuffer.putFloat(0);
                vertexBuffer.putFloat(0);
            } else {
                vertexBuffer.put(ids[i * 4 + 0]);
                vertexBuffer.put(ids[i * 4 + 1]);
                vertexBuffer.put(ids[i * 4 + 2]);
                vertexBuffer.put(ids[i * 4 + 3]);

                vertexBuffer.putFloat(weights[i * 4 + 0]);
                vertexBuffer.putFloat(weights[i * 4 + 1]);
                vertexBuffer.putFloat(weights[i * 4 + 2]);
                vertexBuffer.putFloat(weights[i * 4 + 3]);
            }

            dimensions.max(temp.set(position.x(), position.y(), position.z()));
        }

        vertexBuffer.flip();


        var indexSize = mesh.mNumFaces() * 3;

        glCalls.add(() -> {
            generateVao(model, vertexBuffer, ATTRIBUTES);
            GL30.glBindVertexArray(model.vao);
            model.ebo = GL15.glGenBuffers();
            glBindBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, model.ebo);
            glBufferData(GL15C.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_STATIC_DRAW);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
            model.meshDrawCommands.add(new MeshDrawCommand(model.vao, GL11.GL_TRIANGLES, GL_UNSIGNED_INT, model.ebo, indexSize));
            MemoryUtil.memFree(vertexBuffer);
            MemoryUtil.memFree(indexBuffer);
        });

        return model;
    }

    public static void addBoneData(byte[] ids, float[] weights, int vertexId, byte boneId, float weight) {
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

    private static List<Attribute> DEFAULT_ATTRIBUTES = List.of(
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

    public static List<String> getVariants(GltfModel model) {
        try {
            if (model.getExtensions() == null || model.getExtensions().isEmpty() || !model.getExtensions().containsKey("KHR_materials_variants"))
                return null;

            var variantMap = (Map<String, Object>) model.getExtensions().get("KHR_materials_variants");
            var variantList = (List<Map<String, String>>) variantMap.get("variants");
            var variantNames = new ArrayList<String>();

            for (Map<String, String> a : variantList) {
                var name = a.get("name");
                variantNames.add(name);
            }
            return variantNames;
        } catch (Exception e) {
            throw new RuntimeException("Malformed Variant List in GLTF model.", e);
        }
    }

    public static <T> Map<String, T> createMeshVariantMap(MeshPrimitiveModel primitiveModel, List<T> materials, List<String> variantsList) {
        if (variantsList == null) {
            var materialId = primitiveModel.getMaterialModel().getName();
            return Collections.singletonMap("default", materials.stream().filter(a -> a.toString().equals(materialId)).findAny().get());
        } else {
            var map = (Map<String, Object>) primitiveModel.getExtensions().get("KHR_materials_variants");
            var mappings = (List<Map<String, Object>>) map.get("mappings");
            var variantMap = new HashMap<String, T>();

            for (var mapping : mappings) {
                if (!mapping.containsKey("material")) continue;
                var material = materials.get((Integer) mapping.get("material"));
                var variants = (List<Integer>) mapping.get("variants");

                for (var i : variants) {
                    var variant = variantsList.get(i);
                    variantMap.put(variant, material);
                }
            }

            return variantMap;
        }
    }

    public <T extends RenderObject> MultiRenderObject<T> createObject(@NotNull Supplier<PixelAsset> is, GlCallSupplier<T> objectCreator, Consumer<MultiRenderObject<T>> onFinish) {
        var obj = new MultiRenderObject<T>();
        var task = threadedCreateObject(obj, is, objectCreator, onFinish);
        if (RareCandy.DEBUG_THREADS) task.run();
        else modelLoadingPool.submit(task);
        return obj;
    }

    public MultiRenderObject<MeshObject> generatePlane(float width, float length, Consumer<MultiRenderObject<MeshObject>> onFinish) {
        var pair = PlaneGenerator.generatePlane(width, length);

        var task = ThreadSafety.wrapException(() -> {
            ThreadSafety.runOnContextThread(() -> {
                pair.a().forEach(Runnable::run);
                pair.b().updateDimensions();
                if (onFinish != null) onFinish.accept(pair.b());
            });
        });
        if (RareCandy.DEBUG_THREADS) task.run();
        else modelLoadingPool.submit(task);

        return pair.b();
    }

    private <T extends RenderObject> Runnable threadedCreateObject(MultiRenderObject<T> obj, @NotNull Supplier<PixelAsset> is, GlCallSupplier<T> objectCreator, Consumer<MultiRenderObject<T>> onFinish) {
        return ThreadSafety.wrapException(() -> {
            var asset = is.get();
            var config = asset.getConfig();

            var images = readImages(asset);

            if(asset.getModelFile() == null) return;

            if (config != null) obj.scale = config.scale;
            var smdAnims = readSmdAnimations(asset);
            var gfbAnims = readGfbAnimations(asset);
            var trAnims = readtrAnimations(asset);
            var glCalls = objectCreator.getCalls(asset, smdAnims, gfbAnims, trAnims, images, config, obj);
            ThreadSafety.runOnContextThread(() -> {
                glCalls.forEach(Runnable::run);
                obj.updateDimensions();
                if (onFinish != null) onFinish.accept(obj);
            });
        });
    }

    private Map<String, String> readImages(PixelAsset asset) {
        var images = asset.getImageFiles();
        var map = new HashMap<String, String>();
        for (var entry : images) {
            var key = entry.getKey();

            var id = asset.name + "-" + key;
            ITextureLoader.instance().register(id, key, entry.getValue());

            map.put(key, id);
        }

        return map;
    }

    private Map<String, byte[]> readGfbAnimations(PixelAsset asset) {
        return asset.files.entrySet().stream()
                .filter(entry -> entry.getKey().endsWith(".pkx") || entry.getKey().endsWith(".gfbanm"))
                .collect(Collectors.toMap(this::cleanAnimName, Map.Entry::getValue));
    }

    private HashMap<String, Pair<byte[], byte[]>> readtrAnimations(PixelAsset asset) {
        var map = new HashMap<String, Pair<byte[], byte[]>>();

        var list = asset.files.keySet().stream().filter(a -> a.endsWith("tranm") || a.endsWith("tracm")).collect(Collectors.toCollection(ArrayList::new));

        while (!list.isEmpty()) {
            var a = list.remove(0);

            if (a.endsWith(".tranm")) {
                var index = list.indexOf(a.replace(".tranm", ".tracm"));

                if (index != -1) {
                    var b = list.remove(index);

                    map.put(a.replace(".tranm", ""), new Pair<>(asset.files.get(a), asset.files.get(b)));
                } else {
                    map.put(a.replace(".tranm", ""), new Pair<>(asset.files.get(a), null));
                }
            } else {
                if (a.endsWith(".tracm")) {
                    var index = list.indexOf(a.replace(".tracm", ".tranm"));

                    if (index != -1) {
                        var b = list.remove(index);

                        map.put(a.replace(".tracm", ""), new Pair<>(asset.files.get(b), asset.files.get(a)));
                    } else {
                        map.put(a.replace(".tracm", ""), new Pair<>(null, asset.files.get(a)));
                    }
                }
            }

        }

        return map;
    }


    public String cleanAnimName(Map.Entry<String, byte[]> entry) {
        var str = entry.getKey();
        return cleanAnimName(str);
    }

    public String cleanAnimName(String str) {
        var substringEnd = str.lastIndexOf(".") == -1 ? str.length() : str.lastIndexOf(".");
        var substringStart = str.lastIndexOf("/") == -1 ? 0 : str.lastIndexOf("/");
        return str.substring(substringStart, substringEnd);
    }

    private Map<String, SMDFile> readSmdAnimations(PixelAsset pixelAsset) {
        var files = pixelAsset.getAnimationFiles();
        var map = new HashMap<String, SMDFile>();
        var reader = new SMDTextReader();

        for (var entry : files) {
            var smdFile = reader.read(new String(entry.getValue()));
            map.put(cleanAnimName(entry.getKey().replace(".smd", "")), smdFile);
        }

        return map;
    }

    public void close() {
        modelLoadingPool.shutdown();
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

        var scene = Assimp.aiImportFileEx(name, Assimp.aiProcess_Triangulate | Assimp.aiProcess_ImproveCacheLocality, fileIo);

        if (scene == null) throw new RuntimeException(Assimp.aiGetErrorString());

        return scene;
//            return reader.readWithoutReferences(new ByteArrayInputStream(asset.getModelFile()));
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