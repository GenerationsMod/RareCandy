package gg.generations.rarecandy.pokeutils.reader;

import gg.generations.rarecandy.renderer.loading.ITexture;

public abstract class TextureLoader {
    private static TextureLoader instance;


    public static TextureLoader instance() {
        return instance;
    }

    public static void setInstance(TextureLoader instance) {
        TextureLoader.instance = instance;
    }

    public abstract ITexture getTexture(String name);

    public abstract void register(String name, TextureReference reference);

    public abstract void remove(String name);

    public abstract void clear();
}
