package com.pixelmongenerations.test.tests;

import com.pixelmongenerations.rarecandy.components.AnimatedMeshObject;
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
    private final Stream<String> models = Stream.of("eevee");//, "espeon", "flareon", "glaceon", "jolteon", "leafeon", "umbreon", "vaporeon");
    private List<RenderObjects<AnimatedMeshObject>> objects;

    public AnimationTest() {
        super("animation", "Tests the animation system");
    }

    @Override
    public void init(RareCandy scene, Matrix4f viewMatrix) {
        objects = this.models.map(mdl -> loadAnimatedModel(scene, mdl)).toList();

        for (var model : objects) {
            var instance = new InstanceState(new Matrix4f(), viewMatrix, "normal");
            instance.transformationMatrix().translate(new Vector3f(0, 0, 2)).rotate((float) Math.toRadians(180), new Vector3f(1, 0, 0)).scale(new Vector3f(0.1f, 0.1f, 0.1f));
            scene.addObject(model, instance);
        }
    }

    @Override
    public void update(RareCandy scene, double deltaTime) {
        var timePassed = ((System.currentTimeMillis() - startTime) / 1000/ 1000);

        for (var object : scene.getObjects()) {
            object.transformationMatrix().rotate((float) deltaTime, 0, 1, 0);
        }

        for (var object : objects) {
            for (var animatedSolid : object) {
                animatedSolid.animationTime = animatedSolid.animations.get(animatedSolid.activeAnimation).getAnimationTime(timePassed);
            }
        }
    }
}
