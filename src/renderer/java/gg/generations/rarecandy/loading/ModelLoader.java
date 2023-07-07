package gg.generations.rarecandy.loading;

import de.javagl.jgltf.model.*;
import de.javagl.jgltf.model.image.PixelDatas;
import de.javagl.jgltf.model.io.GltfModelReader;
import de.javagl.jgltf.model.v2.MaterialModelV2;
import dev.thecodewarrior.binarysmd.formats.SMDTextReader;
import dev.thecodewarrior.binarysmd.studiomdl.SMDFile;
import dev.thecodewarrior.binarysmd.studiomdl.SkeletonBlock;
import gg.generations.pokeutils.DataUtils;
import gg.generations.pokeutils.ModelConfig;
import gg.generations.pokeutils.PixelAsset;
import gg.generations.pokeutils.VariantReference;
import gg.generations.pokeutils.reader.TextureReference;
import gg.generations.rarecandy.ThreadSafety;
import gg.generations.rarecandy.animation.Animation;
import gg.generations.rarecandy.animation.Skeleton;
import gg.generations.rarecandy.components.AnimatedMeshObject;
import gg.generations.rarecandy.components.MeshObject;
import gg.generations.rarecandy.components.MultiRenderObject;
import gg.generations.rarecandy.components.RenderObject;
import gg.generations.rarecandy.model.GLModel;
import gg.generations.rarecandy.model.GlCallSupplier;
import gg.generations.rarecandy.model.MeshDrawCommand;
import gg.generations.rarecandy.model.Variant;
import gg.generations.rarecandy.model.material.Material;
import gg.generations.rarecandy.model.material.SolidMaterial;
import gg.generations.rarecandy.pipeline.Pipeline;
import gg.generations.rarecandy.rendering.RareCandy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ModelLoader {
    private final GltfModelReader reader = new GltfModelReader();
    private final ExecutorService modelLoadingPool;

    public ModelLoader() {
        this.modelLoadingPool = Executors.newFixedThreadPool(2);
    }

    public <T extends RenderObject> MultiRenderObject<T> createObject(@NotNull Supplier<PixelAsset> is, GlCallSupplier<T> objectCreator, Consumer<MultiRenderObject<T>> onFinish) {
        var obj = new MultiRenderObject<T>();
        var task = threadedCreateObject(obj, is, objectCreator, onFinish);
        if (RareCandy.DEBUG_THREADS) task.run();
        else modelLoadingPool.submit(task);
        return obj;
    }

    private <T extends RenderObject> Runnable threadedCreateObject(MultiRenderObject<T> obj, @NotNull Supplier<PixelAsset> is, GlCallSupplier<T> objectCreator, Consumer<MultiRenderObject<T>> onFinish) {
        return ThreadSafety.wrapException(() -> {
            var asset = is.get();
            var config = asset.getConfig();

            if(config != null) obj.scale = config.scale;
            var model = read(asset);
            var smdAnims = readSmdAnimations(asset);
            var gfbAnims = readGfbAnimations(asset);
            var images = readImages(asset);
            var glCalls = objectCreator.getCalls(model, smdAnims, gfbAnims, images, config, obj);
            ThreadSafety.runOnContextThread(() -> {
                glCalls.forEach(Runnable::run);
                obj.updateDimensions();
                if (onFinish != null) onFinish.accept(obj);
            });
        });
    }

    private Map<String, TextureReference> readImages(PixelAsset asset) {
        var images = asset.getImageFiles();
        var map = new HashMap<String, TextureReference>();
        for (var entry : images) {
            var key = entry.getKey();

            try {
                map.put(key, TextureReference.read(entry.getValue(), key));
            } catch (IOException e) {
                System.out.println("Error couldn't load: " + key);
            }
        }

        return map;
    }

    private Map<String, byte[]> readGfbAnimations(PixelAsset asset) {
        return asset.files.entrySet().stream()
                .filter(entry -> entry.getKey().endsWith(".pkx") || entry.getKey().endsWith(".gfbanm") || entry.getKey().endsWith(".tranm"))
                .collect(Collectors.toMap(this::cleanAnimName, Map.Entry::getValue));
    }

    private String cleanAnimName(Map.Entry<String, byte[]> entry) {
        var str = entry.getKey();
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
            map.put(entry.getKey(), smdFile);
        }

        return map;
    }

    public void close() {
        modelLoadingPool.shutdown();
    }

    public GltfModel read(PixelAsset asset) {
        try {
            return reader.readWithoutReferences(new ByteArrayInputStream(asset.getModelFile()));
        } catch (IOException e) {
            throw new RuntimeException("Issue reading GLTF Model", e);
        }
    }

    public static <T extends MeshObject> void create2(MultiRenderObject<T> objects, GltfModel gltfModel, Map<String, SMDFile> smdFileMap, Map<String, byte[]> gfbFileMap, Map<String, TextureReference> images, ModelConfig config, List<Runnable> glCalls, Function<String, Pipeline> pipeline, Supplier<T> supplier) {
        create2(objects, gltfModel, smdFileMap, gfbFileMap, images, config, glCalls, pipeline, supplier, Animation.GLB_SPEED);
    }

    public static <T extends MeshObject> void create2(MultiRenderObject<T> objects, GltfModel gltfModel, Map<String, SMDFile> smdFileMap, Map<String, byte[]> gfbFileMap, Map<String, TextureReference> images, ModelConfig config, List<Runnable> glCalls, Function<String, Pipeline> pipeline, Supplier<T> supplier, int animationSpeed) {
        checkForRootTransformation(objects, gltfModel);
        if (gltfModel.getSceneModels().size() > 1) throw new RuntimeException("Cannot handle more than one scene");

        Map<String, Animation> animations = null;

        Skeleton skeleton;

        if (!gltfModel.getSkinModels().isEmpty()) {
            skeleton = new Skeleton(gltfModel.getSkinModels().get(0));
            animations = gltfModel.getAnimationModels().stream().map(animationModel -> new Animation(animationModel, skeleton, animationSpeed)).collect(Collectors.toMap(animation -> animation.name, animation -> animation));

            for (var entry : gfbFileMap.entrySet()) {
                var name = entry.getKey();
                var buffer = ByteBuffer.wrap(entry.getValue());
                var gfbAnim = gg.generations.pokeutils.tranm.Animation.getRootAsAnimation(buffer);
                animations.put(name, new Animation(name, gfbAnim, new Skeleton(skeleton)));
            }

            for (var entry : smdFileMap.entrySet()) {
                var key = entry.getKey();
                var value = entry.getValue();

                for (var block : value.blocks) {
                    if (block instanceof SkeletonBlock skeletonBlock) {
                        animations.put(key, new Animation(key, skeletonBlock, new Skeleton(skeleton), Animation.FPS_24));
                        break;
                    }
                }
            }
        } else {
            skeleton = null;
        }

        if(config != null) {
            var materials = config.materials.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, a -> new Material(a.getValue(), images)));
            var variantPair = new HashMap<VariantReference, Variant>();
            var defaultVariant = config.defaultVariant.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, a -> variantPair.computeIfAbsent(a.getValue().fillIn(), b -> new Variant(materials.get(b.material()), b.hide()))));

            Map<String, Map<String, Variant>> variants = new HashMap<String, Map<String, Variant>>();

            for (var variantName : config.variants.keySet()) {
                for (var entry : config.variants.get(variantName).entrySet()) {
                    variants.computeIfAbsent(variantName, a -> new HashMap<>()).put(entry.getKey(), variantPair.computeIfAbsent(entry.getValue().fillIn(config.defaultVariant.get(entry.getKey())), b -> new Variant(materials.get(b.material()), b.hide())));
                }

                for (Map.Entry<String, Variant> entry : defaultVariant.entrySet()) {
                    String k = entry.getKey();
                    Variant v = entry.getValue();

                    for (Map<String, Variant> map : variants.values()) {
                        if (!map.containsKey(k)) map.put(k, v);
                    }
                }
            }

            variants = switchKeys(variants);

            for (var node : gltfModel.getSceneModels().get(0).getNodeModels()) {
                var transform = new Matrix4f();
                applyTransforms(transform, node);

                if (node.getChildren().isEmpty()) {
                    // Model Loading Method #1
                    objects.setRootTransformation(objects.getRootTransformation().add(transform, new Matrix4f()));

                    for (var meshModel : node.getMeshModels()) {
                        processPrimitiveModels(objects, supplier, meshModel, defaultVariant, variants, pipeline, glCalls, skeleton, animations);
                    }
                } else {
                    // Model Loading Method #2
                    for (var child : node.getChildren()) {
                        applyTransforms(transform, child);
                        objects.setRootTransformation(objects.getRootTransformation().add(transform, new Matrix4f()));

                        for (var meshModel : child.getMeshModels()) {
                            processPrimitiveModels(objects, supplier, meshModel, defaultVariant, variants, pipeline, glCalls, skeleton, animations);
                        }
                    }
                }
            }
        } else {

            //Original model loading code

            var textures = gltfModel.getTextureModels().stream().map(raw -> new TextureReference(PixelDatas.create(raw.getImageModel().getImageData()), raw.getImageModel().getName())).toList();
            var materials = gltfModel.getMaterialModels().stream().map(MaterialModelV2.class::cast).map(raw -> {
                var textureName = raw.getBaseColorTexture().getImageModel().getName();
                int textureIndex = IntStream.range(0, textures.size()).filter(a -> textures.get(a).name().equals(textureName)).findFirst().orElse(-1);
                var textureReference = textures.get(textureIndex);
                return new SolidMaterial(raw.getName(), textureReference);
            }).map(Variant::new).toList();
            var variants = getVariants(gltfModel);

            // gltfModel.getSceneModels().get(0).getNodeModels().get(0).getScale()
            for (var node : gltfModel.getSceneModels().get(0).getNodeModels()) {
                var transform = new Matrix4f();
                applyTransforms(transform, node);

                if (node.getChildren().isEmpty()) {
                    // Model Loading Method #1
                    objects.setRootTransformation(objects.getRootTransformation().add(transform, new Matrix4f()));

                    for (var meshModel : node.getMeshModels()) {
                        processPrimitiveModels(objects, supplier, meshModel, materials, variants, pipeline, glCalls, skeleton, animations);
                    }
                } else {
                    // Model Loading Method #2
                    for (var child : node.getChildren()) {
                        applyTransforms(transform, child);
                        objects.setRootTransformation(objects.getRootTransformation().add(transform, new Matrix4f()));

                        for (var meshModel : child.getMeshModels()) {
                            processPrimitiveModels(objects, supplier, meshModel, materials, variants, pipeline, glCalls, skeleton, animations);
                        }
                    }
                }
            }
        }
    }

    public static <T> Map<String, Map<String, T>> switchKeys(Map<String, Map<String, T>> inputMap) {
        Map<String, Map<String, T>> switchedMap = new HashMap<>();

        for (String outerKey : inputMap.keySet()) {
            Map<String, T> innerMap = inputMap.get(outerKey);

            for (String innerKey : innerMap.keySet()) {
                T value = innerMap.get(innerKey);

                // Swap the keys
                Map<String, T> switchedInnerMap = switchedMap.getOrDefault(innerKey, new HashMap<>());
                switchedInnerMap.put(outerKey, value);
                switchedMap.put(innerKey, switchedInnerMap);
            }
        }

        return switchedMap;
    }

    private static void applyTransforms(Matrix4f transform, NodeModel node) {
        if (node.getScale() != null) transform.scale(new Vector3f(node.getScale()));
        if (node.getRotation() != null)
            transform.rotate(new Quaternionf(node.getRotation()[0], node.getRotation()[1], node.getRotation()[2], node.getRotation()[3]));
        if (node.getTranslation() != null) {
            if (node.getTranslation().length == 3)
                transform.add(new Matrix4f().setTranslation(node.getTranslation()[0], node.getTranslation()[1], node.getTranslation()[2]));
            else
                transform.add(new Matrix4f().set(node.getTranslation()));
        }
    }

    private static <T extends MeshObject> void checkForRootTransformation(MultiRenderObject<T> objects, GltfModel gltfModel) {
        if (gltfModel.getSkinModels().isEmpty()) {
            var node = gltfModel.getNodeModels().get(0);
            while (node.getParent() != null) node = node.getParent();
            var rootTransformation = new Matrix4f().set(node.createGlobalTransformSupplier().get());
            objects.setRootTransformation(rootTransformation);
        }
    }

    private static <T extends MeshObject> void processPrimitiveModels(MultiRenderObject<T> objects, Supplier<T> objSupplier, MeshModel model, List<Variant> materials, List<String> variantsList, Function<String, Pipeline> pipeline, List<Runnable> glCalls, @Nullable Skeleton skeleton, @Nullable Map<String, Animation> animations) {
        for (var primitiveModel : model.getMeshPrimitiveModels()) {
            var variants = createMeshVariantMap(primitiveModel, materials, variantsList);
            var glModel = processPrimitiveModel(primitiveModel, glCalls);
            var renderObject = objSupplier.get();

            if (animations != null && renderObject instanceof AnimatedMeshObject animatedMeshObject) {
                animatedMeshObject.setup(materials.get(0), variants, glModel, pipeline, model.getName(), skeleton, animations);
            } else {
                renderObject.setup(materials.get(0), variants, glModel, pipeline, model.getName());
            }

            objects.add(renderObject);
        }
    }


    private static <T extends MeshObject> void processPrimitiveModels(MultiRenderObject<T> objects, Supplier<T> objSupplier, MeshModel model, Map<String, Variant> defaultVariants, Map<String, Map<String, Variant>> variants, Function<String, Pipeline> pipeline, List<Runnable> glCalls, @Nullable Skeleton skeleton, @Nullable Map<String, Animation> animations) {
        var name = model.getName();

        for (var primitiveModel : model.getMeshPrimitiveModels()) {
            var glModel = processPrimitiveModel(primitiveModel, glCalls);
            var renderObject = objSupplier.get();
//            var appliedPipeline = pipeline.apply(name);

            var defaultvariant = defaultVariants.get(name);
            var variantMap = variants.get(name);

            if (animations != null && renderObject instanceof AnimatedMeshObject animatedMeshObject) {
                animatedMeshObject.setup(defaultvariant, variantMap, glModel, pipeline, name, skeleton, animations);
            } else {
                renderObject.setup(defaultvariant, variantMap, glModel, pipeline, name);
            }

            objects.add(renderObject);
        }
    }


    private static GLModel processPrimitiveModel(MeshPrimitiveModel primitiveModel, List<Runnable> glCalls) {
        var model = new GLModel();
        var attributes = primitiveModel.getAttributes();

        glCalls.add(() -> {
            var vao = GL30.glGenVertexArrays();
            GL30.glBindVertexArray(vao);

            var position = attributes.get("POSITION");
            DataUtils.bindArrayBuffer(position.getBufferViewModel());
            vertexAttribPointer(position, 0);

            // FIXME: this is for old models which dont get proper scaling for
            var buf = position.getBufferViewModel().getBufferViewData();
            var smallestVertexX = 0f;
            var smallestVertexY = 0f;
            var smallestVertexZ = 0f;
            var largestVertexX = 0f;
            var largestVertexY = 0f;
            var largestVertexZ = 0f;
            for (int i = 0; i < buf.capacity(); i += 12) { // Start at the y entry of every vertex and increment by 12 because there are 12 bytes per vertex
                var xPoint = buf.getFloat(i);
                var yPoint = buf.getFloat(i + 4);
                var zPoint = buf.getFloat(i + 8);
                smallestVertexX = Math.min(smallestVertexX, xPoint);
                smallestVertexY = Math.min(smallestVertexY, yPoint);
                smallestVertexZ = Math.min(smallestVertexZ, zPoint);
                largestVertexX = Math.max(largestVertexX, xPoint);
                largestVertexY = Math.max(largestVertexY, yPoint);
                largestVertexZ = Math.max(largestVertexZ, zPoint);
            }
            model.dimensions = new Vector3f(largestVertexX - smallestVertexX, largestVertexY - smallestVertexY, largestVertexZ - smallestVertexZ);

            var uvs = attributes.get("TEXCOORD_0");
            DataUtils.bindArrayBuffer(uvs.getBufferViewModel());
            vertexAttribPointer(uvs, 1);

            var normal = attributes.get("NORMAL");
            DataUtils.bindArrayBuffer(normal.getBufferViewModel());
            vertexAttribPointer(normal, 2);

            var joints = attributes.get("JOINTS_0");

            if (joints != null) {

                DataUtils.bindArrayBuffer(joints.getBufferViewModel());
                vertexAttribPointer(joints, 3);

                var weights = attributes.get("WEIGHTS_0");
                DataUtils.bindArrayBuffer(weights.getBufferViewModel());
                vertexAttribPointer(weights, 4);
            }

            var ebo = GL15.glGenBuffers();
            GL15.glBindBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, ebo);
            GL15.glBufferData(GL15C.GL_ELEMENT_ARRAY_BUFFER, DataUtils.makeDirect(primitiveModel.getIndices().getBufferViewModel().getBufferViewData()), GL15.GL_STATIC_DRAW);

            var mode = primitiveModel.getMode();
            model.meshDrawCommands.add(new MeshDrawCommand(vao, mode, primitiveModel.getIndices().getComponentType(), ebo, primitiveModel.getIndices().getCount()));
        });
        return model;
    }

    private static void vertexAttribPointer(AccessorModel data, int binding) {
        GL20.glEnableVertexAttribArray(binding);
        GL20.glVertexAttribPointer(
                binding,
                data.getElementType().getNumComponents(),
                data.getComponentType(),
                false,
                data.getByteStride(),
                data.getByteOffset());
    }

    private static List<String> getVariants(GltfModel model) {
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

    private static Map<String, Variant> createMeshVariantMap(MeshPrimitiveModel primitiveModel, List<Variant> materials, List<String> variantsList) {
        if (variantsList == null) {
            var materialId = primitiveModel.getMaterialModel().getName();
            return Collections.singletonMap("default", materials.stream().filter(a -> ((SolidMaterial) a.material()).getMaterialName().equals(materialId)).findAny().get());
        } else {
            var map = (Map<String, Object>) primitiveModel.getExtensions().get("KHR_materials_variants");
            var mappings = (List<Map<String, Object>>) map.get("mappings");
            var variantMap = new HashMap<String, Variant>();

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
}