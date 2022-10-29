package com.pokemod.test.tests;

import com.pokemod.rarecandy.rendering.InstanceState;
import com.pokemod.rarecandy.rendering.RareCandy;
import com.pokemod.test.FeatureTest;
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
        int[] xSpacing = {0};
        int[] zSpacing = {0};
        this.models.forEach(mdl -> loadStaticModel(scene, mdl, model -> {
            var instance = new InstanceState(new Matrix4f(), viewMatrix, "normal");
            instance.transformationMatrix().translate(new Vector3f(8 - xSpacing[0], -1, 4 + zSpacing[0])).rotate((float) Math.toRadians(90), new Vector3f(1, 0, 0)).scale(new Vector3f(0.04f, 0.04f, 0.04f));
            scene.addObject(model, instance);
            xSpacing[0] += 4;

            if (xSpacing[0] % 20 == 0) {
                zSpacing[0] += 2;
                xSpacing[0] = 0;
            }
        }));
    }

    @Override
    public void update(RareCandy scene, double deltaTime) {
        for (var object : scene.getObjects()) {
            object.transformationMatrix().rotate((float) deltaTime, 0, 0, 1);
        }
    }
}
