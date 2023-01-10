package com.pokemod.rarecandy.loading;

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
import com.pokemod.rarecandy.model.GlCallSupplier;
import com.pokemod.rarecandy.model.Material;
import com.pokemod.rarecandy.model.MeshDrawCommand;
import com.pokemod.rarecandy.pipeline.Pipeline;
import de.javagl.jgltf.model.*;
import de.javagl.jgltf.model.image.PixelDatas;
import de.javagl.jgltf.model.io.GltfModelReader;
import de.javagl.jgltf.model.v2.MaterialModelV2;
import dev.thecodewarrior.binarysmd.formats.SMDTextReader;
import dev.thecodewarrior.binarysmd.studiomdl.SMDFile;
import dev.thecodewarrior.binarysmd.studiomdl.SkeletonBlock;
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
        modelLoadingPool.submit(ThreadSafety.wrapException(() -> {
            var asset = is.get();
            var model = read(asset);
            var smdAnims = readSmdAnimations(asset);
            var gfbAnims = readGfbAnimations(asset);
            var glCalls = objectCreator.getCalls(model, smdAnims, gfbAnims, obj);
            ThreadSafety.runOnContextThread(() -> {
                glCalls.forEach(Runnable::run);
                obj.updateDimensions();
                if (onFinish != null) onFinish.accept(obj);
            });
        }));
        return obj;
    }

    private Map<String, byte[]> readGfbAnimations(PixelAsset asset) {
        return asset.files.entrySet().stream()
                .filter(stringEntry -> stringEntry.getKey().endsWith(".pkx"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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

    public static <T extends MeshObject> void create2(MultiRenderObject<T> objects, GltfModel gltfModel, Map<String, SMDFile> smdFileMap, Map<String, byte[]> gfbFileMap, List<Runnable> glCalls, Function<String, Pipeline> pipeline, Supplier<T> supplier) {
        create2(objects, gltfModel, smdFileMap, gfbFileMap, glCalls, pipeline, supplier, Animation.GLB_SPEED);
    }

    public static <T extends MeshObject> void create2(MultiRenderObject<T> objects, GltfModel gltfModel, Map<String, SMDFile> smdFileMap, Map<String, byte[]> gfbFileMap, List<Runnable> glCalls, Function<String, Pipeline> pipeline, Supplier<T> supplier, int animationSpeed) {
        checkForRootTransformation(objects, gltfModel);
        if (gltfModel.getSceneModels().size() > 1) throw new RuntimeException("Cannot handle more than one scene");

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
            animations = gltfModel.getAnimationModels().stream().map(animationModel -> new Animation(animationModel, new Skeleton(skeleton), animationSpeed)).collect(Collectors.toMap(animation -> animation.name, animation -> animation));

            for (var entry : gfbFileMap.entrySet()) {
                var name = entry.getKey();
                var buffer = ByteBuffer.wrap(entry.getValue());
                var gfbAnim = com.pokemod.miraidon.Animation.getRootAsAnimation(buffer);
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
        }

        // gltfModel.getSceneModels().get(0).getNodeModels().get(0).getScale()
        for (var node : gltfModel.getSceneModels().get(0).getNodeModels()) {
            var transform = new Matrix4f();
            applyTransforms(transform, node);

            if (node.getChildren().isEmpty()) {
                // Model Loading Method #1
                objects.setRootTransformation(objects.getRootTransformation().add(transform, new Matrix4f()));

                for (var meshModel : node.getMeshModels()) {
                    processPrimitiveModels(objects, supplier, meshModel, materials, variants, pipeline, glCalls, animations);
                }
            } else {
                // Model Loading Method #2
                for (var child : node.getChildren()) {
                    applyTransforms(transform, child);
                    objects.setRootTransformation(objects.getRootTransformation().add(transform, new Matrix4f()));

                    for (var meshModel : child.getMeshModels()) {
                        processPrimitiveModels(objects, supplier, meshModel, materials, variants, pipeline, glCalls, animations);
                    }
                }
            }
        }
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
            var materialId = primitiveModel.getMaterialModel().getName();
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
