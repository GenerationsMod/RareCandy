package com.pixelmongenerations.test;

import com.pixelmongenerations.pkl.PixelAsset;
import com.pixelmongenerations.pkl.reader.AssetType;
import com.pixelmongenerations.rarecandy.components.AnimatedMeshObject;
import com.pixelmongenerations.rarecandy.components.MeshObject;
import com.pixelmongenerations.rarecandy.components.ObjectHolder;
import com.pixelmongenerations.rarecandy.pipeline.Pipeline;
import com.pixelmongenerations.rarecandy.rendering.RareCandy;
import org.joml.Matrix4f;

public abstract class FeatureTest {
    public final String id;
    public final String description;

    protected FeatureTest(String id, String description) {
        this.id = id;
        this.description = description;
    }

    public abstract void init(RareCandy scene, Matrix4f viewMatrix);

    public abstract void update(RareCandy scene, double deltaTime);

    protected <T extends MeshObject> ObjectHolder<T> load(RareCandy renderer, String name, AssetType type, Pipeline pipeline, Runnable onFinish) {
        var loader = renderer.getLoader();
        return loader.createObject(
                () -> new PixelAsset(FeatureTest.class.getResourceAsStream("/new/" + name + (type == AssetType.PK ? ".pk" : ".glb")), type),
                (asset, runnable) -> (T) MeshObject.create(asset, runnable, pipeline),
                onFinish
        );
    }

    protected ObjectHolder<MeshObject> loadStatUpModel(RareCandy renderer, Runnable onFinish) {
        return load(renderer, "stat_up", AssetType.GLB, Pipelines.statUpPipeline(() -> FeatureTester.PROJECTION_MATRIX), onFinish);
    }

    protected ObjectHolder<MeshObject> loadStaticModel(RareCandy renderer, String name) {
        return load(renderer, name, AssetType.PK, Pipelines.staticPipeline(() -> FeatureTester.PROJECTION_MATRIX), null);
    }

    protected ObjectHolder<AnimatedMeshObject> loadAnimatedModel(RareCandy renderer, String name) {
        return load(renderer, name, AssetType.PK, Pipelines.animatedPipeline(() -> FeatureTester.PROJECTION_MATRIX), null);
    }
}
