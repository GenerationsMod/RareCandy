package com.pokemod.test.tests;

import com.pokemod.rarecandy.components.AnimatedMeshObject;
import com.pokemod.rarecandy.components.MultiRenderObject;
import com.pokemod.rarecandy.rendering.InstanceState;
import com.pokemod.rarecandy.rendering.RareCandy;
import com.pokemod.test.FeatureTest;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;
import java.util.stream.Stream;

public class AnimationTest extends FeatureTest {
    private final double startTime = System.currentTimeMillis();
    private final Stream<String> models = Stream.of("sobble");//, "espeon", "flareon", "glaceon", "jolteon", "leafeon", "umbreon", "vaporeon");
    private List<MultiRenderObject<AnimatedMeshObject>> objects;

    public AnimationTest() {
        super("animation", "Tests the animation system");
    }

    @Override
    public void init(RareCandy scene, Matrix4f viewMatrix) {
        objects = this.models.map(mdl -> loadPokemonModel(scene, mdl, model -> {
            var i = 0;
            var variants = List.of("none-normal", "none-shiny");

            for (String variant : variants) {
                var instance = new InstanceState(new Matrix4f(), viewMatrix, variant, 0xFFFFFFFF);
                instance.transformationMatrix().translate(new Vector3f(i * 8 - 4, -2f, 8)).scale(1).rotate((float) Math.toRadians(180), new Vector3f(0, 1, 0)).scale(0.3f);
                scene.addObject(model, instance);
                i++;
            }
        })).toList();
    }

    @Override
    public void update(RareCandy scene, double deltaTime) {
        var timePassed = ((System.currentTimeMillis() - startTime) / 16000);

        for (var obj : objects) {
            obj.onUpdate(object -> object.updateAnimationTime(timePassed));
        }
    }

    @Override
    public void leftTap() {
        for (var obj : objects) {
            obj.onUpdate(a -> {
                var map = a.animations.keySet().stream().toList();

                var active = map.indexOf(a.activeAnimation);

                a.activeAnimation = map.get(clamp(active - 1, map.size() - 1));
                System.out.println(a.activeAnimation);
            });
        }
    }

    @Override
    public void rightTap() {
        for (var obj : objects) {
            obj.onUpdate(a -> {
                var map = a.animations.keySet().stream().toList();
                var active = map.indexOf(a.activeAnimation);

                a.activeAnimation = map.get(clamp(active + 1, map.size() - 1));
                System.out.println(a.activeAnimation);
            });
        }
    }

    private static int clamp(int value, int max) {
        return Math.min(Math.max(value, 0), max);
    }
}
