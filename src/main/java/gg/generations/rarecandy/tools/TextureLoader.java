package gg.generations.rarecandy.tools;

import gg.generations.rarecandy.pokeutils.reader.ITextureLoader;
import gg.generations.rarecandy.pokeutils.reader.TextureReference;
import gg.generations.rarecandy.renderer.loading.ITexture;
import gg.generations.rarecandy.renderer.loading.Texture;
import gg.generations.rarecandy.renderer.pipeline.Pipeline;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TextureLoader extends ITextureLoader {
    public static Map<String, ITexture> MAP = new HashMap<>();

    public TextureLoader() {
        reload();
    }

    @Override
    public ITexture getTexture(String name) {
        return MAP.getOrDefault(name, null);
    }

    @Override
    public void register(String name, ITexture texture) {
        MAP.computeIfAbsent(name, s -> texture);
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
    public TextureReference generateDirectReference(String path) {
        try (var is = Pipeline.class.getResourceAsStream("/textures/" + path)) {
            assert is != null;
            return TextureReference.read(is.readAllBytes(), path, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected ITexture loadFromReference(TextureReference reference) {
        return new Texture(reference);
    }

    @Override
    public Set<String> getTextureEntries() {
        return MAP.keySet();
    }
}
