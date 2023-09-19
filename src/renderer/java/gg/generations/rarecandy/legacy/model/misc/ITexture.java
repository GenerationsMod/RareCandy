package gg.generations.rarecandy.legacy.model.misc;

import gg.generations.pokeutils.reader.TextureReference;

import java.util.function.Function;

public abstract class ITexture {
    private static final Function<TextureReference, ITexture> function = Texture::new;

    public static void setTextureGenerator(Function<TextureReference, Texture> function) {
        function = function;
    }

    public static ITexture generate(TextureReference diffuseTextureReference) {
        return function.apply(diffuseTextureReference);
    }

    public abstract void bind(int slot);
}
