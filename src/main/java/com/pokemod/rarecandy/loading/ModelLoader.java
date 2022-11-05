package com.pokemod.rarecandy.loading;

import com.pokemod.TriFunction;
import com.pokemod.pokeutils.PixelAsset;
import com.pokemod.pokeutils.reader.TextureReference;
import com.pokemod.rarecandy.DataUtils;
import com.pokemod.rarecandy.ThreadSafety;
import com.pokemod.rarecandy.animation.Animation;
import com.pokemod.rarecandy.animation.Skeleton;
import com.pokemod.rarecandy.components.AnimatedMeshObject;
import com.pokemod.rarecandy.components.MeshObject;
import com.pokemod.rarecandy.components.MultiRenderObject;
import com.pokemod.rarecandy.components.RenderObject;
import com.pokemod.rarecandy.model.GLModel;
import com.pokemod.rarecandy.model.Material;
import com.pokemod.rarecandy.model.MeshDrawCommand;
import com.pokemod.rarecandy.pipeline.Pipeline;
import de.javagl.jgltf.model.AccessorModel;
import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.MeshModel;
import de.javagl.jgltf.model.MeshPrimitiveModel;
import de.javagl.jgltf.model.image.PixelDatas;
import de.javagl.jgltf.model.io.GltfModelReader;
import de.javagl.jgltf.model.v2.MaterialModelV2;
import dev.thecodewarrior.binarysmd.formats.SMDBinaryReader;
import dev.thecodewarrior.binarysmd.studiomdl.SMDFile;
import dev.thecodewarrior.binarysmd.studiomdl.SMDFileBlock;
import dev.thecodewarrior.binarysmd.studiomdl.SkeletonBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.msgpack.core.MessagePack;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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

    public <T extends RenderObject> MultiRenderObject<T> createObject(@NotNull Supplier<PixelAsset> is, TriFunction<GltfModel, Map<String, SMDFile>, MultiRenderObject<T>, List<Runnable>> objectCreator, Consumer<MultiRenderObject<T>> onFinish) {
        var obj = new MultiRenderObject<T>();
        modelLoadingPool.submit(ThreadSafety.wrapException(() -> {
            var asset = is.get();
            var model = read(asset);
            var separateAims = readAnimations(asset);
            var glCalls = objectCreator.apply(model, separateAims, obj);
            ThreadSafety.runOnContextThread(() -> {
                glCalls.forEach(Runnable::run);
                obj.updateDimensions();
                if (onFinish != null) onFinish.accept(obj);
            });
        }));
        return obj;
    }

    private Map<String, SMDFile> readAnimations(PixelAsset pixelAsset) {
        var files = pixelAsset.getAnimationFiles();

        var map = new HashMap<String, SMDFile>();
        var reader = new SMDBinaryReader();

        for (Map.Entry<String, byte[]> entry : files) {
            var unpacker = MessagePack.newDefaultUnpacker(entry.getValue());
            try {
                var smdFile = reader.read(unpacker);
                map.put(entry.getKey(), smdFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            throw new RuntimeException("", e);
        }
    }

    public static <T extends MeshObject> void create(MultiRenderObject<T> objects, GltfModel gltfModel, Map<String, SMDFile> smdFileMap, List<Runnable> glCalls, Function<String, Pipeline> pipeline, Supplier<T> supplier) {
        checkForRootTransformation(objects, gltfModel);

        var textures = gltfModel.getTextureModels().stream().map(raw -> new TextureReference(PixelDatas.create(raw.getImageModel().getImageData()), raw.getImageModel().getName())).toList();

        var materials = gltfModel.getMaterialModels().stream().map(MaterialModelV2.class::cast).map(raw -> {
            var textureName = raw.getBaseColorTexture().getImageModel().getName();

            int textureIndex = IntStream.range(0, textures.size()).filter(a -> textures.get(a).name().equals(textureName)).findFirst().orElse(-1);

            var textureReference = textures.get(textureIndex);

            return new Material(raw.getName(), textureReference);
        }).toList();
        var variants = getVariants(gltfModel);

        Map<String, Animation> animations = null;

        if (!gltfModel.getSkinModels().isEmpty()) {
            var skeleton = new Skeleton(gltfModel.getSkinModels().get(0));
            animations = gltfModel.getAnimationModels().stream().map(animationModel -> new Animation(animationModel, new Skeleton(skeleton))).collect(Collectors.toMap(animation -> animation.name, animation -> animation));

            for (Map.Entry<String, SMDFile> entry : smdFileMap.entrySet()) {
                var key = entry.getKey();
                var value = entry.getValue();

                for (SMDFileBlock block : value.blocks) {
                    if (block instanceof SkeletonBlock skeletonBlock) {
                        var animation = new Animation(key, skeletonBlock, new Skeleton(skeleton));
                        animations.put(key, animation);
                        break;
                    }
                }
            }
        }

        for (var sceneModel : gltfModel.getSceneModels()) {
            for (var nodeModel : sceneModel.getNodeModels()) {
                if (nodeModel.getChildren().isEmpty()) {
                    // Model Loading Method #1
                    for (var meshModel : nodeModel.getMeshModels()) {
                        processPrimitiveModels(objects, supplier, meshModel, materials, variants, pipeline, glCalls, animations);
                    }
                } else {
                    // Model Loading Method #2
                    for (var child : nodeModel.getChildren()) {
                        for (var meshModel : child.getMeshModels()) {
                            processPrimitiveModels(objects, supplier, meshModel, materials, variants, pipeline, glCalls, animations);
                        }
                    }
                }
            }
        }
    }

    private static <T extends MeshObject> void checkForRootTransformation(MultiRenderObject<T> objects, GltfModel gltfModel) {
        if(gltfModel.getSkinModels().isEmpty()) {

            var node = gltfModel.getNodeModels().get(0);

            while (node.getParent() != null) node = node.getParent();

            var rootTransformation = new Matrix4f().set(node.createGlobalTransformSupplier().get());

            objects.setRootTransformation(rootTransformation);
        }
    }

    private static <T extends MeshObject> void processPrimitiveModels(MultiRenderObject<T> objects, Supplier<T> objSupplier, MeshModel model, List<Material> materials, List<String> variantsList, Function<String, Pipeline> pipeline, List<Runnable> glCalls, @Nullable Map<String, Animation> animations) {
        for (var primitiveModel : model.getMeshPrimitiveModels()) {
            var variants = createMeshVariantMap(primitiveModel, materials, variantsList);
            var glModel = processPrimitiveModel(primitiveModel, glCalls);
            var renderObject = objSupplier.get();
            var appliedPipeline = pipeline.apply(primitiveModel.getMaterialModel().getName());

            if (animations != null && renderObject instanceof AnimatedMeshObject animatedMeshObject) {
                animatedMeshObject.setup(materials, variants, glModel, appliedPipeline, animations);
            } else {
                renderObject.setup(materials, variants, glModel, appliedPipeline);
            }

            objects.add(renderObject);
        }
    }

    private static void processMeshModel(AnimatedMeshObject object, MeshModel meshModel, List<Material> materials, List<String> variantsList, Pipeline pipeline, Map<String, Animation> animations, List<Runnable> glCalls) {
        for (var primitiveModel : meshModel.getMeshPrimitiveModels()) {
            var variants = createMeshVariantMap(primitiveModel, materials, variantsList);
            var glModel = processPrimitiveModel(primitiveModel, glCalls);
            if (animations == null)
                throw new RuntimeException("No animations found when trying to load animated model.");
            object.setup(materials, variants, glModel, pipeline, animations);
        }
    }

    private static void processPrimitiveModel(MeshObject object, MeshPrimitiveModel primitiveModel, List<Material> materials, List<String> variantsList, Pipeline pipeline, List<Runnable> glCalls) {
        var variants = createMeshVariantMap(primitiveModel, materials, variantsList);
        var glModel = processPrimitiveModel(primitiveModel, glCalls);
        object.setup(materials, variants, glModel, pipeline);
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
                if (smallestVertexX > xPoint) smallestVertexX = xPoint;
                if (smallestVertexY > xPoint) smallestVertexY = yPoint;
                if (smallestVertexZ > xPoint) smallestVertexZ = zPoint;
                if (largestVertexX < xPoint) largestVertexX = xPoint;
                if (largestVertexY < yPoint) largestVertexY = yPoint;
                if (largestVertexZ < zPoint) largestVertexZ = zPoint;
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

    private static Map<String, Material> createMeshVariantMap(MeshPrimitiveModel primitiveModel, List<Material> materials, List<String> variantsList) {
        if (variantsList == null) {
            String materialId = primitiveModel.getMaterialModel().getName();
            return Collections.singletonMap("default", materials.stream().filter(a -> a.getMaterialName().equals(materialId)).findAny().get());
        } else {
            var map = (Map<String, Object>) primitiveModel.getExtensions().get("KHR_materials_variants");
            var mappings = (List<Map<String, Object>>) map.get("mappings");
            var variantMap = new HashMap<String, Material>();

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
