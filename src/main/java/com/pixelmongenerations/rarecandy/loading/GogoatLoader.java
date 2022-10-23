package com.pixelmongenerations.rarecandy.loading;

import com.pixelmongenerations.pkl.PixelAsset;
import com.pixelmongenerations.pkl.reader.AssetType;
import com.pixelmongenerations.pkl.reader.TextureReference;
import com.pixelmongenerations.pkl.scene.material.Material;
import com.pixelmongenerations.rarecandy.ThreadSafety;
import com.pixelmongenerations.rarecandy.animation.Animation;
import com.pixelmongenerations.rarecandy.animation.Skeleton;
import com.pixelmongenerations.rarecandy.components.AnimatedMeshObject;
import com.pixelmongenerations.rarecandy.components.MeshObject;
import com.pixelmongenerations.rarecandy.components.RenderObject;
import com.pixelmongenerations.rarecandy.pipeline.Pipeline;
import com.pixelmongenerations.rarecandy.settings.Settings;
import de.javagl.jgltf.model.AccessorModel;
import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.MeshModel;
import de.javagl.jgltf.model.MeshPrimitiveModel;
import de.javagl.jgltf.model.image.PixelDatas;
import de.javagl.jgltf.model.io.GltfModelReader;
import de.javagl.jgltf.model.v2.MaterialModelV2;
import me.hydos.gogoat.GLModel;
import me.hydos.gogoat.MeshDrawCommand;
import me.hydos.gogoat.util.DataUtils;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GogoatLoader {
    private final GltfModelReader reader = new GltfModelReader();
    private final ExecutorService modelLoadingPool;

    public GogoatLoader(Settings settings) {
        this.modelLoadingPool = Executors.newFixedThreadPool(settings.modelLoadingThreads());
    }

    public <T extends RenderObject> T createObject(@NotNull Supplier<PixelAsset> asset, Supplier<T> objectCreator, BiFunction<GltfModel, T, List<Runnable>> objectSetup, Consumer<T> onFinish) {
        var obj = objectCreator.get();
        modelLoadingPool.submit(ThreadSafety.wrapException(() -> {
            var model = read(asset.get());
            var glCalls = objectSetup.apply(model, obj);
            ThreadSafety.runOnContextThread(() -> {
                glCalls.forEach(Runnable::run);
                if (onFinish != null) onFinish.accept(obj);
            });
        }));
        return obj;
    }

    /**
     * Stops everything until the model is loaded. This is a horrible idea
     */
    public <T extends RenderObject> T createObjectNow(@NotNull InputStream is, AssetType type, Function<PixelAsset, T> consumer) {
        return consumer.apply(new PixelAsset(is, type));
    }

    public void close() {
        modelLoadingPool.shutdown();
    }

    public GltfModel read(PixelAsset asset) {
        try {
            return reader.readWithoutReferences(new ByteArrayInputStream(asset.modelFile));
        } catch (IOException e) {
            throw new RuntimeException("", e);
        }
    }

    public static void create(MeshObject object, GltfModel gltfModel, List<Runnable> glCalls, Pipeline pipeline) {
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
            var bones = new Skeleton(gltfModel.getSkinModels().get(0));
            animations = gltfModel.getAnimationModels().stream().map(animationModel -> new Animation(animationModel, bones)).collect(Collectors.toMap(animation -> animation.name, animation -> animation));
        }

        for (var sceneModel : gltfModel.getSceneModels()) {
            for (var nodeModel : sceneModel.getNodeModels()) {
                if (nodeModel.getChildren().isEmpty()) {
                    // Model Loading Method #1
                    for (var meshModel : nodeModel.getMeshModels()) {
                        if (object instanceof AnimatedMeshObject animatedMeshObject)
                            processMeshModel(animatedMeshObject, meshModel, materials, variants, pipeline, animations, glCalls);
                        else processMeshModel(object, meshModel, materials, variants, pipeline, glCalls);
                    }
                }

                // Model Loading Method #2
                for (var child : nodeModel.getChildren()) {
                    for (var meshModel : child.getMeshModels()) {
                        if (object instanceof AnimatedMeshObject animatedMeshObject)
                            processMeshModel(animatedMeshObject, meshModel, materials, variants, pipeline, animations, glCalls);
                        else processMeshModel(object, meshModel, materials, variants, pipeline, glCalls);
                    }
                }
            }
        }
    }

    private static void processMeshModel(MeshObject object, MeshModel meshModel, List<Material> materials, List<String> variantsList, Pipeline pipeline, List<Runnable> runnables) {
        for (var primitiveModel : meshModel.getMeshPrimitiveModels()) {
            var variants = createMeshVariantMap(primitiveModel, materials, variantsList);
            var glModel = processPrimitiveModel(primitiveModel, runnables);
            object.setup(materials, variants, glModel, pipeline);
        }
    }

    private static void processMeshModel(AnimatedMeshObject object, MeshModel meshModel, List<Material> materials, List<String> variantsList, Pipeline pipeline, Map<String, Animation> animations, List<Runnable> runnables) {
        for (var primitiveModel : meshModel.getMeshPrimitiveModels()) {
            var variants = createMeshVariantMap(primitiveModel, materials, variantsList);
            var glModel = processPrimitiveModel(primitiveModel, runnables);
            if (animations == null)
                throw new RuntimeException("No animations found when trying to load animated model.");
            object.setup(materials, variants, glModel, pipeline, animations);
        }
    }

    private static GLModel processPrimitiveModel(MeshPrimitiveModel primitiveModel, List<Runnable> runnables) {
        var model = new GLModel();
        var attributes = primitiveModel.getAttributes();

        runnables.add(() -> {
            var vao = GL30.glGenVertexArrays();
            GL30.glBindVertexArray(vao);

            var position = attributes.get("POSITION");
            DataUtils.bindArrayBuffer(position.getBufferViewModel());
            vertexAttribPointer(position, 0);

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
