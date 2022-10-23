package com.pixelmongenerations.test;

import com.pixelmongenerations.pkl.PixelAsset;
import com.pixelmongenerations.pkl.reader.AssetType;
import com.pixelmongenerations.rarecandy.components.AnimatedMeshObject;
import com.pixelmongenerations.rarecandy.components.MeshObject;
import com.pixelmongenerations.rarecandy.loading.GogoatLoader;
import com.pixelmongenerations.rarecandy.pipeline.Pipeline;
import com.pixelmongenerations.rarecandy.rendering.RareCandy;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class FeatureTest {
    public final String id;
    public final String description;

    protected FeatureTest(String id, String description) {
        this.id = id;
        this.description = description;
    }

    public abstract void init(RareCandy scene, Matrix4f viewMatrix);

    public abstract void update(RareCandy scene, double deltaTime);

    protected <T extends MeshObject> T load(RareCandy renderer, String name, AssetType type, Pipeline pipeline, Supplier<T> supplier, Consumer<T> onFinish) {
        var loader = renderer.getLoader();
        return loader.createObject(
                () -> new PixelAsset(FeatureTest.class.getResourceAsStream("/new/" + name + (type == AssetType.PK ? ".pk" : ".glb")), type),
                supplier,
                (gltfModel, object) -> {
                    var glCalls = new ArrayList<Runnable>();
                    GogoatLoader.create(object, gltfModel, glCalls, pipeline);
                    return glCalls;
                },
                onFinish
        );
    }

    protected void loadStatUpModel(RareCandy renderer, Consumer<MeshObject> onFinish) {
        load(renderer, "stat_up", AssetType.GLB, Pipelines.statUpPipeline(() -> FeatureTester.PROJECTION_MATRIX), MeshObject::new, onFinish);
    }

    protected void loadStaticModel(RareCandy renderer, String name, Consumer<MeshObject> onFinish) {
        load(renderer, name, AssetType.PK, Pipelines.staticPipeline(() -> FeatureTester.PROJECTION_MATRIX), MeshObject::new, onFinish);
    }

    protected AnimatedMeshObject loadAnimatedModel(RareCandy renderer, String name, Consumer<AnimatedMeshObject> onFinish) {
        return load(renderer, name, AssetType.PK, Pipelines.animatedPipeline(() -> FeatureTester.PROJECTION_MATRIX), AnimatedMeshObject::new, onFinish);
    }
}
