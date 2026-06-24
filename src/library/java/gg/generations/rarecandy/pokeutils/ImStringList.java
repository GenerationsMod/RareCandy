package gg.generations.rarecandy.pokeutils;

import imgui.ImGui;
import imgui.type.ImString;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

final class ImStringList extends AbstractList<String> {
    private final List<ImString> values = new ArrayList<>();
    private final ImString newValue = ImGuiConfigUtil.text("");

    ImStringList() {
    }

    ImStringList(List<String> values) {
        if (values != null) values.forEach(value -> this.values.add(ImGuiConfigUtil.text(value)));
    }

    boolean render(String label) {
        var dirty = false;
        if (ImGui.treeNode(label + " (" + values.size() + ")")) {
            int remove = -1;
            for (int i = 0; i < values.size(); i++) {
                ImGui.pushID(i);
                dirty |= ImGui.inputText("Value", values.get(i));
                ImGui.sameLine();
                if (ImGui.button("Remove")) remove = i;
                ImGui.popID();
            }

            if (remove >= 0) {
                values.remove(remove);
                dirty = true;
            }

            if (values.isEmpty()) ImGui.textDisabled("empty");

            ImGui.inputText("New value", newValue);
            ImGui.sameLine();
            if (ImGui.button("Add")) {
                values.add(ImGuiConfigUtil.text(newValue.get()));
                newValue.clear();
                dirty = true;
            }

            ImGui.treePop();
        }
        return dirty;
    }

    @Override public String get(int index) { return values.get(index).get(); }
    @Override public int size() { return values.size(); }

    @Override
    public String set(int index, String element) {
        var old = get(index);
        values.get(index).set(element);
        return old;
    }

    @Override public void add(int index, String element) { values.add(index, ImGuiConfigUtil.text(element)); }
    @Override public String remove(int index) { return values.remove(index).get(); }
}
