package gg.generations.rarecandy.tools;

import gg.generations.rarecandy.pokeutils.reader.ITextureLoader;
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
        long startTime = System.nanoTime();
        MAP.computeIfAbsent(name, s -> {
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1000000;

            if (texture != null) {
                System.out.println("Loaded texture: ID = " + name + " in = " + duration + " ms");
            } else {
                System.err.println("Failed to load texture: ID = " + name + " (texture is null)");
            }
            return texture;
        });
    }

    @Override
    public void register(String id, String fileName, byte[] data) {
        try {
            long startTime = System.nanoTime();
            ITexture texture = Texture.read(data, fileName);
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1000000;
            System.out.println("Loaded texture data for: ID = " + id + " in = " + duration + " ms");
            register(id, texture);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    public ITexture generateDirectReference(String path) {
        try (var is = TextureLoader.class.getResourceAsStream("/textures/" + path)) {
            assert is != null;
            long startTime = System.nanoTime();
            ITexture texture = Texture.read(is.readAllBytes(), path);
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1000000;
            System.out.println("Loaded direct reference for: Path = " + path + ", Load Time = " + duration + " ms");
            return texture;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<String> getTextureEntries() {
        return MAP.keySet();
    }

    public void reload() {
        register("dark", generateDirectReference("dark.png"));
        register("neutral", generateDirectReference("neutral.png"));
        register("bright", generateDirectReference("bright.png"));
        register("paradox_mask", generateDirectReference("paradox_mask.png"));
        register("blank", generateDirectReference("blank.png"));
        register("burnt_concrete", generateDirectReference("burnt_concrete.png"));
        register("concrete", generateDirectReference("concrete.png"));
        register("glass", generateDirectReference("glass.png"));
        register("metal", generateDirectReference("metal.png"));
        register("silver", generateDirectReference("silver.png"));
    }
}
