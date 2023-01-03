package com.pokemod.test.tests;

import com.pokemod.rarecandy.rendering.ObjectInstance;
import com.pokemod.rarecandy.rendering.RareCandy;
import com.pokemod.test.FeatureTest;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class BlockTest extends FeatureTest {
    private static final List<List<String>> BLOCK_MODELS = List.of(
            List.of("utility_blocks/breeder/auto_feeder.glb", "utility_blocks/breeder/auto_feeder_fill.glb", "utility_blocks/breeder/breeder.glb", "utility_blocks/breeder/egg.glb"),
            List.of("utility_blocks/cooking_pot.glb"),
            List.of("utility_blocks/fossil_cleaner.glb"),
            List.of("utility_blocks/fossil_extractor.glb"),
            List.of("utility_blocks/healer.glb"),
            List.of("utility_blocks/pc.glb"),
            List.of("utility_blocks/poke_stop.glb"),
            List.of("utility_blocks/rotom_pc.glb"),
            List.of("utility_blocks/scarecrow.glb"),
            List.of("utility_blocks/table_pc.glb"),
            List.of("utility_blocks/trade_machine.glb"),
            List.of("utility_blocks/vending_machine.glb"),
            List.of("utility_blocks/zygarde_machine.glb")
    );
    private double timeSinceLastReset = 1000;
    private double lastTime = GLFW.glfwGetTime();
    private final List<List<ObjectInstance>> instances = new ArrayList<>();

    public BlockTest() {
        super("blocks", null);
    }

    @Override
    public void init(RareCandy scene, Matrix4f viewMatrix) {
        super.init(scene, viewMatrix);
        for (int i = 0; i < BLOCK_MODELS.size(); i++) {
            add(scene, viewMatrix, BLOCK_MODELS.get(i), -4 + i * 2);
        }
    }

    private void add(RareCandy scene, Matrix4f viewMatrix, List<String> names, int xOffset) {
        var subInstances = new ArrayList<ObjectInstance>();

        for (var name : names) {
            loadStaticModel(scene, name, model -> {
                var instance = new ObjectInstance(new Matrix4f(), viewMatrix, "none");
                instance.transformationMatrix()
                        .scale(1)
                        .rotate((float) Math.toRadians(180), new Vector3f(0, 1, 0))
                        .translate(new Vector3f(xOffset, -1f, -1));
                subInstances.add(scene.objectManager.add(model, instance));
                if (subInstances.size() == names.size()) instances.add(subInstances);
            });
        }
    }

    @Override
    public void update(RareCandy scene, double deltaTime) {
        for (var instanceSet : instances) {
            var pos = instanceSet.get(0).transformationMatrix().getTranslation(new Vector3f());
            if (-pos.x > 10 && timeSinceLastReset > 0.5) {
                timeSinceLastReset = 0;
                pos.x = 8;
                instanceSet.forEach(instance -> instance.transformationMatrix().setTranslation(pos));
            }

            instanceSet.forEach(instance -> instance.transformationMatrix().translate((float) (time() * 5f), 0, 0));
        }

        timeSinceLastReset += time();
        lastTime = GLFW.glfwGetTime();
    }

    public double time() {
        return (GLFW.glfwGetTime() - lastTime);
    }

    @Override
    public void leftTap() {

    }

    @Override
    public void rightTap() {

    }
}
