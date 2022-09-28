package com.pixelmongenerations.rarecandy.animation;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

public class Storage<T> implements Iterable<Storage.TimeKey<T>> {
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
        var values = new ArrayList<>(keys.values());
        return values.get(i);
    }

    public int size() {
        return keys.size();
    }

    record TimeKey<T>(double time, T value) implements Comparable<TimeKey<T>> {

        @Override
        public int compareTo(@NotNull Storage.TimeKey<T> timeKey2) {
            return Double.compare(time, timeKey2.time);
        }
    }
}
