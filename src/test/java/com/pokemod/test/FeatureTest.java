package com.pokemod.test;

import com.pokemod.pokeutils.PixelAsset;
import com.pokemod.rarecandy.components.AnimatedMeshObject;
import com.pokemod.rarecandy.components.MeshObject;
import com.pokemod.rarecandy.components.MultiRenderObject;
import com.pokemod.rarecandy.loading.GogoatLoader;
import com.pokemod.rarecandy.pipeline.Pipeline;
import com.pokemod.rarecandy.rendering.RareCandy;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
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

    protected <T extends MeshObject> MultiRenderObject<T> load(RareCandy renderer, String name, Function<String, Pipeline> pipelineFactory, Consumer<MultiRenderObject<T>> onFinish, Supplier<T> supplier) {
        var loader = renderer.getLoader();
        return loader.createObject(
                () -> new PixelAsset(FeatureTest.class.getResourceAsStream("/new/" + name + ".pk")),
                (gltfModel, smdFileMap, object) -> {
                    var glCalls = new ArrayList<Runnable>();
                    GogoatLoader.create(object, gltfModel, smdFileMap, glCalls, pipelineFactory, supplier);
                    return glCalls;
                },
                onFinish
        );
    }

    protected void loadStatUpModel(RareCandy renderer, Consumer<MultiRenderObject<MeshObject>> onFinish) {
        load(renderer, "stat_up", materials -> Pipelines.STAT_UP, onFinish, MeshObject::new);
    }

    protected void loadStaticModel(RareCandy renderer, String name, Consumer<MultiRenderObject<MeshObject>> onFinish) {
        load(renderer, name, materials -> Pipelines.STATIC, onFinish, MeshObject::new);
    }

    protected MultiRenderObject<AnimatedMeshObject> loadAnimatedModel(RareCandy renderer, String name, Consumer<MultiRenderObject<AnimatedMeshObject>> onFinish) {
        return load(renderer, name, materials -> Pipelines.ANIMATED, onFinish, AnimatedMeshObject::new);
    }

    protected MultiRenderObject<AnimatedMeshObject> loadPokemonModel(RareCandy renderer, String name, Consumer<MultiRenderObject<AnimatedMeshObject>> onFinish) {
        return load(renderer, name, this::getPokemonPipeline, onFinish, AnimatedMeshObject::new);
    }

    private Pipeline  getPokemonPipeline(String materialName) {
        System.out.println("E");
        return Pipelines.ANIMATED;
    }

    public void rightTap() {

    }

    public void leftTap() {

    }
}
