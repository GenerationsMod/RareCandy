package gg.generations.rarecandy.tools;

import gg.generations.rarecandy.pokeutils.reader.TextureReference;
import gg.generations.rarecandy.renderer.loading.ITexture;
import gg.generations.rarecandy.renderer.loading.Texture;
import gg.generations.rarecandy.renderer.pipeline.Pipeline;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TextureLoader extends gg.generations.rarecandy.pokeutils.reader.TextureLoader {
    public static Map<String, Texture> MAP = new HashMap<>();

    private Texture darkFallback = createFallback("dark.png");

    private Texture createFallback(String path) {
        try (var is = Pipeline.class.getResourceAsStream("/textures/" + path);) {
            assert is != null;
            return new Texture(TextureReference.read(is.readAllBytes(), path));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Texture neutralFallback = createFallback("neutral.png");
    private Texture brightFallback = createFallback("bright.png");

    @Override
    public ITexture getTexture(String name) {
        var texture = MAP.getOrDefault(name, null);

        return texture != null ? texture.get() : null;
    }

    @Override
    public void register(String name, TextureReference reference) {
        MAP.computeIfAbsent(name, s -> new Texture(reference));
    }

    @Override
    public void remove(String name) {
        var value = MAP.remove(name);
        if(value != null) {
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

    @Override
    public ITexture getDarkFallback() {
        return darkFallback.get();
    }

    @Override
    public ITexture getBrightFallback() {
        return brightFallback.get();
    }

    @Override
    public ITexture getNuetralFallback() {
        return neutralFallback.get();
    }
}
