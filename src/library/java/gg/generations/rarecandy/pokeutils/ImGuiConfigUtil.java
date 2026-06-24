package gg.generations.rarecandy.pokeutils;

import imgui.ImGui;
import imgui.type.ImInt;
import imgui.type.ImString;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class ImGuiConfigUtil {
    static final String[] EFFECT_OPTIONS = { "galaxy", "pastel", "shadow", "sketch", "vintage" };

    private static final int TEXT_CAPACITY = 512;

    private ImGuiConfigUtil() {
    }

    static <T> Map<String, T> mutableMap(Map<String, T> map) {
        if (map == null || map.isEmpty()) return new LinkedHashMap<>();
        if (map instanceof LinkedHashMap<?, ?>) return map;
        return new LinkedHashMap<>(map);
    }

    static Map<String, List<String>> mutableStringListMap(Map<String, List<String>> map) {
        var mutable = new LinkedHashMap<String, List<String>>();
        if (map != null) map.forEach((key, value) -> mutable.put(key, new ImStringList(value)));
        return mutable;
    }

    static ImString text(String value) {
        return new ImString(value != null ? value : "", TEXT_CAPACITY);
    }

    static String nullable(ImString value) {
        var string = value.get();
        return string == null || string.isBlank() ? null : string;
    }

    static Vector2f mutable(Vector2f value, Vector2f fallback) {
        return new Vector2f(value != null ? value : fallback);
    }

    static Vector3f mutable(Vector3f value, Vector3f fallback) {
        return new Vector3f(value != null ? value : fallback);
    }

    static Quaternionf mutable(Quaternionf value, Quaternionf fallback) {
        return new Quaternionf(value != null ? value : fallback);
    }

    static boolean inputVector2(String label, Vector2f value) {
        var data = new float[] { value.x(), value.y() };
        if (!ImGui.inputFloat2(label, data)) return false;
        value.set(data[0], data[1]);
        return true;
    }

    static boolean inputVector3(String label, Vector3f value) {
        var data = new float[] { value.x(), value.y(), value.z() };
        if (!ImGui.inputFloat3(label, data)) return false;
        value.set(data[0], data[1], data[2]);
        return true;
    }

    static boolean inputQuaternion(String label, Quaternionf value) {
        var data = new float[] { value.x(), value.y(), value.z(), value.w() };
        if (!ImGui.inputFloat4(label, data)) return false;
        value.set(data[0], data[1], data[2], data[3]);
        return true;
    }

    static boolean colorVector3(String label, Vector3f value) {
        var data = new float[] { value.x(), value.y(), value.z() };
        if (!ImGui.colorEdit3(label, data)) return false;
        value.set(data[0], data[1], data[2]);
        return true;
    }

    static <T extends Enum<T>> boolean enumCombo(String label, T value, T[] values, EnumSetter<T> setter) {
        var names = Arrays.stream(values).map(Enum::name).toArray(String[]::new);
        var selected = new ImInt(Math.max(0, Arrays.asList(values).indexOf(value)));
        if (!ImGui.combo(label, selected, names)) return false;
        setter.set(values[selected.get()]);
        return true;
    }

    static boolean stringCombo(String label, ImString value, Collection<String> options, boolean nullable) {
        var current = nullable(value);
        var preview = current != null ? current : "";
        var dirty = false;

        if (ImGui.beginCombo(label, preview)) {
            if (nullable && ImGui.selectable("<none>##null", current == null)) {
                value.clear();
                dirty = true;
            }

            var foundCurrent = current == null;
            for (var option : options) {
                if (option == null || option.isBlank()) continue;
                if (option.equals(current)) foundCurrent = true;
                if (ImGui.selectable(option, option.equals(current))) {
                    value.set(option);
                    dirty = true;
                }
            }

            if (!foundCurrent && current != null && ImGui.selectable(current, true)) {
                value.set(current);
            }

            ImGui.endCombo();
        }

        return dirty;
    }

    static boolean stringCombo(String label, ImString value, String[] options, boolean nullable) {
        return stringCombo(label, value, Arrays.asList(options), nullable);
    }

    interface EnumSetter<T> {
        void set(T value);
    }
}
