package com.pixelmongenerations.test.tests;

import com.pixelmongenerations.rarecandy.components.GuiObject;
import com.pixelmongenerations.rarecandy.rendering.InstanceState;
import com.pixelmongenerations.rarecandy.rendering.RareCandy;
import com.pixelmongenerations.test.FeatureTest;
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
