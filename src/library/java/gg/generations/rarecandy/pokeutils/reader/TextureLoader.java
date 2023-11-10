package gg.generations.rarecandy.pokeutils.reader;

import gg.generations.rarecandy.renderer.loading.Texture;
import gg.generations.rarecandy.renderer.model.material.CloseableSupplier;
import gg.generations.rarecandy.renderer.model.material.ImageSupplier;

public abstract class TextureLoader {
    private static TextureLoader instance;


    public static TextureLoader instance() {
        return instance;
    }

    public static void setInstance(TextureLoader instance) {
        TextureLoader.instance = instance;
    }

    public abstract CloseableSupplier<Texture> getTexture(String name);

    public abstract void register(String name, TextureReference reference);

    public abstract void remove(String name);
}
