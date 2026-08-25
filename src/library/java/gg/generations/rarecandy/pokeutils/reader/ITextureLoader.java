package gg.generations.rarecandy.pokeutils.reader;

import gg.generations.rarecandy.renderer.textures.ITexture;

import java.util.Set;

public abstract class ITextureLoader {
    private static ITextureLoader instance;

    public static ITextureLoader instance() {
        return instance;
    }

    public static void setInstance(ITextureLoader instance) {
        ITextureLoader.instance = instance;
    }

    public abstract boolean contains(String name);

    public abstract ITexture getTexture(String name);

    public ITexture getTexture(String name, String defaultName) {
        if(contains(name)) {
            return getTexture(name);
        } else if(contains(defaultName)) {
            return getTexture(defaultName);
        } else {
            return getDarkFallback();
        }
    }

    public abstract void register(String name, ITexture reference);

    public abstract void register(String id, String fileName, byte[] data);

    public abstract void remove(String name);

    public ITexture getDarkFallback() {
        return getTexture("dark");
    }

    public ITexture getBrightFallback() {
        return getTexture("neutral");
    }
    public ITexture getNuetralFallback() {
        return getTexture("bright");
    }

    public abstract Set<String> getTextureEntries();
}
