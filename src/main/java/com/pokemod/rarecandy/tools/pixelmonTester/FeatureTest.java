package com.pokemod.rarecandy.tools.pixelmonTester;

import com.pokemod.pokeutils.PixelAsset;
import com.pokemod.rarecandy.components.AnimatedMeshObject;
import com.pokemod.rarecandy.components.MeshObject;
import com.pokemod.rarecandy.components.MultiRenderObject;
import com.pokemod.rarecandy.loading.ModelLoader;
import com.pokemod.rarecandy.pipeline.Pipeline;
import com.pokemod.rarecandy.rendering.RareCandy;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class FeatureTest {
    public RareCandy renderer;
    public Pipelines pipelines;

    public void init(RareCandy scene, Matrix4f projectionMatrix, Matrix4f viewMatrix) {
        this.renderer = scene;
        this.pipelines = new Pipelines(projectionMatrix);
    }

    public abstract void update(RareCandy scene, double deltaTime);

    protected <T extends MeshObject> void load(RareCandy renderer, Supplier<PixelAsset> asset, Function<String, Pipeline> pipelineFactory, Consumer<MultiRenderObject<T>> onFinish, Supplier<T> supplier) {
        var loader = renderer.getLoader();
        loader.createObject(
                asset,
                (gltfModel, smdFileMap, gfbFileMap, object) -> {
                    var glCalls = new ArrayList<Runnable>();
                    try {
                        ModelLoader.create2(object, gltfModel, smdFileMap, gfbFileMap, glCalls, pipelineFactory, supplier);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to interpret data", e);
                    }
                    return glCalls;
                },
                onFinish
        );
    }

    protected void loadPokemonModel(RareCandy renderer, Supplier<PixelAsset> assetSupplier, Consumer<MultiRenderObject<AnimatedMeshObject>> onFinish) {
        load(renderer, assetSupplier, this::getPokemonPipeline, onFinish, AnimatedMeshObject::new);
    }

    private Pipeline getPokemonPipeline(String materialName) {
        return pipelines.animated;
    }

    public void rightTap() {}

    public void leftTap() {}
}
