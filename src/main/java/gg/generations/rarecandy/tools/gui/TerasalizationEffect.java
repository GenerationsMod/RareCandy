package gg.generations.rarecandy.tools.gui;

import imgui.ImGui;
import imgui.type.ImBoolean;
import org.joml.Vector3f;

public class TerasalizationEffect {
    public static final ImBoolean enabled = new ImBoolean(true);
    public static final Vector3f teraColor = new Vector3f(1f,1f, 1f);

    private static final float[] colorArray = new float[] {1f, 1f, 1f};

    public static void render() {
        ImGui.begin("Terastalization");

        ImGui.checkbox("Enabled", enabled);

        if(ImGui.colorEdit3("Tint", colorArray)) {
            teraColor.x = colorArray[0];
            teraColor.y = colorArray[1];
            teraColor.z = colorArray[2];
        }

        ImGui.end();
    }
}
