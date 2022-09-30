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
    private final Stream<String> models = Stream.of("raikou");

    public InstancingTest() {
        super("instancing", "Tests the ability for the renderer to handle the same model multiple times.");
    }

    @Override
    public void init(RareCandy scene, Matrix4f viewMatrix) {
        int pokemonRendered = 0;
        var compiledModels = this.models.map(mdl -> loadAnimatedModel(scene, mdl)).toList();
        for (int z = 1; z < 64; z++) {
            for (int x = -5; x < 10; x++) {
                for (int y = 2; y > -4; y--) {
                    RenderObject model = compiledModels.get(random.nextInt(compiledModels.size()));
                    InstanceState instance = new InstanceState(new Matrix4f(), viewMatrix, "shiny");
                    instance.transformationMatrix().translate(new Vector3f(x, y, z));//.scale(new Vector3f(0.02f, 0.02f, 0.02f));
                    scene.addObject(model, instance);
                    pokemonRendered++;
                }
            }
        }
        System.out.println("Models On Screen: " + pokemonRendered);
    }

    @Override
    public void update(RareCandy scene, double deltaTime) {
        for (InstanceState object : scene.getObjects()) {
            object.transformationMatrix().rotate((float) deltaTime, 0, 1, 0);
        }
    }
}
