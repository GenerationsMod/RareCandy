package com.pokemod.test.tests;

import com.pokemod.pokeutils.LoosePixelAsset;
import com.pokemod.pokeutils.PixelAsset;
import com.pokemod.rarecandy.components.AnimatedMeshObject;
import com.pokemod.rarecandy.components.MultiRenderObject;
import com.pokemod.rarecandy.rendering.RareCandy;
import com.pokemod.rarecandy.storage.AnimatedInstance;
import com.pokemod.test.FeatureTest;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PokemonTest extends FeatureTest {
    private final List<AnimatedInstance> instances = new ArrayList<>();

    @Override
    public void init(RareCandy scene, Matrix4f viewMatrix) {
        super.init(scene, viewMatrix);
        loadPokemonModel(scene, this::loadQuaquavalFromConverter, model -> {
            var i = 0;
            var variants = List.of("none-normal", "none-shiny");

            for (String variant : variants) {
                var instance = new AnimatedInstance(new Matrix4f(), viewMatrix, variant);
                instance.transformationMatrix()
                        .translate(new Vector3f(i * 4 - 2, -1f, 0))
                        .rotate((float) Math.toRadians(180), new Vector3f(0, 1, 0))
                        .rotate((float) Math.toRadians(-90), new Vector3f(-1, 0, 0));
                instances.add(scene.objectManager.add(model, instance));
                i++;
            }
        });
    }

    private PixelAsset loadQuaquavalFromConverter() {
        /*return new PixelAsset("quaquaval", Objects.requireNonNull(PokemonTest.class.getResourceAsStream("/pokemon/quaquaval.pk")).readAllBytes());*/return new LoosePixelAsset(
                Paths.get("D:/Projects/PixelmonGenerations/RareCandy/converter/in/quaquaval_with_glb_anim"),
                Paths.get("quaquaval_with_glb_anim.glb")
        );
    }

    @Override
    public void update(RareCandy scene, double deltaTime) {
    }

    @Override
    public void leftTap() {
        ((MultiRenderObject<AnimatedMeshObject>) instances.get(0).object()).onUpdate(a -> {
            for (var instance : instances) {
                var map = a.animations.values().stream().toList();
                var active = map.indexOf(instance.currentAnimation);
                var newAnimation = map.get(clamp(active - 1, map.size() - 1));
                renderer.objectManager.changeAnimation(instance, newAnimation);
            }
        });
    }

    @Override
    public void rightTap() {
        ((MultiRenderObject<AnimatedMeshObject>) instances.get(0).object()).onUpdate(a -> {
            for (var instance : instances) {
                var map = a.animations.values().stream().toList();
                var active = map.indexOf(instance.currentAnimation);
                var newAnimation = map.get(clamp(active + 1, map.size() - 1));
                System.out.println("animation is now " + newAnimation.name);
                renderer.objectManager.changeAnimation(instance, newAnimation);
            }
        });
    }

    private static int clamp(int value, int max) {
        return Math.min(Math.max(value, 0), max);
    }
}
