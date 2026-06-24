package gg.generations.rarecandy.pokeutils;

import gg.generations.rarecandy.renderer.animation.ITransformSet;
import imgui.ImGui;
import imgui.type.ImString;

import java.util.Collection;
import java.util.Collections;

final class ImVariantDetails implements IVariantDetails, ImRenderable {
    private final ImString material;
    private final ImString effect;
    private final NullableBoolean paradox;
    private final NullableBoolean hide;
    private final ITransformSet transform;

    static IVariantDetails empty() {
        return ImModelConfig.FACTORY.createVariantDetails(null, null, null, null, null);
    }

    ImVariantDetails(String material, String effect, Boolean paradox, Boolean hide, ITransformSet transform) {
        this.material = ImGuiConfigUtil.text(material);
        this.effect = ImGuiConfigUtil.text(effect);
        this.paradox = new NullableBoolean(paradox);
        this.hide = new NullableBoolean(hide);
        this.transform = transform != null ? transform : ImModelConfig.FACTORY.createTransformSet(null, null, null, null);
    }

    @Override
    public boolean render() {
        return render(Collections.emptyList());
    }

    boolean render(Collection<String> materialOptions) {
        var dirty = false;
        dirty |= ImGuiConfigUtil.stringCombo("Material", material, materialOptions, true);
        dirty |= ImGuiConfigUtil.stringCombo("Effect", effect, ImGuiConfigUtil.EFFECT_OPTIONS, true);
        dirty |= paradox.render("Paradox");
        dirty |= hide.render("Hide");
        if (ImGui.treeNode("Transform")) {
            dirty |= ((ImTransformSet) transform).render();
            ImGui.treePop();
        }
        return dirty;
    }

    @Override public String material() { return ImGuiConfigUtil.nullable(material); }
    @Override public String effect() { return ImGuiConfigUtil.nullable(effect); }
    @Override public Boolean paradox() { return paradox.value(); }
    @Override public Boolean hide() { return hide.value(); }
    @Override public ITransformSet transform() { return transform; }
}
