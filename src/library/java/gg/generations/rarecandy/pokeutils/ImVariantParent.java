package gg.generations.rarecandy.pokeutils;

import imgui.ImGui;
import imgui.type.ImString;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

final class ImVariantParent implements IVariantParent, ImRenderable {
    private final ImString inherits;
    private final Map<String, IVariantDetails> details;
    private final ImConfigEditor editor = new ImConfigEditor();

    static IVariantParent empty() {
        return ImModelConfig.FACTORY.createVariantParent(null, new LinkedHashMap<>());
    }

    ImVariantParent(String inherits, Map<String, IVariantDetails> details) {
        this.inherits = ImGuiConfigUtil.text(inherits);
        this.details = ImGuiConfigUtil.mutableMap(details);
    }

    @Override
    public boolean render() {
        return render(Collections.emptyList());
    }

    boolean render(Collection<String> materialOptions) {
        var dirty = ImGui.inputText("Inherits", inherits);
        dirty |= editor.renderMap("Overrides", "overrides", details, ImVariantDetails::empty, value -> ((ImVariantDetails) value).render(materialOptions), false);
        return dirty;
    }

    @Override public String inherits() { return ImGuiConfigUtil.nullable(inherits); }
    @Override public Map<String, IVariantDetails> details() { return details; }
}
