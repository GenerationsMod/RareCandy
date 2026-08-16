package gg.generations.rarecandy.tools.gui.imgui;

import imgui.ImGui;

public class ImInt {
    private final imgui.type.ImInt value;
    private final int min, max;
    private final String name;

    public ImInt(String name, int initValue, int min, int max) {
        this.name = name;
        this.value = new imgui.type.ImInt(initValue);
        this.min = min;
        this.max = max;
    }

    public boolean render() {
        if (ImGui.sliderInt(name, value.getData(), min, max)) {
            return true;
        } else {
            return false;
        }
    }

    public int getValue() {
        return value.get();
    }

    public void setValue(int value) {
        this.value.set(Math.clamp(value, min, max));
    }
}
