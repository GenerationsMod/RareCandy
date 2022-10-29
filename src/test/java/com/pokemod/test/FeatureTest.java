package com.pokemod.test;

import com.pokemod.rarecandy.components.AnimatedMeshObject;
import com.pokemod.rarecandy.components.MeshObject;
import com.pokemod.rarecandy.components.MutiRenderObject;
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

    protected <T extends MeshObject> MutiRenderObject<T> load(RareCandy renderer, String name, Pipeline pipeline, Consumer<MutiRenderObject<T>> onFinish, Supplier<T> supplier) {
        var loader = renderer.getLoader();
        return loader.createObject(
                () -> FeatureTest.class.getResourceAsStream("/new/" + name + ".pk"),
                (gltfModel, smdFileMap, object) -> {
                    var glCalls = new ArrayList<Runnable>();
                    GogoatLoader.create(object, gltfModel, smdFileMap, glCalls, pipeline, supplier);
                    return glCalls;
                },
                onFinish
        );
    }

    protected void loadStatUpModel(RareCandy renderer, Consumer<MutiRenderObject<MeshObject>> onFinish) {
        load(renderer, "stat_up", Pipelines.statUpPipeline(() -> FeatureTester.PROJECTION_MATRIX), onFinish, MeshObject::new);
    }

    protected void loadStaticModel(RareCandy renderer, String name, Consumer<MutiRenderObject<MeshObject>> onFinish) {
        load(renderer, name, Pipelines.staticPipeline(() -> FeatureTester.PROJECTION_MATRIX), onFinish, MeshObject::new);
    }

    protected MutiRenderObject<AnimatedMeshObject> loadAnimatedModel(RareCandy renderer, String name, Consumer<MutiRenderObject<AnimatedMeshObject>> onFinish) {
        return load(renderer, name, Pipelines.animatedPipeline(() -> FeatureTester.PROJECTION_MATRIX), onFinish, AnimatedMeshObject::new);
    }

    public void rightTap() {

    }

    public void leftTap() {

    }
}
