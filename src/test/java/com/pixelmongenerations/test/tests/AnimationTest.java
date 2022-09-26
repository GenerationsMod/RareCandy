package com.pixelmongenerations.test.tests;

import com.pixelmongenerations.rarecandy.components.AnimatedSolid;
import com.pixelmongenerations.rarecandy.components.RenderObjects;
import com.pixelmongenerations.rarecandy.rendering.InstanceState;
import com.pixelmongenerations.rarecandy.rendering.RareCandy;
import com.pixelmongenerations.test.FeatureTest;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;
import java.util.stream.Stream;

public class AnimationTest extends FeatureTest {
    private final double startTime = System.currentTimeMillis();
    private final Stream<String> models = Stream.of("eevee", "espeon", "flareon", "glaceon", "jolteon", "leafeon", "umbreon", "vaporeon");
    private List<RenderObjects<AnimatedSolid>> objects;
    private boolean alreadyUpdated;

    public AnimationTest() {
        super("animation", "Tests the animation system");
    }

    @Override
    public void init(RareCandy scene, Matrix4f viewMatrix) {
        objects = this.models.map(this::loadAnimatedModel).toList();

        for (int i = 0; i < objects.size(); i++) {
            var model = objects.get(i);
            var instance = new InstanceState(new Matrix4f(), viewMatrix, "normal");
            instance.transformationMatrix().translate(new Vector3f(14 - (i * 4), -2, 8)).scale(new Vector3f(0.06f, 0.06f, 0.06f));
            scene.addObject(model, instance);
        }
    }

    @Override
    public void update(RareCandy scene, double deltaTime) {
        var timePassed = ((System.currentTimeMillis() - startTime) / 1000);

        for (var object : scene.getObjects()) {
            object.transformationMatrix().rotate((float) deltaTime, 0, 1, 0);
        }

        if ((int) timePassed % 3 == 0) {
            if (!alreadyUpdated) {
                for (var object : objects) {
                    for (var animatedSolid : object) {
                        animatedSolid.activeAnimation++;
                        if (animatedSolid.activeAnimation >= animatedSolid.animations.length) animatedSolid.activeAnimation = 0;
                    }
                }
            }

            alreadyUpdated = true;
        } else {
            alreadyUpdated = false;
        }

        for (var object : objects) {
            for (var animatedSolid : object) {
                animatedSolid.animationTime = animatedSolid.animations[animatedSolid.activeAnimation].getAnimationTime(timePassed);
            }
        }
    }
}
