package com.pokemod.test.tests;

import com.pokemod.rarecandy.components.AnimatedMeshObject;
import com.pokemod.rarecandy.components.MultiRenderObject;
import com.pokemod.rarecandy.rendering.RareCandy;
import com.pokemod.rarecandy.storage.AnimatedInstance;
import com.pokemod.test.FeatureTest;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class AnimationTest extends FeatureTest {
    private final List<AnimatedInstance> instances = new ArrayList<>();

    public AnimationTest() {
        super("animation", "Tests the animation system");
    }

    @Override
    public void init(RareCandy scene, Matrix4f viewMatrix) {
        super.init(scene, viewMatrix);
        loadPokemonModel(scene, "pokeball", model -> {
            var i = 0;
            var variants = List.of("none");

            for (String variant : variants) {
                var instance = new AnimatedInstance(new Matrix4f(), viewMatrix, variant);
                instance.transformationMatrix().translate(new Vector3f(i * 8 - 4, -2f, 8)).scale(1).rotate((float) Math.toRadians(180), new Vector3f(0, 1, 0)).scale(0.01f);
                instances.add(scene.objectManager.add(model, instance));
                i++;
            }
        });
    }

    @Override
    public void update(RareCandy scene, double deltaTime) {
    }

    @Override
    public void leftTap() {
        ((MultiRenderObject<AnimatedMeshObject>) instances.get(0).object()).onUpdate(a -> {
            for (var instance : instances) {
                var map = a.animations.values().stream().toList();
                var oldAnimation = instance.currentAnimation;
                var active = map.indexOf(oldAnimation);
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
                var oldAnimation = instance.currentAnimation;
                var active = map.indexOf(oldAnimation);
                var newAnimation = map.get(clamp(active + 1, map.size() - 1));
                renderer.objectManager.changeAnimation(instance, newAnimation);
            }
        });
    }

    private static int clamp(int value, int max) {
        return Math.min(Math.max(value, 0), max);
    }
}
