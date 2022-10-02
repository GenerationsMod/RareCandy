package com.pixelmongenerations.test.tests;

import com.pixelmongenerations.rarecandy.components.RenderObject;
import com.pixelmongenerations.rarecandy.rendering.InstanceState;
import com.pixelmongenerations.rarecandy.rendering.RareCandy;
import com.pixelmongenerations.test.FeatureTest;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Random;
import java.util.stream.Stream;

public class InstancingTest extends FeatureTest {
    private final Random random = new Random();
    private final Stream<String> models = Stream.of("dimdoors_cube");

    public InstancingTest() {
        super("instancing", "Tests the ability for the renderer to handle the same model multiple times.");
    }

    @Override
    public void init(RareCandy scene, Matrix4f viewMatrix) {
        var compiledModels = this.models.map(mdl -> loadStaticModel(scene, mdl)).toList();
        RenderObject model = compiledModels.get(random.nextInt(compiledModels.size()));
        InstanceState instance = new InstanceState(new Matrix4f(), viewMatrix, "normal");
        instance.transformationMatrix().translate(new Vector3f(0, 0, 0));//.scale(new Vector3f(0.02f, 0.02f, 0.02f));
        scene.addObject(model, instance);
    }

    @Override
    public void update(RareCandy scene, double deltaTime) {
        for (InstanceState object : scene.getObjects()) {
            object.transformationMatrix().rotate((float) deltaTime, 0, 1, 0);
        }
    }
}
