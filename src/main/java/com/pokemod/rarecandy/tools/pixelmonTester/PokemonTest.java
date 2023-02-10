package com.pokemod.rarecandy.tools.pixelmonTester;

import com.pokemod.pokeutils.LoosePixelAsset;
import com.pokemod.pokeutils.PixelAsset;
import com.pokemod.rarecandy.animation.Animation;
import com.pokemod.rarecandy.animation.AnimationInstance;
import com.pokemod.rarecandy.animation.ThreeStageAnimationInstance;
import com.pokemod.rarecandy.components.AnimatedMeshObject;
import com.pokemod.rarecandy.components.MeshObject;
import com.pokemod.rarecandy.components.MultiRenderObject;
import com.pokemod.rarecandy.loading.ModelLoader;
import com.pokemod.rarecandy.pipeline.Pipeline;
import com.pokemod.rarecandy.rendering.RareCandy;
import com.pokemod.rarecandy.storage.AnimatedObjectInstance;
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

    private final List<AnimatedObjectInstance> instances = new ArrayList<>();
    private final Path path;
    private boolean rotate;
    public RareCandy renderer;
    public Pipelines pipelines;

    public PokemonTest(String[] args) {
        Animation.animationModifier = (animation, s) -> {
            animation.ticksPerSecond = 16;
        };
        this.path = Paths.get(args[0]);
        if (args.length == 3) this.rotate = Boolean.parseBoolean(args[2]);
    }

    public void init(RareCandy scene, Matrix4f projectionMatrix, Matrix4f viewMatrix) {
        this.renderer = scene;
        this.pipelines = new Pipelines(projectionMatrix);

        loadPokemonModel(scene, this::getAsset, model -> {
            var variants = List.of("normal", "shiny");

            for (int i = 0; i < variants.size(); i++) {
                var instance = new AnimatedObjectInstance(new Matrix4f(), viewMatrix, variants.get(i));
                instance.transformationMatrix()
                        .translate(new Vector3f(i * 4 - 2, -1f, 2))
                        .rotate((float) Math.toRadians(-180), new Vector3f(0, 1, 0))
                        .scale(0.4f);

                if (rotate) instance.transformationMatrix()
                        .rotate((float) Math.toRadians(-90), new Vector3f(0, 1, 0));

                instances.add(scene.objectManager.add(model, instance));
            }
        });
    }

    private PixelAsset getAsset() {
        try {
            try (var files = Files.list(path)) {
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
        var map = instances.get(0).getAnimationsIfAvailable();

        for (var instance : instances) {
            if (instance.currentAnimation instanceof ThreeStageAnimationInstance threeStageAnimationInstance) {
                threeStageAnimationInstance.finish(() -> instance.changeAnimation(new AnimationInstance(map.get("idle"))));
            } else instance.changeAnimation(new AnimationInstance(map.get("idle")));
        }
    }

    public void rightTap() {
        var map = instances.get(0).getAnimationsIfAvailable();

        for (var instance : instances) {
            //instance.changeAnimation(new AnimationInstance(map.get("walk")));
            instance.changeAnimation(new ThreeStageAnimationInstance(map, "rest_start", "rest_loop", "rest_end", "idle", "walk"));
        }
    }

    private static int clamp(int value, int max) {
        return Math.min(Math.max(value, 0), max);
    }

    public void space() {
        for (var instance : instances) {
            if (instance.currentAnimation != null) {
                if (instance.currentAnimation.isPaused()) instance.currentAnimation.unpause();
                else instance.currentAnimation.pause();
            }
        }
    }
}
