package gg.generations.rarecandy.pokeutils.util;

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
}
