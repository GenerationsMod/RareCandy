package gg.generations.rarecandy.pokeutils;

import gg.generations.rarecandy.renderer.model.material.IMaterialImages;
import imgui.ImGui;
import imgui.type.ImString;

final class ImMaterialImages implements IMaterialImages {
    private final ImString diffuse;
    private final ImString layer;
    private final ImString mask;
    private final ImString emission;

    ImMaterialImages(String diffuse, String layer, String mask, String emission) {
        this.diffuse = ImGuiConfigUtil.text(diffuse);
        this.layer = ImGuiConfigUtil.text(layer);
        this.mask = ImGuiConfigUtil.text(mask);
        this.emission = ImGuiConfigUtil.text(emission);
    }

    boolean render() {
        var dirty = false;
        dirty |= ImGui.inputText("Diffuse", diffuse);
        dirty |= ImGui.inputText("Layer", layer);
        dirty |= ImGui.inputText("Mask", mask);
        dirty |= ImGui.inputText("Emission", emission);
        return dirty;
    }

    @Override public String diffuse() { return ImGuiConfigUtil.nullable(diffuse); }
    @Override public String layer() { return ImGuiConfigUtil.nullable(layer); }
    @Override public String mask() { return ImGuiConfigUtil.nullable(mask); }
    @Override public String emission() { return ImGuiConfigUtil.nullable(emission); }
}
