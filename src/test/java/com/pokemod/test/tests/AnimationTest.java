package com.pokemod.test.tests;

import com.pokemod.rarecandy.components.AnimatedMeshObject;
import com.pokemod.rarecandy.components.MutiRenderObject;
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
    private List<MutiRenderObject<AnimatedMeshObject>> objects;

    public AnimationTest() {
        super("animation", "Tests the animation system");
    }

    @Override
    public void init(RareCandy scene, Matrix4f viewMatrix) {
        objects = this.models.map(mdl -> loadAnimatedModel(scene, mdl, model -> {
            var i = 0;
            var variants = List.of("none-normal", "none-shiny");

            for (String variant : variants) {
                var instance = new InstanceState(new Matrix4f(), viewMatrix, variant, 0xe60a60);
                instance.transformationMatrix().translate(new Vector3f(i, 0, 0))/*.rotate((float) Math.toRadians(180), new Vector3f(1, 0, 0))*/;//.mul(scale));
                scene.addObject(model, instance);
                i++;
            }
        })).toList();
    }

    @Override
    public void update(RareCandy scene, double deltaTime) {
        var timePassed = ((System.currentTimeMillis() - startTime) / 1000 / 1000);

        for (var object : scene.getObjects()) {
//            object.transformationMatrix().rotate((float) deltaTime, 0, 1, 0);
        }

        for (var obj : objects) {
            obj.apply(object -> object.animationTime = object.animations.get(object.activeAnimation).getAnimationTime(timePassed));
        }
    }

    @Override
    public void leftTap() {
        for (var obj : objects) {
            obj.apply(a -> {
                var map = a.animations.keySet().stream().toList();

                var active = map.indexOf(a.activeAnimation);

                a.activeAnimation = map.get(clamp(active - 1, 0, map.size() - 1));
            });
        }
    }

    @Override
    public void rightTap() {
        for (var obj : objects) {
            obj.apply(a -> {
                var map = a.animations.keySet().stream().toList();
                var active = map.indexOf(a.activeAnimation);

                a.activeAnimation = map.get(clamp(active + 1, 0, map.size() - 1));
            });
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }
}
