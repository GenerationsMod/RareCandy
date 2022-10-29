package com.pokemod.test;

import com.pokemod.pkl.PixelAsset;
import com.pokemod.rarecandy.components.AnimatedMeshObject;
import com.pokemod.rarecandy.components.MeshObject;
import com.pokemod.rarecandy.loading.GogoatLoader;
import com.pokemod.rarecandy.pipeline.Pipeline;
import com.pokemod.rarecandy.rendering.RareCandy;
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

    protected <T extends MeshObject> T load(RareCandy renderer, String name, Pipeline pipeline, Supplier<T> supplier, Consumer<T> onFinish) {
        var loader = renderer.getLoader();
        return loader.createObject(
                () -> new PixelAsset(FeatureTest.class.getResourceAsStream("/new/" + name + ".pk")),
                supplier,
                (gltfModel, smdFileMap, object) -> {
                    var glCalls = new ArrayList<Runnable>();
                    GogoatLoader.create(object, gltfModel, smdFileMap, glCalls, pipeline);
                    return glCalls;
                },
                onFinish
        );
    }

    protected void loadStatUpModel(RareCandy renderer, Consumer<MeshObject> onFinish) {
        load(renderer, "stat_up", Pipelines.statUpPipeline(() -> FeatureTester.PROJECTION_MATRIX), MeshObject::new, onFinish);
    }

    protected void loadStaticModel(RareCandy renderer, String name, Consumer<MeshObject> onFinish) {
        load(renderer, name, Pipelines.staticPipeline(() -> FeatureTester.PROJECTION_MATRIX), MeshObject::new, onFinish);
    }

    protected AnimatedMeshObject loadAnimatedModel(RareCandy renderer, String name, Consumer<AnimatedMeshObject> onFinish) {
        return load(renderer, name, Pipelines.animatedPipeline(() -> FeatureTester.PROJECTION_MATRIX), AnimatedMeshObject::new, onFinish);
    }

    public void rightTap() {

    }

    public void leftTap() {

    }
}
