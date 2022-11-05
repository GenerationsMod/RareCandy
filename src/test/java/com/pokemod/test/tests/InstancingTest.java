package com.pokemod.test.tests;

import com.pokemod.rarecandy.rendering.ObjectInstance;
import com.pokemod.rarecandy.rendering.RareCandy;
import com.pokemod.test.FeatureTest;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

public class InstancingTest extends FeatureTest {
    private final List<String> models = List.of("articuno_shrine", "moltres_shrine", "zapdos_shrine");

    public InstancingTest() {
        super("instancing", "Tests the ability for the renderer to handle the same model multiple times.");
    }

    @Override
    public void init(RareCandy scene, Matrix4f viewMatrix) {
        int[] xSpacing = {0};
        int[] zSpacing = {0};
        this.models.forEach(mdl -> loadStaticModel(scene, mdl, model -> {
            var instance = new ObjectInstance(new Matrix4f(), viewMatrix, "normal");
            instance.transformationMatrix().rotate((float) Math.toRadians(180), new Vector3f(0, 1, 0));//.scale(new Vector3f(0.04f, 0.04f, 0.04f));
            scene.objectManager.add(model, instance);
//            xSpacing[0] += 4;

//            if (xSpacing[0] % 20 == 0) {
//                zSpacing[0] += 2;
//                xSpacing[0] = 0;
//            }
        }));
    }

    @Override
    public void update(RareCandy scene, double deltaTime) {
        /*for (var object : scene.getObjects()) {
            object.transformationMatrix().rotate((float) deltaTime, 0, 1, 0);
        }*/
    }
}
