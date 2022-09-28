package com.pixelmongenerations.rarecandy.animation;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

public class TransformStorage<T> implements Iterable<TransformStorage.TimeKey<T>> {
    public final TreeMap<Double, TimeKey<T>> keys = new TreeMap<>();

    @NotNull
    @Override
    public Iterator<TimeKey<T>> iterator() {
        return keys.values().iterator();
    }

    public void add(double time, T value) {
        keys.put(time, new TimeKey<>(time, value));
    }

    public TimeKey<T> get(int i) {
        return values().get(i);
    }

    public int indexOf(TimeKey<T> value) {
        var values = values();

        for (int i = 0; i < values.size(); i++) {
            if(values.get(i) == value) return i;
        }

        return 0;
    }

    public TimeKey<T> getBefore(TimeKey<T> valueAhead) {
        var index = indexOf(valueAhead);
        return get(index - 1);
    }

    public List<TimeKey<T>> values() {
        return new ArrayList<>(keys.values());
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
