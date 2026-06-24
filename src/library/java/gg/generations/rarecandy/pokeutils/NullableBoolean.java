package gg.generations.rarecandy.pokeutils;

import imgui.ImGui;
import imgui.type.ImBoolean;

final class NullableBoolean {
    private final ImBoolean enabled;
    private final ImBoolean value;

    NullableBoolean(Boolean value) {
        this.enabled = new ImBoolean(value != null);
        this.value = new ImBoolean(Boolean.TRUE.equals(value));
    }

    boolean render(String label) {
        var dirty = ImGui.checkbox(label + " enabled", enabled);
        if (enabled.get()) dirty |= ImGui.checkbox(label, value);
        return dirty;
    }

    Boolean value() {
        return enabled.get() ? value.get() : null;
    }
}
