package gg.generations.rarecandy.legacy.model.misc;

import gg.generations.pokeutils.util.ResourceLocation;

public class SolidMaterial implements Material {
    private static String TYPE = "solid";
    private final ResourceLocation difuseTexture;

    public SolidMaterial(ResourceLocation diffuseTexture) {
        difuseTexture = diffuseTexture;
    }

    public String type() {
        return TYPE;
    }

    public ResourceLocation getDiffuseTexture() {
        return difuseTexture;
    }
}
