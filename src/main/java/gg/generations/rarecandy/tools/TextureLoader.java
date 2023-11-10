package gg.generations.rarecandy.tools;

import gg.generations.rarecandy.pokeutils.reader.TextureReference;
import gg.generations.rarecandy.renderer.loading.Texture;
import gg.generations.rarecandy.renderer.model.material.CloseableSupplier;
import gg.generations.rarecandy.renderer.model.material.ImageSupplier;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TextureLoader extends gg.generations.rarecandy.pokeutils.reader.TextureLoader {
    public static Map<String, CloseableSupplier<Texture>> MAP = new HashMap<>();

    @Override
    public CloseableSupplier<Texture> getTexture(String name) {
        return MAP.getOrDefault(name, ImageSupplier.BLANK);
    }

    @Override
    public void register(String name, TextureReference reference) {
        MAP.computeIfAbsent(name, s -> new ImageSupplier(reference));
    }

    @Override
    public void remove(String name) {
        var value = MAP.remove(name);
        if(value == null) {
            try {
                value.close();
            } catch (IOException ignored) {
            }
        }
    }
}
