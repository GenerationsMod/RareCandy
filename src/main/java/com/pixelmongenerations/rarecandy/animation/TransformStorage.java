package com.pixelmongenerations.rarecandy.animation;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.TreeMap;

public class TransformStorage<T> implements Iterable<TransformStorage.TimeKey<T>> {
    public final TreeMap<Double, TimeKey<T>> keys = new TreeMap<>();
    public TimeKey<T>[] values = new TimeKey[0];

    @NotNull
    @Override
    public Iterator<TimeKey<T>> iterator() {
        return keys.values().iterator();
    }

    public void add(double time, T value) {
        keys.put(time, new TimeKey<>(time, value));
    }

    public TimeKey<T> get(int i) {
        return values()[i];
    }

    public int indexOf(TimeKey<T> value) {
        var values = values();

        for (int i = 0; i < values.length; i++) {
            if (values[i] == value) return i;
        }

        return 0;
    }

    public TimeKey<T> getBefore(TimeKey<T> valueAhead) {
        var index = indexOf(valueAhead);
        return get(index - 1);
    }

    public TimeKey<T>[] values() {
        if (keys.size() != values.length) {
            values = keys.values().<TimeKey<T>>toArray(TimeKey[]::new);
        }

        return values;
    }

    public int size() {
        return keys.size();
    }

    record TimeKey<T>(double time, T value) implements Comparable<TimeKey<T>> {

        @Override
        public int compareTo(@NotNull TransformStorage.TimeKey<T> timeKey2) {
            return Double.compare(time, timeKey2.time);
        }
    }
}
