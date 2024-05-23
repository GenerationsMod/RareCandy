package gg.generations.rarecandy.pokeutils.util;

public record Time(long miliseconds, String title) {
    public static Time create(String title) {
        return new Time(System.currentTimeMillis(), title);
    }

    public void display() {
        System.out.println(title + " took " + ((System.currentTimeMillis() - miliseconds)/1000f) + " seconds.");
    }
}
