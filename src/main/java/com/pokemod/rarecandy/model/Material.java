package com.pokemod.rarecandy.model;

import com.pokemod.pkl.reader.TextureReference;
import com.pokemod.rarecandy.loading.Texture;

public class Material {

    private final String materialName;
    public final TextureReference diffuseTextureReference;
    private Texture diffuseTexture;

    public Material(String materialName, TextureReference diffuseTexture) {
        this.materialName = materialName;
        this.diffuseTextureReference = diffuseTexture;
    }

    public Texture getDiffuseTexture() {
        if(diffuseTexture == null) this.diffuseTexture = new Texture(diffuseTextureReference);
        return diffuseTexture;
    }

    public String getMaterialName() {
        return materialName;
    }
}
