package gg.generations.rarecandy.arceus.model.pk;

import gg.generations.rarecandy.legacy.pipeline.ITexture;
import gg.generations.rarecandy.legacy.pipeline.ShaderProgram;
import gg.generations.rarecandy.legacy.pipeline.Texture;
import gg.generations.rarecandy.legacy.pipeline.TextureReference;
import gg.generationsmod.rarecandy.FileLocator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static gg.generations.rarecandy.legacy.pipeline.TextureReference.read;

public class TextureLoader {

    public static Map<String, ITexture> MAP = new HashMap<>();

    private static TextureLoader instance = new TextureLoader();

    public static TextureLoader instance() {
        return instance;
    }

    public TextureLoader() {
        reload();
    }


    public ITexture getTexture(String name) {
        var texture = MAP.getOrDefault(name, null);

        if(texture instanceof TextureReference) {
            texture = ((TextureReference) texture).create();
            MAP.put(name, texture);

        }

        return texture;
    }

    public void register(String name, ITexture texture) {
        MAP.computeIfAbsent(name, s -> texture);
    }

    public void remove(String name) {
        var value = MAP.remove(name);
        if(value != null) {
            try {
                value.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void clear() {
        getTextureEntries().forEach(this::remove);
        reload();
    }

    public TextureReference generateDirectReference(String path) {
        try(var is = ShaderProgram.class.getResourceAsStream("/images/" + path)) {
            var image = path.endsWith(".jxl") ? read(is.readAllBytes()) : ImageIO.read(is);
            int height = image.getHeight();
            int width = image.getWidth();

            // Mirror image if not square. TODO: maybe do this in the shader to save gpu memory and upload time in general
            if (height / width == 2) {
                var mirror = new BufferedImage(width * 2, height, BufferedImage.TYPE_INT_ARGB);
                for (int y = 0; y < height; y++) {
                    for (int lx = 0, rx = width * 2 - 1; lx < width; lx++, rx--) {
                        int p = image.getRGB(lx, y);
                        mirror.setRGB(lx, y, p);
                        mirror.setRGB(rx, y, p);
                    }
                }

                image = mirror;
            }

            return TextureReference.read(image);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void reload() {
        register("dark", generateDirectReference("dark.png"));
        register("neutral", generateDirectReference("neutral.png"));
        register("bright", generateDirectReference("bright.png"));
        register("paradox_mask", generateDirectReference("paradox_mask_tiled.png"));
        register("blank", generateDirectReference("blank.png"));
        register("burnt_concrete", generateDirectReference("burnt_concrete.png"));
        register("concrete", generateDirectReference("concrete.png"));
        register("glass", generateDirectReference("glass.png"));
        register("metal", generateDirectReference("metal.png"));
        register("silver", generateDirectReference("silver.png"));

    }
    public void register(String name, BufferedImage reference) {
        System.out.println("Blorp: "+ name);

        register(name, loadFromReference(reference));
    }

    protected ITexture loadFromReference(BufferedImage reference) {
        return TextureReference.of(reference);
    }


    public ITexture getDarkFallback() {
        return getTexture("dark");
    }

    public ITexture getBrightFallback() {
        return getTexture("neutral");
    }
    public ITexture getNuetralFallback() {
        return getTexture("bright");
    }

    public Set<String> getTextureEntries() {
        return MAP.keySet();
    }
}
