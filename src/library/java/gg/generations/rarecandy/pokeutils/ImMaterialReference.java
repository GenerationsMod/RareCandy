package gg.generations.rarecandy.pokeutils;

import gg.generations.rarecandy.renderer.model.material.IMaterialImages;
import gg.generations.rarecandy.renderer.model.material.IMaterialValues;
import imgui.ImGui;
import imgui.type.ImString;

final class ImMaterialReference implements IMaterialReference, ImRenderable {
    private final ImString parent;
    private final ImString shader;
    private CullType cull;
    private BlendType blend;
    private final IMaterialImages images;
    private final IMaterialValues values;

    static IMaterialReference empty() {
        return ImModelConfig.FACTORY.createMaterialReference(null, "solid", CullType.None, BlendType.None, null, null);
    }

    ImMaterialReference(String parent, String shader, CullType cull, BlendType blend, IMaterialImages images, IMaterialValues values) {
        this.parent = ImGuiConfigUtil.text(parent);
        this.shader = ImGuiConfigUtil.text(shader);
        this.cull = cull != null ? cull : CullType.None;
        this.blend = blend != null ? blend : BlendType.None;
        this.images = images != null ? images : ImModelConfig.FACTORY.createMaterialImages(null, null, null, null);
        this.values = values != null ? values : ImModelConfig.FACTORY.createMaterialValues(
                IMaterialValues.DEFAULT.baseColor1(), IMaterialValues.DEFAULT.baseColor2(), IMaterialValues.DEFAULT.baseColor3(), IMaterialValues.DEFAULT.baseColor4(), IMaterialValues.DEFAULT.baseColor5(),
                IMaterialValues.DEFAULT.emiColor1(), IMaterialValues.DEFAULT.emiColor2(), IMaterialValues.DEFAULT.emiColor3(), IMaterialValues.DEFAULT.emiColor4(), IMaterialValues.DEFAULT.emiColor5(),
                IMaterialValues.DEFAULT.emiIntensity1(), IMaterialValues.DEFAULT.emiIntensity2(), IMaterialValues.DEFAULT.emiIntensity3(), IMaterialValues.DEFAULT.emiIntensity4(), IMaterialValues.DEFAULT.emiIntensity5(),
                IMaterialValues.DEFAULT.useLight(), IMaterialValues.DEFAULT.disableDepth()
        );
    }

    @Override
    public boolean render() {
        var dirty = false;
        dirty |= ImGui.inputText("Parent", parent);
        dirty |= ImGui.inputText("Shader", shader);
        dirty |= ImGuiConfigUtil.enumCombo("Cull", cull, CullType.values(), value -> cull = value);
        dirty |= ImGuiConfigUtil.enumCombo("Blend", blend, BlendType.values(), value -> blend = value);
        if (ImGui.treeNode("Images")) {
            dirty |= ((ImMaterialImages) images).render();
            ImGui.treePop();
        }
        if (ImGui.treeNode("Values")) {
            dirty |= ((ImMaterialValues) values).render();
            ImGui.treePop();
        }
        return dirty;
    }

    @Override public String parent() { return ImGuiConfigUtil.nullable(parent); }
    @Override public String shader() { return ImGuiConfigUtil.nullable(shader); }
    @Override public CullType cull() { return cull; }
    @Override public BlendType blend() { return blend; }
    @Override public IMaterialImages images() { return images; }
    @Override public IMaterialValues values() { return values; }
}
