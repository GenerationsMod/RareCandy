package com.pokemod.rarecandy.tools.pixelmonTester;

import com.pokemod.pokeutils.LoosePixelAsset;
import com.pokemod.pokeutils.PixelAsset;
import com.pokemod.rarecandy.components.AnimatedMeshObject;
import com.pokemod.rarecandy.components.MeshObject;
import com.pokemod.rarecandy.components.MultiRenderObject;
import com.pokemod.rarecandy.loading.ModelLoader;
import com.pokemod.rarecandy.pipeline.Pipeline;
import com.pokemod.rarecandy.rendering.RareCandy;
import com.pokemod.rarecandy.storage.AnimatedInstance;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PokemonTest {
    private final List<AnimatedInstance> instances = new ArrayList<>();
    private final Path path;
    public RareCandy renderer;
    public Pipelines pipelines;

    public PokemonTest(String[] args) {
        this.path = Paths.get(args[0]);
    }

    public void init(RareCandy scene, Matrix4f projectionMatrix, Matrix4f viewMatrix) {
        this.renderer = scene;
        this.pipelines = new Pipelines(projectionMatrix);

        loadPokemonModel(scene, this::getAsset, model -> {
            var variants = List.of("none-normal", "none-shiny");

            for (int i = 0; i < variants.size(); i++) {
                var instance = new AnimatedInstance(new Matrix4f(), viewMatrix, variants.get(i));
                instance.transformationMatrix()
                        .translate(new Vector3f(i * 4 - 2, -1f, 2))
                        .rotate((float) Math.toRadians(-180), new Vector3f(0, 1, 0));
                        //.rotate((float) Math.toRadians(-90), new Vector3f(0, 1, 0));
                instances.add(scene.objectManager.add(model, instance));
            }
        });
    }

    private PixelAsset getAsset() {
        try {
            try(var files = Files.list(path)) {
                return new LoosePixelAsset(
                        path,
                        Paths.get(path.getFileName().toString() + ".glb"),
                        files.toArray(Path[]::new)
                );
            }
        } catch (IOException e) {
            throw new RuntimeException("Fuck", e);
        }
    }

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
        load(renderer, assetSupplier, s -> pipelines.animated, onFinish, AnimatedMeshObject::new);
    }

    public void leftTap() {
        var a = instances.get(0).getAnimatedMesh();

        for (var instance : instances) {
            var map = a.animations.values().stream().toList();
            var active = map.indexOf(instance.currentAnimation);
            var newAnimation = map.get(clamp(active - 1, map.size() - 1));
            System.out.println(newAnimation.name);
            renderer.objectManager.changeAnimation(instance, newAnimation);
        }
    }

    public void rightTap() {
        var a = instances.get(0).getAnimatedMesh();

        for (var instance : instances) {
            var map = a.animations.values().stream().toList();
            var active = map.indexOf(instance.currentAnimation);
            var newAnimation = map.get(clamp(active + 1, map.size() - 1));
            System.out.println(newAnimation.name);
            renderer.objectManager.changeAnimation(instance, newAnimation);
        }
    }

    private static int clamp(int value, int max) {
        return Math.min(Math.max(value, 0), max);
    }
}
