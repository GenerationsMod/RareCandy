package gg.generations.rarecandy.arceus.model.pk;

import gg.generations.rarecandy.legacy.pipeline.ITexture;
import gg.generations.rarecandy.legacy.pipeline.ShaderProgram;
import gg.generations.rarecandy.legacy.pipeline.Texture;
import gg.generations.rarecandy.legacy.pipeline.TextureReference;
import gg.generationsmod.rarecandy.FileLocator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
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

            return TextureReference.read(TextureReference.process(image));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<String> getTextureEntries() {
        return MAP.keySet();
    }
}
