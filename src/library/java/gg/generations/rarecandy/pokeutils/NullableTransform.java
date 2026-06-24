package gg.generations.rarecandy.pokeutils;

import gg.generations.rarecandy.renderer.animation.ITransform;
import imgui.ImGui;
import imgui.type.ImBoolean;

final class NullableTransform {
    private final ImBoolean enabled;
    private final ImTransform value;

    NullableTransform(ITransform value) {
        this.enabled = new ImBoolean(value != null);
        this.value = value instanceof ImTransform imTransform ? imTransform : new ImTransform(value != null ? value.scale() : null, value != null ? value.offset() : null);
    }

    boolean render(String label) {
        var dirty = false;
        if (ImGui.treeNode(label)) {
            dirty |= ImGui.checkbox("Enabled", enabled);
            if (enabled.get()) dirty |= value.render();
            ImGui.treePop();
        }
        return dirty;
    }

    ITransform value() {
        return enabled.get() ? value : null;
    }
}
