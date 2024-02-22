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

public class TextureLoader extends ITextureLoader {

    public static Map<String, ITexture> MAP = new HashMap<>();

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

    public Set<String> getTextureEntries() {
        return MAP.keySet();
    }
}
