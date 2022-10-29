package com.pokemod.test.tests;

import com.pokemod.rarecandy.rendering.InstanceState;
import com.pokemod.rarecandy.rendering.RareCandy;
import com.pokemod.test.FeatureTest;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class TransparencyFeatureTest extends FeatureTest {

    public TransparencyFeatureTest() {
        super("transparency", "Tests the ability of the renderer to do transparent object sorting");
    }

    @Override
    public void init(RareCandy scene, Matrix4f viewMatrix) {
        loadStaticModel(scene, "solosis", object -> {
            scene.addObject(object, new InstanceState(new Matrix4f().translate(new Vector3f(1, 1, 1)).scale(new Vector3f(0.02f, 0.02f, 0.02f)), viewMatrix, "shiny"));
        });
    }

    @Override
    public void update(RareCandy scene, double deltaTime) {
        for (InstanceState object : scene.getObjects()) {
            object.transformationMatrix().rotate((float) deltaTime, 0, 1, 0);
        }
    }
}
