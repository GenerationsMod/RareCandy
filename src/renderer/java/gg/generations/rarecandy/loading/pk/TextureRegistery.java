package gg.generations.rarecandy.loading.pk;

import gg.generations.rarecandy.legacy.model.misc.ITexture;
import gg.generations.pokeutils.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class TextureRegistery {
    private static Map<ResourceLocation, ITexture> TEXTURES = new HashMap<>();

    public static ITexture get(ResourceLocation location) {
        return TEXTURES.get(location);
    }

    public static void register(ResourceLocation location, ITexture texture) {
        TEXTURES.put(location, texture);
    }

}
