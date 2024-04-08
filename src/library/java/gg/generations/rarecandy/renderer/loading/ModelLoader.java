package gg.generations.rarecandy.renderer.loading;

import de.javagl.jgltf.model.*;
import dev.thecodewarrior.binarysmd.formats.SMDTextReader;
import dev.thecodewarrior.binarysmd.studiomdl.SMDFile;
import gg.generations.rarecandy.pokeutils.*;
import gg.generations.rarecandy.pokeutils.gfbanm.AnimationT;
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader;
import gg.generations.rarecandy.pokeutils.reader.TextureReference;
import gg.generations.rarecandy.pokeutils.tracm.TRACM;
import gg.generations.rarecandy.pokeutils.tranm.TRANMT;
import gg.generations.rarecandy.renderer.ThreadSafety;
import gg.generations.rarecandy.renderer.animation.*;
import gg.generations.rarecandy.renderer.components.AnimatedMeshObject;
import gg.generations.rarecandy.renderer.components.MeshObject;
import gg.generations.rarecandy.renderer.components.MultiRenderObject;
import gg.generations.rarecandy.renderer.components.RenderObject;
import gg.generations.rarecandy.renderer.model.GLModel;
import gg.generations.rarecandy.renderer.model.GlCallSupplier;
import gg.generations.rarecandy.renderer.model.Variant;
import gg.generations.rarecandy.renderer.model.material.Material;
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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL30C.*;

public class ModelLoader {
    private final ExecutorService modelLoadingPool;

    public ModelLoader() {
        this.modelLoadingPool = Executors.newFixedThreadPool(2);
    }

    public interface NodeProvider {
        Animation.AnimationNode[] getNode(Animation animation, Skeleton skeleton);
    }

    public static <T extends MeshObject> void create2(PixelAsset gltfModel, Map<String, SMDFile> smdFileMap, Map<String, byte[]> gfbFileMap, Map<String, Pair<byte[], byte[]>> trFilesMap, Map<String, String> images, ModelConfig config, List<Runnable> glCalls, Supplier<T> supplier) {
        return create2(gltfModel, smdFileMap, gfbFileMap, trFilesMap, images, config, glCalls, supplier, Animation.GLB_SPEED);
    }

    private static List<Attribute> ATTRIBUTES = List.of(
            Attribute.POSITION,
            Attribute.TEXCOORD,
            Attribute.NORMAL,
            Attribute.BONE_IDS,
            Attribute.BONE_WEIGHTS
    );

    public record AnimationData(
            Map<String, NodeProvider> animationNodeMap,
    Map<String, Integer> fpsMap,
    Map<String, Map<String, Animation.Offset>> offsetsMap,

    Set<String> animationNames
    ) {
        public Map<String, Animation> process(Skeleton skeleton, ModelConfig config) {
            var animations = new HashMap<String, Animation>();
            for (var name : animationNames) {
                var fps = fpsMap.get(name);
                fps = config.animationFpsOverride != null && config.animationFpsOverride.containsKey(name) ? config.animationFpsOverride.get(name) : fps;

                var offsets = offsetsMap.getOrDefault(name, new HashMap<>());
                offsets.forEach((trackName, offset) -> config.getMaterialsForAnimation(trackName).forEach(a -> offsets.put(a, offset)));

                var nodes = animationNodeMap.getOrDefault(name, (animation, skeleton14) -> null);
                var ignoreScaling = config.ignoreScaleInAnimation != null && (config.ignoreScaleInAnimation.contains(name) || config.ignoreScaleInAnimation.contains("all"));

                animations.put(name, new Animation(name, fps, skeleton, nodes, offsets, ignoreScaling));
            }

            return animations;
        }
    }

    public static <T extends MeshObject> CompletableFuture<MultiRenderObject<T>> create2(PixelAsset asset, CompletableFuture<Map<String, SMDFile>> smdFileMap, CompletableFuture<Map<String, byte[]>> tracmMap, CompletableFuture<Map<String, byte[]>> tranmMap, CompletableFuture<Map<String, byte[]>> gfbanmMap, CompletableFuture<Map<String, String>> images, ModelConfig config, List<Runnable> glCalls, Supplier<T> supplier, int animationSpeed) {
        if (config == null) throw new RuntimeException("config.json can't be null.");

        var data = new AnimationData(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashSet<>());

        var gltfModel = ModelLoader.read(asset);

        var rootNode = ModelNode.create(gltfModel.mRootNode());

        Skeleton skeleton = new Skeleton(rootNode);


        var animations = CompletableFuture.allOf(
                tracmMap.thenAccept(trFilesMap -> {
                    for (var entry : trFilesMap.entrySet()) {
                        var name = entry.getKey();
                        var tracm = TRACM.getRootAsTRACM(ByteBuffer.wrap(entry.getValue()));
                        TracmUtils.process(data, name, tracm);
                    }
                }),
                tranmMap.thenAccept(trFilesMap -> {
                    for (var entry : trFilesMap.entrySet()) {
                        var name = entry.getKey();
                        var tranm = TRANMT.deserializeFromBinary(entry.getValue());
                        TranmUtils.process(data, name, tranm);
                    }
                }),
                gfbanmMap.thenAccept(trFilesMap -> {
                    for (var entry : trFilesMap.entrySet()) {
                        var name = entry.getKey();
                        var gfbAnim = AnimationT.deserializeFromBinary(entry.getValue());
                        GfbanmUtils.process(data, name, gfbAnim);
                    }
                }),
                smdFileMap.thenAccept(trFilesMap -> {
                    for (var entry : trFilesMap.entrySet()) {
                        var name = entry.getKey();
                        SmdUtils.process(data, name, entry.getValue());
                    }
                })).thenApplyAsync(ignored -> data.process(skeleton, config));

        var variants = images.thenApplyAsync(map -> createVariants(config, map));

//        objects.dimensions.set(calculateDimensions(meshes));

        var meshes = Mesh.readMeshData(skeleton, gltfModel);
        Stream.of(meshes).flatMap(a -> a.positions().stream()).forEach(objects.dimensions::max);
        for (var meshModel : meshes) {
            processPrimitiveModels(objects, supplier, meshModel, matMap, hidMap, offsetMap, glCalls, skeleton, animations, config.hideDuringAnimation != null ? config.hideDuringAnimation : new HashMap<String, ModelConfig.HideDuringAnimation>());
        }


        var transform = new Matrix4f();

        traverseTree(transform, rootNode, objects);

        Assimp.aiReleaseImport(gltfModel);

    }

    private static Map<String, Map<String, Variant>> createVariants(ModelConfig config, Map<String, String> images) {
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

        var variants = new HashMap<String, Map<String, Variant>>();

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

                var variantSet = variants.computeIfAbsent(variantKey, s -> new HashMap<>());

                map.forEach((k, v) -> variantSet.put(k, v.process(materials)));
            }
        } else {
            variants.computeIfAbsent("regular", s3 -> new HashMap<>()).putAll(defaultVariant);
        }

//        var matMap = reverseMap(variantMaterialMap);
//        var hidMap = reverseListMap(variantHideMap);
//        var offsetMap = reverseMap(variantOffsetMap);


        return reverseMap(variants);
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

    private static void applyVariant(Map<String, Material> materials, Map<String, Variant> variants, Map<String, VariantDetails> variantMap) {
        variantMap.forEach((k, v) -> variants.put(k, v.process(materials)));
    }

    public static Vector3f calculateDimensions(Mesh[] meshes) {
        var vec = new Vector3f();
        var pos = new Vector3f();

        for(var mesh : meshes) {
            var buf = mesh.positions();
            var smallestVertexX = 0f;
                var smallestVertexY = 0f;
                var smallestVertexZ = 0f;
                var largestVertexX = 0f;
                var largestVertexY = 0f;
                var largestVertexZ = 0f;
                for (int i = 0; i < buf.size(); i += 3) { // Start at the y entry of every vertex and increment by 12 because there are 12 bytes per vertex
                    var xPoint = buf.get(i).x();
                    var yPoint = buf.get(i).y();
                    var zPoint = buf.get(i).z();
                    smallestVertexX = Math.min(smallestVertexX, xPoint);
                    smallestVertexY = Math.min(smallestVertexY, yPoint);
                    smallestVertexZ = Math.min(smallestVertexZ, zPoint);
                    largestVertexX = Math.max(largestVertexX, xPoint);
                    largestVertexY = Math.max(largestVertexY, yPoint);
                    largestVertexZ = Math.max(largestVertexZ, zPoint);
                }

                vec.set(largestVertexX - smallestVertexX, largestVertexY - smallestVertexY, largestVertexZ - smallestVertexZ);

                pos.max(vec);

        }

        return pos;
    }

    private static void applyTransforms(Matrix4f transform, ModelNode node) {
        transform.set(node.transform);

//        if (node.getScale() != null) transform.scale(new Vector3f(node.getScale()));
//        if (node.getRotation() != null)
//            transform.rotate(new Quaternionf(node.getRotation()[0], node.getRotation()[1], node.getRotation()[2], node.getRotation()[3]));
//        if (node.getTranslation() != null) {
//            if (node.getTranslation().length == 3)
//                transform.add(new Matrix4f().setTranslation(node.getTranslation()[0], node.getTranslation()[1], node.getTranslation()[2]));
//            else
//                transform.add(new Matrix4f().set(node.getTranslation()));
//        }
    }

//    public static <T extends MeshObject> void checkForRootTransformation(MultiRenderObject<T> objects, AIScene gltfModel) {
//        if (gltfModel.getSkinModels().isEmpty()) {
//            var node = gltfModel.getNodeModels().get(0);
//            while (node.getParent() != null) node = node.getParent();
//            var rootTransformation = new Matrix4f().set(node.createGlobalTransformSupplier().get());
//            objects.setRootTransformation(rootTransformation);
//        }
//    }

    private static <T extends MeshObject> void processPrimitiveModels(
            MultiRenderObject<T> objects,
            Supplier<T> objSupplier,
            Mesh mesh,
            Map<String, Map<String, Material>> materialMap,
            Map<String, List<String>> hiddenMap,
            Map<String, Map<String, Vector2f>> offsetMap,
            List<Runnable> glCalls,
            @Nullable Skeleton skeleton, @Nullable Map<String, Animation> animations, Map<String, ModelConfig.HideDuringAnimation> hideDuringAnimations) {
        var name = mesh.name();

        var map = materialMap.get(name);
        var list = hiddenMap.get(name);
        var offset = offsetMap.get(name);

        var glModel = processPrimitiveModel(mesh, skeleton);
        var renderObject = objSupplier.get();

        if (animations != null && renderObject instanceof AnimatedMeshObject animatedMeshObject) {
            animatedMeshObject.setup(map, list, offset, glModel, name, skeleton, animations, hideDuringAnimations.getOrDefault(name, ModelConfig.HideDuringAnimation.NONE));
        } else {
            renderObject.setup(map, list, offset, glModel, name);
        }

        objects.add(renderObject);
    }

    private static GLModel processPrimitiveModel(Model mesh, Skeleton skeleton) {
        var model = new GLModel();

        var length = calculateVertexSize(ATTRIBUTES);
        var amount = mesh.vertices().size();

        var vertexBuffer = MemoryUtil.memAlloc(length * amount);

        var bones = IntStream.range(0, amount).mapToObj(a -> new VertexBoneData()).toList();

        var isEmpty = mesh.vertices().stream().map(Model.Vertex::vertexBoneData).allMatch(VertexBoneData::isEmpty);

        for (int i = 0; i < amount; i++) {

            var position = mesh.positions().get(i);
            var uv = mesh.uvs().get(i);
            var normal = mesh.normals().get(i);
            var bone = bones.get(i);

            vertexBuffer.putFloat(position.x);
            vertexBuffer.putFloat(position.y);
            vertexBuffer.putFloat(position.z);
            vertexBuffer.putFloat(uv.x);
            vertexBuffer.putFloat(uv.y);
            vertexBuffer.putFloat(normal.x);
            vertexBuffer.putFloat(normal.y);
            vertexBuffer.putFloat(normal.z);

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
                vertexBuffer.put((byte) bone.ids()[0]);
                vertexBuffer.put((byte) bone.ids()[1]);
                vertexBuffer.put((byte) bone.ids()[2]);
                vertexBuffer.put((byte) bone.ids()[3]);

                vertexBuffer.putFloat(bone.weights()[0]);
                vertexBuffer.putFloat(bone.weights()[1]);
                vertexBuffer.putFloat(bone.weights()[2]);
                vertexBuffer.putFloat(bone.weights()[3]);
            }
        }

        vertexBuffer.flip();
        model.vertexBuffer = vertexBuffer;

        var indexBuffer = MemoryUtil.memAlloc(mesh.indices().size() * 4);
        indexBuffer.asIntBuffer().put(mesh.indices().stream().mapToInt(a -> a).toArray()).flip();

        model.indexBuffer = indexBuffer;
        model.indexSize = mesh.indices().size();

        return model;
    }

    public static int generateVao(ByteBuffer vertexBuffer, List<Attribute> layout) {
        var vao = glGenVertexArrays();

        glBindVertexArray(vao);
        var stride = calculateVertexSize(layout);
        var attribPtr = 0;

        // I hate openGL. why cant I keep the vertex data and vertex layout separate :(
        var vbo = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
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

        return vao;
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

    public <T extends RenderObject> CompletableFuture<MultiRenderObject<T>> createObjectThreaded(@NotNull Supplier<PixelAsset> is, GlCallSupplier<T> objectCreator, Consumer<MultiRenderObject<T>> onFinish) {
        return threadedCreateObject(is, objectCreator, onFinish, modelLoadingPool);
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

    private <T extends RenderObject> CompletableFuture<MultiRenderObject<T>> threadedCreateObject(@NotNull Supplier<PixelAsset> is, GlCallSupplier<T> objectCreator, Consumer<MultiRenderObject<T>> onFinish, ExecutorService service) {
        var asset = is.get();
        if(asset.getModelFile() == null) return CompletableFuture.completedFuture(null);
        var config = asset.getConfig();

        var images = readImages(asset, service);
        var smdAnims = readSmdAnimations(asset, service);
        var gfbAnims = readAnimations(asset, "gfbanm", service);
        var tracms = readAnimations(asset, "tracm", service);
        var tranms = readAnimations(asset, "tranm", service);

        ModelLoader.create2(object, gltfModel, smdFileMap, gfbFileMap, tramnAnimations, images, config, glCalls, supplier);


        return CompletableFuture.allOf(images, smdAnims, gfbAnims, trAnims).thenApplyAsync(unused -> {
            try {
                var obj = new MultiRenderObject<T>();
                return new Pair<>(obj, objectCreator.getCalls(asset, smdAnims.get(), gfbAnims.get(), trAnims.get(), images.get(), config, obj));
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }, service).thenApplyAsync(pair -> {
            var obj = pair.a();
            if (config != null) obj.scale = config.scale;

            ThreadSafety.runOnContextThread(() -> {
                pair.b().forEach(Runnable::run);
                obj.updateDimensions();
                if (onFinish != null) onFinish.accept(obj);
            });

            return obj;
        }, service);
    }

    private CompletableFuture<Map<String, String>> readImages(PixelAsset asset, ExecutorService service) {
        var images = asset.getImageFiles();

        var map = new HashMap<String,CompletableFuture<String>>();

        for (var entry : images) {
            var key = entry.getKey();

            var future1 = CompletableFuture.supplyAsync(entry::getValue, service).thenApplyAsync(a -> {
                try {
                    return TextureReference.read(a, key);
                } catch (IOException e) {
                    return null;
                }
            }, service).thenApplyAsync(reference -> {
                var id = asset.name + "-" + key;
                if(reference != null) ITextureLoader.instance().register(id, reference);
                return id;
            }, service);

            map.put(key, future1);



//            try {
//                var id = asset.name + "-" + key;
//                ITextureLoader.instance().register(id, TextureReference.read(entry.getValue(), key));
//
//                map.put(key, id);
//            } catch (IOException e) {
//                System.out.print("Error couldn't load: " + key); //TODO: Logger solution.
//            }
        }

        return CompletableFuture.allOf(map.values().toArray(new CompletableFuture[0])).thenApplyAsync(ignored -> {
            var resultMap = new HashMap<String, String>();
            for (var entry : map.entrySet()) {
                try {
                    resultMap.put(entry.getKey(), entry.getValue().get());
                } catch (Exception e) {
                    // Handle exception if needed
                }
            }

            return resultMap;
        }, service);

//        var map = new HashMap<String, String>();
//        return map;
    }


    public CompletableFuture<Map<String, byte[]>> readAnimations(PixelAsset asset, String extension, ExecutorService service) {
        return CompletableFuture.supplyAsync(() ->
                asset.files.entrySet().stream()
                        .filter(entry -> entry.getKey().endsWith(extension))
                        .collect(Collectors.toMap(this::cleanAnimName, Map.Entry::getValue)), service);
        );
    }
    public CompletableFuture<HashMap<String, Pair<byte[], byte[]>>> readtrAnimations(PixelAsset asset, ExecutorService service) {
        return CompletableFuture.supplyAsync(() -> {
            var map = new HashMap<String, Pair<byte[], byte[]>>();
            var list = asset.files.keySet().stream()
                    .filter(a -> a.endsWith("tranm") || a.endsWith("tracm"))
                    .collect(Collectors.toCollection(ArrayList::new));

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
        }, service);
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

    public CompletableFuture<Map<String, SMDFile>> readSmdAnimations(PixelAsset pixelAsset, ExecutorService service) {
        var files = pixelAsset.getAnimationFiles();
        var map = new HashMap<String, CompletableFuture<SMDFile>>();
        var reader = new SMDTextReader();

        for (var entry : files) {
            var future = CompletableFuture.supplyAsync(entry::getValue, service).thenApplyAsync(String::new, service).thenApplyAsync(reader::read, service);

            map.put(cleanAnimName(entry.getKey().replace(".smd", "")), future);
        }

        // Combine all CompletableFutures into one CompletableFuture that completes when all of them are done
        return CompletableFuture.allOf(map.values().toArray(new CompletableFuture[0]))
                .thenApplyAsync(ignored -> {
                    var resultMap = new HashMap<String, SMDFile>();
                    for (var entry : map.entrySet()) {
                        try {
                            resultMap.put(entry.getKey(), entry.getValue().get());
                        } catch (Exception e) {
                            // Handle exception if needed
                        }
                    }
                    return resultMap;
                }, service);
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
}