package gg.generations.rarecandy.tools;

import gg.generations.rarecandy.pokeutils.reader.TextureReference;
import gg.generations.rarecandy.renderer.loading.ITexture;
import gg.generations.rarecandy.renderer.loading.Texture;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TextureLoader extends gg.generations.rarecandy.pokeutils.reader.TextureLoader {
    public static Map<String, Texture> MAP = new HashMap<>();

    @Override
    public ITexture getTexture(String name) {
        return MAP.getOrDefault(name, null).get();
    }

    @Override
    public void register(String name, TextureReference reference) {
        MAP.computeIfAbsent(name, s -> new Texture(reference));
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

    @Override
    public void clear() {
        MAP.clear();
    }
}
