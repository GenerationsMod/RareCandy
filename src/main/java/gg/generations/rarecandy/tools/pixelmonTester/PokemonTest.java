package gg.generations.rarecandy.tools.pixelmonTester;

import gg.generations.rarecandy.pokeutils.LoosePixelAsset;
import gg.generations.rarecandy.pokeutils.PixelAsset;
import gg.generations.rarecandy.renderer.animation.Animation;
import gg.generations.rarecandy.renderer.animation.AnimationInstance;
import gg.generations.rarecandy.renderer.animation.ThreeStageAnimationInstance;
import gg.generations.rarecandy.renderer.components.MeshObject;
import gg.generations.rarecandy.renderer.components.MultiRenderObject;
import gg.generations.rarecandy.renderer.loading.ModelLoader;
import gg.generations.rarecandy.renderer.pipeline.Pipeline;
import gg.generations.rarecandy.renderer.rendering.RareCandy;
import gg.generations.rarecandy.renderer.storage.AnimatedObjectInstance;
import org.joml.Matrix4f;

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
    public RareCandy renderer;
//    public Pipelines pipelines;
    private boolean rotate;

    public PokemonTest(String[] args) {
        Animation.animationModifier = (animation, s) -> animation.ticksPerSecond = 16;
        this.path = Paths.get(args[0]);
        if (args.length == 3) this.rotate = Boolean.parseBoolean(args[2]);
    }

    private static int clamp(int value, int max) {
        return Math.min(Math.max(value, 0), max);
    }

    public void init(RareCandy scene, Matrix4f projectionMatrix, Matrix4f viewMatrix) {
//        this.renderer = scene;
//        this.pipelines = new Pipelines(projectionMatrix);
//
//        loadPokemonModel(scene, this::getAsset, model -> {
//            var variants = List.of("none-normal");//, "none-shiny");
//
//            for (int i = 0; i < variants.size(); i++) {
//                var instance = new AnimatedObjectInstance(new Matrix4f(), viewMatrix, variants.get(i));
//                instance.transformationMatrix()
//                        .translate(new Vector3f(i * 4 - 2, -1f, 2))
//                        .rotate((float) Math.toRadians(-180), new Vector3f(0, 1, 0))
//                        .scale(0.4f);
//
//                if (rotate) instance.transformationMatrix()
//                        .rotate((float) Math.toRadians(-90), new Vector3f(0, 1, 0));
//
//                instances.add(scene.objectManager.add(model, instance));
//            }
//        });
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
        loader.createObjectThreaded(
                asset,
                (gltfModel, smdFileMap, gfbFileMap, tranmFilesMaps, images, config, object) -> {
                    var glCalls = new ArrayList<Runnable>();
                    try {
                        ModelLoader.create2(object, gltfModel, smdFileMap, gfbFileMap, tranmFilesMaps, images, config, glCalls, supplier);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to interpret data", e);
                    }
                    return glCalls;
                },
                onFinish
        );
    }

//    protected void loadPokemonModel(RareCandy renderer, Supplier<PixelAsset> assetSupplier, Consumer<MultiRenderObject<AnimatedMeshObject>> onFinish) {
//        load(renderer, assetSupplier, s -> pipelines.animated, onFinish, AnimatedMeshObject::new);
//    }

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

    public void space() {
        for (var instance : instances) {
            if (instance.currentAnimation != null) {
                if (instance.currentAnimation.isPaused()) instance.currentAnimation.unpause();
                else instance.currentAnimation.pause();
            }
        }
    }
}
