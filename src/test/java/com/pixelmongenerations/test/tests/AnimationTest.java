package com.pixelmongenerations.test.tests;

import com.pixelmongenerations.rarecandy.components.AnimatedMeshObject;
import com.pixelmongenerations.rarecandy.rendering.InstanceState;
import com.pixelmongenerations.rarecandy.rendering.RareCandy;
import com.pixelmongenerations.test.FeatureTest;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;
import java.util.stream.Stream;

public class AnimationTest extends FeatureTest {
    private final double startTime = System.currentTimeMillis();
    private final Stream<String> models = Stream.of("none");//, "espeon", "flareon", "glaceon", "jolteon", "leafeon", "umbreon", "vaporeon");
    private List<AnimatedMeshObject> objects;

    public AnimationTest() {
        super("animation", "Tests the animation system");
    }

    @Override
    public void init(RareCandy scene, Matrix4f viewMatrix) {
        objects = this.models.map(mdl -> loadAnimatedModel(scene, mdl,model -> {
            var i = 0;
            var variants = List.of("none-normal", "none-shiny");

            for (String variant : variants) {
                var instance = new InstanceState(new Matrix4f(), viewMatrix, variant, 0xe60a60);
                instance.transformationMatrix().translate(new Vector3f(i, 0, 0))/*.rotate((float) Math.toRadians(180), new Vector3f(1, 0, 0))*/.scale(new Vector3f(0.01f, 0.01f, 0.01f));
                scene.addObject(model, instance);
                i++;
            }
        })).toList();
    }

    @Override
    public void update(RareCandy scene, double deltaTime) {
        var timePassed = ((System.currentTimeMillis() - startTime) / 1000 / 1000);

        for (var object : scene.getObjects()) {
            object.transformationMatrix().rotate((float) deltaTime, 0, 1, 0);
        }

        for (var object : objects) {
            if (object.isReady()) {
                object.animationTime = object.animations.get(object.activeAnimation).getAnimationTime(timePassed);
            }
        }
    }
}
