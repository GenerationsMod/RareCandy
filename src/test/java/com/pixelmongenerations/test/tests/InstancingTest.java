package com.pixelmongenerations.test.tests;

import com.pixelmongenerations.rarecandy.rendering.InstanceState;
import com.pixelmongenerations.rarecandy.rendering.RareCandy;
import com.pixelmongenerations.test.FeatureTest;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

public class InstancingTest extends FeatureTest {
    private final List<String> models = List.of("eevee", "umbreon", "vaporeon", "leafeon", "flareon", "glaceon", "jolteon");

    public InstancingTest() {
        super("instancing", "Tests the ability for the renderer to handle the same model multiple times.");
    }

    @Override
    public void init(RareCandy scene, Matrix4f viewMatrix) {
        this.models.forEach(mdl -> loadStaticModel(scene, mdl, model -> {
            var instance = new InstanceState(new Matrix4f(), viewMatrix, "normal");
            instance.transformationMatrix().translate(new Vector3f(0, -1, 2)).rotate((float) Math.toRadians(90), new Vector3f(1, 0, 0)).scale(new Vector3f(0.04f, 0.04f, 0.04f));
            scene.addObject(model, instance);
        }));
    }

    @Override
    public void update(RareCandy scene, double deltaTime) {
        for (InstanceState object : scene.getObjects()) {
            object.transformationMatrix().rotate((float) deltaTime, 0, 0, 1);
        }
    }
}
