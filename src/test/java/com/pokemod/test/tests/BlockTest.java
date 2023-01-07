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
    private static final List<List<String>> BLOCK_MODELS = List.<List<String>>of(
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
            List.of("utility_blocks/zygarde_machine.glb"),
            List.of("decorations/shop/shop_cabnet.glb"),
            List.of("decorations/shop/shop_cabnet2.glb"),
            List.of("decorations/shop/shop_shelf.glb"),
            List.of("decorations/shop/shop_shelf2.glb"),
            List.of("decorations/shop/shop_shelf_round.glb"),
            List.of("decorations/shop/shop_shelf_round2.glb"),
            List.of("decorations/bench.glb"),
            List.of("decorations/blue_desk.glb"),
            List.of("decorations/bookshelf.glb"),
            List.of("decorations/box.glb"),
            List.of("decorations/clock.glb"),
            List.of("decorations/couch.glb"),
            List.of("decorations/cushion_chair.glb"),
            List.of("decorations/desk.glb"),
            List.of("decorations/double_street_lamp.glb"),
            List.of("decorations/end_table.glb"),
            List.of("decorations/floor_cushion.glb"),
            List.of("decorations/fossil_display.glb"),
            List.of("decorations/fridge.glb"),
            List.of("decorations/gym_sign.glb"),
            List.of("decorations/house_lamp.glb"),
            List.of("decorations/litwick_candle.glb"),
            List.of("decorations/litwick_candles.glb"),
            List.of("decorations/pastel_bean_bag.glb"),
            List.of("decorations/pokeball_statue.glb"),
            List.of("decorations/pokecenter_scarlet_sign.glb"),
            List.of("decorations/pot_plant.glb"),
            List.of("decorations/rug.glb"),
            List.of("decorations/snorlax_bean_bag.glb"),
            List.of("decorations/switch.glb"),
            List.of("decorations/swivel_chair.glb"),
            List.of("decorations/tall_house_lamp.glb"),
            List.of("decorations/trash_can.glb"),
            List.of("decorations/tree.glb"),
            List.of("decorations/umbrella.glb"),
            List.of("decorations/water_float.glb"),
            List.of("shrines/hoopa/prison_bottle_0.glb"),
            List.of("shrines/hoopa/prison_bottle_1.glb"),
            List.of("shrines/hoopa/prison_bottle_2.glb"),
            List.of("shrines/hoopa/prison_bottle_3.glb"),
            List.of("shrines/hoopa/prison_bottle_4.glb"),
            List.of("shrines/hoopa/prison_bottle_5.glb"),
            List.of("shrines/hoopa/prison_bottle_6.glb")
    );
    private double timeSinceLastReset = 1000;
    private double lastTime = GLFW.glfwGetTime();
    private final List<List<ObjectInstance>> recycleQueue = new ArrayList<>();
    private final List<List<ObjectInstance>> instances = new ArrayList<>();

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
            if (-pos.x > 20 && !recycleQueue.contains(instanceSet)) recycleQueue.add(instanceSet);
            instanceSet.forEach(instance -> instance.transformationMatrix().translate((float) (time() * 5f), 0, 0));
        }

        if (timeSinceLastReset > 0.5 && recycleQueue.size() > 0) {
            var nextSet = recycleQueue.remove(0);
            var pos = nextSet.get(0).transformationMatrix().getTranslation(new Vector3f());
            timeSinceLastReset = 0;
            pos.x = 8;
            nextSet.forEach(instance -> instance.transformationMatrix().setTranslation(pos));
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
