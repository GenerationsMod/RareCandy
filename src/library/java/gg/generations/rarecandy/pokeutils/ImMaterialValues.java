package gg.generations.rarecandy.pokeutils;

import gg.generations.rarecandy.renderer.model.material.IMaterialValues;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import org.joml.Vector3f;

final class ImMaterialValues implements IMaterialValues {
    private final Vector3f baseColor1;
    private final Vector3f baseColor2;
    private final Vector3f baseColor3;
    private final Vector3f baseColor4;
    private final Vector3f baseColor5;
    private final Vector3f emiColor1;
    private final Vector3f emiColor2;
    private final Vector3f emiColor3;
    private final Vector3f emiColor4;
    private final Vector3f emiColor5;
    private final ImFloat emiIntensity1;
    private final ImFloat emiIntensity2;
    private final ImFloat emiIntensity3;
    private final ImFloat emiIntensity4;
    private final ImFloat emiIntensity5;
    private final ImBoolean useLight;
    private final ImBoolean disableDepth;

    ImMaterialValues(Vector3f baseColor1, Vector3f baseColor2, Vector3f baseColor3, Vector3f baseColor4, Vector3f baseColor5, Vector3f emiColor1, Vector3f emiColor2, Vector3f emiColor3, Vector3f emiColor4, Vector3f emiColor5, float emiIntensity1, float emiIntensity2, float emiIntensity3, float emiIntensity4, float emiIntensity5, boolean useLight, boolean disableDepth) {
        this.baseColor1 = ImGuiConfigUtil.mutable(baseColor1, IMaterialValues.DEFAULT.baseColor1());
        this.baseColor2 = ImGuiConfigUtil.mutable(baseColor2, IMaterialValues.DEFAULT.baseColor2());
        this.baseColor3 = ImGuiConfigUtil.mutable(baseColor3, IMaterialValues.DEFAULT.baseColor3());
        this.baseColor4 = ImGuiConfigUtil.mutable(baseColor4, IMaterialValues.DEFAULT.baseColor4());
        this.baseColor5 = ImGuiConfigUtil.mutable(baseColor5, IMaterialValues.DEFAULT.baseColor5());
        this.emiColor1 = ImGuiConfigUtil.mutable(emiColor1, IMaterialValues.DEFAULT.emiColor1());
        this.emiColor2 = ImGuiConfigUtil.mutable(emiColor2, IMaterialValues.DEFAULT.emiColor2());
        this.emiColor3 = ImGuiConfigUtil.mutable(emiColor3, IMaterialValues.DEFAULT.emiColor3());
        this.emiColor4 = ImGuiConfigUtil.mutable(emiColor4, IMaterialValues.DEFAULT.emiColor4());
        this.emiColor5 = ImGuiConfigUtil.mutable(emiColor5, IMaterialValues.DEFAULT.emiColor5());
        this.emiIntensity1 = new ImFloat(emiIntensity1);
        this.emiIntensity2 = new ImFloat(emiIntensity2);
        this.emiIntensity3 = new ImFloat(emiIntensity3);
        this.emiIntensity4 = new ImFloat(emiIntensity4);
        this.emiIntensity5 = new ImFloat(emiIntensity5);
        this.useLight = new ImBoolean(useLight);
        this.disableDepth = new ImBoolean(disableDepth);
    }

    boolean render() {
        var dirty = false;
        dirty |= ImGuiConfigUtil.colorVector3("Base Color 1", baseColor1);
        dirty |= ImGuiConfigUtil.colorVector3("Base Color 2", baseColor2);
        dirty |= ImGuiConfigUtil.colorVector3("Base Color 3", baseColor3);
        dirty |= ImGuiConfigUtil.colorVector3("Base Color 4", baseColor4);
        dirty |= ImGuiConfigUtil.colorVector3("Base Color 5", baseColor5);
        dirty |= ImGuiConfigUtil.colorVector3("Emission Color 1", emiColor1);
        dirty |= ImGuiConfigUtil.colorVector3("Emission Color 2", emiColor2);
        dirty |= ImGuiConfigUtil.colorVector3("Emission Color 3", emiColor3);
        dirty |= ImGuiConfigUtil.colorVector3("Emission Color 4", emiColor4);
        dirty |= ImGuiConfigUtil.colorVector3("Emission Color 5", emiColor5);
        dirty |= ImGui.inputFloat("Emission Intensity 1", emiIntensity1);
        dirty |= ImGui.inputFloat("Emission Intensity 2", emiIntensity2);
        dirty |= ImGui.inputFloat("Emission Intensity 3", emiIntensity3);
        dirty |= ImGui.inputFloat("Emission Intensity 4", emiIntensity4);
        dirty |= ImGui.inputFloat("Emission Intensity 5", emiIntensity5);
        dirty |= ImGui.checkbox("Use light", useLight);
        dirty |= ImGui.checkbox("Disable depth", disableDepth);
        return dirty;
    }

    @Override public Vector3f baseColor1() { return baseColor1; }
    @Override public Vector3f baseColor2() { return baseColor2; }
    @Override public Vector3f baseColor3() { return baseColor3; }
    @Override public Vector3f baseColor4() { return baseColor4; }
    @Override public Vector3f baseColor5() { return baseColor5; }
    @Override public Vector3f emiColor1() { return emiColor1; }
    @Override public Vector3f emiColor2() { return emiColor2; }
    @Override public Vector3f emiColor3() { return emiColor3; }
    @Override public Vector3f emiColor4() { return emiColor4; }
    @Override public Vector3f emiColor5() { return emiColor5; }
    @Override public float emiIntensity1() { return emiIntensity1.get(); }
    @Override public float emiIntensity2() { return emiIntensity2.get(); }
    @Override public float emiIntensity3() { return emiIntensity3.get(); }
    @Override public float emiIntensity4() { return emiIntensity4.get(); }
    @Override public float emiIntensity5() { return emiIntensity5.get(); }
    @Override public boolean useLight() { return useLight.get(); }
    @Override public boolean disableDepth() { return disableDepth.get(); }
}
