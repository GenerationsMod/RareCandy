package com.pokemod.rarecandy.tools.pixelmonTester;

import com.pokemod.pokeutils.LoosePixelAsset;
import com.pokemod.pokeutils.PixelAsset;
import com.pokemod.rarecandy.animation.Animation;
import com.pokemod.rarecandy.animation.AnimationInstance;
import com.pokemod.rarecandy.animation.ThreeStageAnimationInstance;
import com.pokemod.rarecandy.components.AnimatedMeshObject;
import com.pokemod.rarecandy.components.MeshObject;
import com.pokemod.rarecandy.components.MultiRenderObject;
import com.pokemod.rarecandy.components.SkyboxRenderObject;
import com.pokemod.rarecandy.loading.CubeMapTexture;
import com.pokemod.rarecandy.loading.ModelLoader;
import com.pokemod.rarecandy.loading.Texture;
import com.pokemod.rarecandy.pipeline.ShaderPipeline;
import com.pokemod.rarecandy.rendering.ObjectInstance;
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
    public static PokemonTest INSTANCE;
    private static final double START_TIME = System.currentTimeMillis();
    private final List<AnimatedObjectInstance> instances = new ArrayList<>();
    private final Path path;
    private boolean rotate;
    public RareCandy renderer;
    public Pipelines pipelines;
    public CubeMapTexture cubeMap;
    public Texture starsTexture;

    public PokemonTest(String[] args) {
        Animation.animationModifier = (animation, s) -> animation.ticksPerSecond = 16;
        this.path = Paths.get(args[0]);
        if (args.length == 3) this.rotate = Boolean.parseBoolean(args[2]);
        INSTANCE = this;
    }

    public void init(RareCandy scene, Matrix4f projectionMatrix, Matrix4f viewMatrix) {
        try {
            this.renderer = scene;
            this.pipelines = new Pipelines(() -> projectionMatrix);
            var skybox = new SkyboxRenderObject(pipelines.skybox);
            scene.objectManager.add(skybox, new ObjectInstance(new Matrix4f(), viewMatrix, ""));
            this.cubeMap = skybox.texture;
            this.starsTexture = new Texture(PokemonTest.class.getResourceAsStream("/shared/stars.png").readAllBytes(), "stars.png");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        loadPokemonModel(scene, this::getAsset, model -> {
            var variants = List.of("normal");

            for (var variant : variants) {
                var instance = new AnimatedObjectInstance(new Matrix4f(), viewMatrix, variant);
                instance.transformationMatrix()
                        .rotate((float) Math.toRadians(90), new Vector3f(0, 1, 0))
                        .translate(new Vector3f(0f, -0.5f, 0))
                        .scale(0.4f);

                instances.add(scene.objectManager.add(model, instance));
            }
        });
    }

    public void loop() {
        for (ObjectInstance instance : instances) {
            if (rotate) instance.transformationMatrix()
                    .rotate((float) Math.toRadians(1f), new Vector3f(0, 1, 0));
        }
    }

    private PixelAsset getAsset() {
        try {
            if (!Files.isDirectory(path)) return new PixelAsset(Files.newInputStream(path), "archive");
            else {
                try (var files = Files.list(path)) {
                    return new LoosePixelAsset(
                            path,
                            Paths.get(path.getFileName().toString() + ".glb"),
                            files.toArray(Path[]::new)
                    );
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Fuck", e);
        }
    }

    protected <T extends MeshObject> void load(RareCandy renderer, Supplier<PixelAsset> asset, Function<String, ShaderPipeline> pipelineFactory, Consumer<MultiRenderObject<T>> onFinish, Supplier<T> supplier) {
        var loader = renderer.getLoader();
        loader.createObject(
                asset,
                (gltfModel, smdFileMap, gfbFileMap, object) -> {
                    try {
                        var glCalls = new ArrayList<Runnable>();
                        ModelLoader.create2(object, gltfModel, smdFileMap, gfbFileMap, glCalls, pipelineFactory, supplier);
                        return glCalls;
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to interpret data", e);
                    }
                },
                onFinish
        );
    }

    protected void loadPokemonModel(RareCandy renderer, Supplier<PixelAsset> assetSupplier, Consumer<MultiRenderObject<AnimatedMeshObject>> onFinish) {
        load(renderer, assetSupplier, this::getPipeline, onFinish, AnimatedMeshObject::new);
    }

    private ShaderPipeline getPipeline(String materialName) {
        var lightingSettings = Pipelines.LightingType.PBR;

        if (materialName.contains("eyes")) lightingSettings = Pipelines.LightingType.EMISSIVE;
        else if (materialName.contains("glow")) lightingSettings = Pipelines.LightingType.GLOSSY_EXPERIMENTAL;
        else if (materialName.contains("emi")) lightingSettings = Pipelines.LightingType.EMISSIVE;
        else if (materialName.contains("fast")) lightingSettings = Pipelines.LightingType.BASIC_FAST;
        return pipelines.cachePipeline(lightingSettings, true);
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

    public void space() {
        for (var instance : instances) {
            if (instance.currentAnimation != null) {
                if (instance.currentAnimation.isPaused()) instance.currentAnimation.unpause();
                else instance.currentAnimation.pause();
            }
        }
    }

    public static double getTimePassed() {
        return (System.currentTimeMillis() - START_TIME) / 200;
    }
}
