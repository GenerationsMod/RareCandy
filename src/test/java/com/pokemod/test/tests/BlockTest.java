package com.pokemod.test.tests;

import com.pokemod.rarecandy.rendering.ObjectInstance;
import com.pokemod.rarecandy.rendering.RareCandy;
import com.pokemod.test.FeatureTest;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class BlockTest extends FeatureTest {
    private final List<ObjectInstance> instances = new ArrayList<>();

    public BlockTest() {
        super("blocks", null);
    }

    @Override
    public void init(RareCandy scene, Matrix4f viewMatrix) {
        super.init(scene, viewMatrix);
        add(scene, viewMatrix, "utility_blocks/pc", -2);
        add(scene, viewMatrix, "utility_blocks/healer", 0);

    }

    private void add(RareCandy scene, Matrix4f viewMatrix, String name, int xOffset) {
        loadStaticModel(scene, name, model -> {
            var instance = new ObjectInstance(new Matrix4f(), viewMatrix, "none");
            instance.transformationMatrix()
                    .scale(1)
                    .rotate((float) Math.toRadians(180), new Vector3f(0, 1, 0))
                    .translate(new Vector3f(xOffset, -1f, -1));
            instances.add(scene.objectManager.add(model, instance));
        });
    }

    @Override
    public void update(RareCandy scene, double deltaTime) {
    }

    @Override
    public void leftTap() {

    }

    @Override
    public void rightTap() {

    }
}
