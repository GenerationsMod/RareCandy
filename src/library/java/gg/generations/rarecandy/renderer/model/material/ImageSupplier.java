package gg.generations.rarecandy.renderer.model.material;

import gg.generations.rarecandy.pokeutils.reader.TextureReference;
import gg.generations.rarecandy.renderer.loading.Texture;

import java.io.IOException;

public class ImageSupplier implements CloseableSupplier<Texture> {
    public static final CloseableSupplier<Texture> BLANK = new CloseableSupplier<>() {
        @Override
        public void close() throws IOException {

        }

        @Override
        public Texture get() {
            return null;
        }
    };

    private final TextureReference textureReference;
    private Texture texture;

    public ImageSupplier(TextureReference textureReference) {
        this.textureReference = textureReference;
    }

    @Override
    public Texture get() {
        if (texture == null) this.texture = new Texture(textureReference);
        return texture;
    }

    @Override
    public void close() throws IOException {
        texture.close();
    }
}
