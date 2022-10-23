package com.pixelmongenerations.test.tests;

import com.pixelmongenerations.rarecandy.rendering.InstanceState;
import com.pixelmongenerations.rarecandy.rendering.RareCandy;
import com.pixelmongenerations.test.FeatureTest;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class StatUpTest extends FeatureTest {
    public static final double START_TIME = System.currentTimeMillis();

    public StatUpTest() {
        super("stat_up", null);
    }

    @Override
    public void init(RareCandy scene, Matrix4f viewMatrix) {
        loadStatUpModel(scene, (model) -> {
            var instance = new InstanceState(new Matrix4f(), viewMatrix, "none", 0xe60a60);
            instance.transformationMatrix().translate(new Vector3f(0, 0, 1)).rotate((float) Math.toRadians(90), new Vector3f(1, 0, 0)).scale(new Vector3f(0.01f, 0.01f, 0.01f));
            scene.addObject(model, instance);

        });
    }

    public static double getTimePassed() {
        return (System.currentTimeMillis() - START_TIME) / 1000 / 1000;
    }

    @Override
    public void update(RareCandy scene, double deltaTime) {
        for (var object : scene.getObjects()) {
            //object.transformationMatrix().rotate((float) deltaTime, 0, 1, 0);
        }
    }
}
