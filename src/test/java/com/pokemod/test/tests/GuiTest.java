package com.pokemod.test.tests;

import com.pokemod.rarecandy.components.GuiObject;
import com.pokemod.rarecandy.rendering.InstanceState;
import com.pokemod.rarecandy.rendering.RareCandy;
import com.pokemod.test.FeatureTest;
import org.joml.Matrix4f;

public class GuiTest extends FeatureTest {

    public GuiTest() {
        super("gui", "gui test");
    }

    @Override
    public void init(RareCandy scene, Matrix4f viewMatrix) {
        var guiTest = new GuiObject();
        scene.addObject(guiTest, new InstanceState(new Matrix4f(), null, null));
    }

    @Override
    public void update(RareCandy scene, double deltaTime) {

    }
}
