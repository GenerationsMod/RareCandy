package gg.generations.rarecandy.pokeutils;

import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import imgui.type.ImString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

final class ImConfigEditor {
    private final Map<String, ImString> newKeys = new HashMap<>();
    private final Map<String, Map<String, ImString>> renameBuffers = new HashMap<>();

    <T> boolean renderMap(String label, String id, Map<String, T> map, Supplier<T> factory, Class<? extends ImRenderable> renderableType) {
        return renderMap(label, id, map, factory, value -> renderableType.cast(value).render());
    }

    <T> boolean renderMap(String label, String id, Map<String, T> map, Supplier<T> factory, Class<? extends ImRenderable> renderableType, boolean allowRename) {
        return renderMap(label, id, map, factory, value -> renderableType.cast(value).render(), allowRename);
    }

    boolean renderStringListMap(String label, String id, Map<String, List<String>> map) {
        return renderStringListMap(label, id, map, true);
    }

    boolean renderStringListMap(String label, String id, Map<String, List<String>> map, boolean allowRename) {
        return renderMap(label, id, map, ImStringList::new, value -> {
            if (value instanceof ImStringList list) return list.render("Values");
            return false;
        }, allowRename);
    }

    boolean renderIntegerMap(String label, String id, Map<String, Integer> map) {
        return renderMap(label, id, map, () -> 0, value -> false, (key, value) -> {
            var data = new ImInt(value != null ? value : 0);
            if (!ImGui.inputInt("Value", data)) return null;
            return data.get();
        });
    }

    boolean renderBooleanMap(String label, String id, Map<String, Boolean> map) {
        return renderMap(label, id, map, () -> false, value -> false, (key, value) -> {
            var data = new ImBoolean(Boolean.TRUE.equals(value));
            if (!ImGui.checkbox("Value", data)) return null;
            return data.get();
        });
    }

    boolean renderStringList(String label, String id, ImStringList list) {
        ImGui.pushID(id);
        var dirty = list.render(label);
        ImGui.popID();
        return dirty;
    }

    <T> boolean renderMap(String label, String id, Map<String, T> map, Supplier<T> factory, ImValueRenderer<T> renderer) {
        return renderMap(label, id, map, factory, renderer, true);
    }

    <T> boolean renderMap(String label, String id, Map<String, T> map, Supplier<T> factory, ImValueRenderer<T> renderer, boolean allowRename) {
        return renderMap(label, id, map, factory, renderer, null, allowRename);
    }

    private <T> boolean renderMap(String label, String id, Map<String, T> map, Supplier<T> factory, ImValueRenderer<T> renderer, ImValueEditor<T> valueEditor) {
        return renderMap(label, id, map, factory, renderer, valueEditor, true);
    }

    private <T> boolean renderMap(String label, String id, Map<String, T> map, Supplier<T> factory, ImValueRenderer<T> renderer, ImValueEditor<T> valueEditor, boolean allowRename) {
        var dirty = false;
        ImGui.pushID(id);
        if (ImGui.treeNode(label + " (" + map.size() + ")")) {
            String removeKey = null;
            Rename rename = null;

            for (var entry : new ArrayList<>(map.entrySet())) {
                var key = entry.getKey();
                ImGui.pushID(key);
                if (ImGui.treeNode(key)) {
                    if (allowRename) {
                        var renameBuffer = renameBuffer(id, key);
                        ImGui.inputText("Key", renameBuffer);

                        if (ImGui.button("Rename")) {
                            var newKey = ImGuiConfigUtil.nullable(renameBuffer);
                            if (newKey != null && !newKey.equals(key) && !map.containsKey(newKey)) {
                                rename = new Rename(key, newKey);
                            }
                        }
                        ImGui.sameLine();
                    } else {
                        ImGui.text("Name: " + key);
                    }

                    if (ImGui.button("Remove")) removeKey = key;

                    ImGui.separator();
                    if (valueEditor != null) {
                        var editedValue = valueEditor.render(key, entry.getValue());
                        if (editedValue != null) {
                            map.put(key, editedValue);
                            dirty = true;
                        }
                    } else {
                        dirty |= renderer.render(entry.getValue());
                    }

                    ImGui.treePop();
                }
                ImGui.popID();
            }

            if (rename != null) {
                renameKey(map, rename.oldKey, rename.newKey);
                var buffers = renameBuffers.get(id);
                if (buffers != null) buffers.remove(rename.oldKey);
                dirty = true;
            }

            if (removeKey != null) {
                map.remove(removeKey);
                var buffers = renameBuffers.get(id);
                if (buffers != null) buffers.remove(removeKey);
                dirty = true;
            }

            if (map.isEmpty()) ImGui.textDisabled("empty");

            var newKey = newKeys.computeIfAbsent(id, ignored -> ImGuiConfigUtil.text(""));
            ImGui.inputText("New key", newKey);
            ImGui.sameLine();
            if (ImGui.button("Add")) {
                var key = ImGuiConfigUtil.nullable(newKey);
                if (key != null && !map.containsKey(key)) {
                    map.put(key, factory.get());
                    newKey.clear();
                    dirty = true;
                }
            }

            ImGui.treePop();
        }
        ImGui.popID();
        return dirty;
    }

    private ImString renameBuffer(String id, String key) {
        return renameBuffers
                .computeIfAbsent(id, ignored -> new HashMap<>())
                .computeIfAbsent(key, ignored -> ImGuiConfigUtil.text(key));
    }

    private static <T> void renameKey(Map<String, T> map, String oldKey, String newKey) {
        var renamed = new LinkedHashMap<String, T>();
        map.forEach((key, value) -> renamed.put(key.equals(oldKey) ? newKey : key, value));
        map.clear();
        map.putAll(renamed);
    }

    private record Rename(String oldKey, String newKey) {
    }
}

interface ImValueRenderer<T> {
    boolean render(T value);
}

interface ImValueEditor<T> {
    T render(String key, T value);
}
