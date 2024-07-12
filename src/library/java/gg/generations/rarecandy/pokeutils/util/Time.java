package gg.generations.rarecandy.pokeutils.util;

import gg.generations.rarecandy.renderer.LoggerUtil;

public record Time(long miliseconds, String title) {
    public static Time create(String title) {
        return new Time(System.currentTimeMillis(), title);
    }

    public void display() {
        LoggerUtil.print(title + " took " + ((System.currentTimeMillis() - miliseconds) / 1000f) + " seconds.");
    }
}
