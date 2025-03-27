package gg.generations.rarecandy.pokeutils.util;

import java.util.HashMap;
import java.util.Map;

public class Util {
    public static <T> T useOrDefault(T oldValue, T newValue, T defaultValue) {
        if(oldValue == null) {
            if(newValue == null) return defaultValue;
            else return newValue;
        } else return oldValue;
    }

    public static <T> T defaultIfNull(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    public static <K, V> Map<K, V> mergeMaps(Map<K, V> main, Map<K, V> reference) {
        if(main == null && reference == null) {
            return new HashMap<>();
        } else if(main == null) {
            main = new HashMap<>(reference);
        } else if(reference == null) {
            return main;
        }

        main.forEach(reference::putIfAbsent);
        return main;
    }
}
