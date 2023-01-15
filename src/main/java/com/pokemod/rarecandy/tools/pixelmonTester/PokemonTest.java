package com.pokemod.rarecandy.tools.pixelmonTester;

import com.pokemod.pokeutils.LoosePixelAsset;
import com.pokemod.pokeutils.PixelAsset;
import com.pokemod.rarecandy.components.AnimatedMeshObject;
import com.pokemod.rarecandy.components.MultiRenderObject;
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

public class PokemonTest extends FeatureTest {
    private final List<AnimatedInstance> instances = new ArrayList<>();
    private final Path path;

    public PokemonTest(String[] args) {
        this.path = Paths.get(args[0]);
    }

    @Override
    public void init(RareCandy scene, Matrix4f projectionMatrix, Matrix4f viewMatrix) {
        super.init(scene, projectionMatrix, viewMatrix);
        loadPokemonModel(scene, this::getAsset, model -> {
            var variants = List.of("none-normal", "none-shiny");

            for (int i = 0; i < variants.size(); i++) {
                var instance = new AnimatedInstance(new Matrix4f(), viewMatrix, variants.get(i));
                instance.transformationMatrix()
                        .translate(new Vector3f(i * 4 - 2, -1f, 2))
                        .rotate((float) Math.toRadians(-180), new Vector3f(0, 1, 0));
                instances.add(scene.objectManager.add(model, instance));
            }
        });
    }

    private PixelAsset getAsset() {
        try {
            Path root = path;
            return new LoosePixelAsset(
                    root,
                    Paths.get(root.getFileName().toString() + ".glb"),
                    Files.list(root).toArray(Path[]::new)
            );
        } catch (IOException e) {
            throw new RuntimeException("Fuck", e);
        }
    }

    @Override
    public void update(RareCandy scene, double deltaTime) {
    }

    @Override
    public void leftTap() {
        var a = instances.get(0).getAnimatedMesh();

        for (var instance : instances) {
            var map = a.animations.values().stream().toList();
            var active = map.indexOf(instance.currentAnimation);
            var newAnimation = map.get(clamp(active - 1, map.size() - 1));
            System.out.println("animation is now " + newAnimation.name);
            renderer.objectManager.changeAnimation(instance, newAnimation);
        }
    }

    @Override
    public void rightTap() {
        var a = instances.get(0).getAnimatedMesh();

        for (var instance : instances) {
            var map = a.animations.values().stream().toList();
            var active = map.indexOf(instance.currentAnimation);
            var newAnimation = map.get(clamp(active + 1, map.size() - 1));
            System.out.println("animation is now " + newAnimation.name);
            renderer.objectManager.changeAnimation(instance, newAnimation);
        }
    }

    private static int clamp(int value, int max) {
        return Math.min(Math.max(value, 0), max);
    }
}
