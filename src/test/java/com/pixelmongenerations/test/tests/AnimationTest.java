package com.pixelmongenerations.test.tests;

import com.pixelmongenerations.rarecandy.rendering.InstanceState;
import com.pixelmongenerations.rarecandy.rendering.RareCandy;
import com.pixelmongenerations.test.FeatureTest;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.stream.Stream;

public class AnimationTest extends FeatureTest {
    private final Stream<String> models = Stream.of("eevee", "espeon", "flareon", "glaceon", "jolteon", "leafeon", "umbreon", "vaporeon");

    public AnimationTest() {
        super("animation", "Tests the animation system");
    }

    @Override
    public void init(RareCandy scene, Matrix4f viewMatrix) {
        var models = this.models.map(this::loadAnimatedModel).toList();

        for (int i = 0; i < models.size(); i++) {
            var model = models.get(i);
            var instance = new InstanceState(new Matrix4f(), viewMatrix, "normal");
            instance.transformationMatrix().translate(new Vector3f(14 - (i * 4), -2, 8)).scale(new Vector3f(0.06f, 0.06f, 0.06f));
            scene.addObject(model, instance);
        }
    }

    @Override
    public void update(RareCandy scene, double deltaTime) {
        for (InstanceState object : scene.getObjects()) {
            object.transformationMatrix().rotate((float) deltaTime, 0, 1, 0);
        }
    }
}
